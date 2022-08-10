package fun.gatsby.commons.schedule;


import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author Gatsby
 * @date 2022/3/7 15:29
 * @see <a href="https://blog.csdn.net/haohao_ding/article/details/123404531"</a>
 * @see <img src="https://img-blog.csdnimg.cn/883009304fb942b2b20a14d14d85c9fd.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAaGFvaGFvX2Rpbmc=,size_20,color_FFFFFF,t_70,g_se,x_16">
 */
@Slf4j
public class Scheduler {

    /**
     * 执行任务队列数组
     */
    private final LinkedBlockingQueue<Runnable> blockingTaskQueue;
    /**
     * 允许并行执行的线程池
     */
    private final ExecutorService loopExecutor;
    /**
     * 允许并行执行的线程数,数值为并发量加1，额外一条用于添加指向性任务
     */
    private final int nThrends;
    /**
     * 协助判断，是否线程池的任务全部结束
     */
    private final AtomicInteger count = new AtomicInteger();
    /**
     * 任务信息数组
     */
    private final List<TaskGroup> taskGroups;
    /**
     * 队列的容量
     */
    int queueSize;
    /**
     * 执行模式：
     * 1：所有任务信息都执行,一个任务执行完成后，接着执行下一个任务，知道全部完成
     * 2：每次随机取一个任务组的任务开始
     */
    private int mode;

    /**
     * 调度器的执行状态
     */
    private volatile boolean status = false;


    Map<Runnable, TaskGroup> taskGroupMap;

    /**
     * 构造器
     *
     * @param taskGroups 任务数组
     * @param nThrends   同时执行任务中的任务线程数
     * @param queueSize  任务执行队列
     * @param mode       执行模式 1：所有任务信息都执行 2：先执行部分任务，执行完后再执行其他任务
     */
    public Scheduler(int nThrends, int queueSize, int mode, List<TaskGroup> taskGroups) {
        this.taskGroups = new CopyOnWriteArrayList<>(taskGroups);
        this.nThrends = nThrends;
        this.queueSize = queueSize;
        this.loopExecutor = Executors.newFixedThreadPool(this.nThrends + 1);
        this.mode = mode;
        this.blockingTaskQueue = new LinkedBlockingQueue<>(queueSize);
        this.taskGroups.forEach(taskGroup -> this.count.addAndGet(taskGroup.getTaskQueue().size()));

        this.taskGroupMap = new HashMap<>(this.count.get());

        this.taskGroups.forEach(taskGroup -> {
            taskGroup.getTaskQueue().forEach(task -> {
                taskGroupMap.put(task, taskGroup);
            });
        });

    }

    public Scheduler(int nThrends, int mode, List<TaskGroup> taskGroups) {
        this(nThrends, 2 << 7, mode, taskGroups);
    }

    public Scheduler(int nThrends, List<TaskGroup> taskGroups) {
        this(nThrends, 1, taskGroups);
    }

    public Scheduler(int nThrends, TaskGroup... taskGroups) {
        this(nThrends, 1, List.of(taskGroups));
    }

    public int getnThrends() {
        return nThrends;
    }

    public int getQueueSize() {
        return queueSize;
    }

    /**
     * 执行方法
     */
    public void run() {
        this.status = true;
        // 开启向队列中添加执行任务线程,会在队列满时阻塞
        putTaskToQueue();
        // 循环执行执行任务
        //FIXME 这里始调用线程一直在循环取任务加入队列，是不是需要另起线程让调用线程继续往下走
        takeAndExecute();

        int size;
        // 所有线程执行完毕出循环
        do {
            Thread.yield();
            size = this.count.get();
        } while (size != 0);

        //停止线程池
        this.loopExecutor.shutdown();
        log.info("全部任务执行完毕");
        for (; ; ) {
            //只有当线程池中所有线程完成任务时才会返回true，并且需要先调用线程池的shutdown方法或者shutdownNow方法。
            if (this.loopExecutor.isTerminated()) {
                log.debug("执行结束，关闭线程池！");
                break;
            }
        }
    }

    private void takeAndExecute() {
        while (this.status) {
            // 所有执行任务执行完后，退出
            if (this.taskGroups.size() == 0) {
                if (this.blockingTaskQueue.size() == 0) {
                    //退出信号
                    this.status = false;
                    return;
                }
            }
            // 获取一个执行任务
            Runnable task;
            try {
                task = this.blockingTaskQueue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            final Runnable finalTask = task;
            this.loopExecutor.execute(() -> {
                //TODO 根据task获取任务组
                TaskGroup taskGroup = taskGroupMap.get(finalTask);
                int sizeToDecrease = 1;
                try {
                    if ("ERROR".equals(taskGroup.state)) {
                        taskGroups.remove(taskGroup);
                        return;
                    }
                    finalTask.run();
//                        if (taskGroup.getTaskQueue().size() == 0) {
////                            //TODO 还得拍段队列中是否还存在该任务组的任务，如果不存在，则可以调用taskGroups的成功回调函数
//                            if(!blockingTaskQueue.contains(task))
//                                taskGroup.callback();
//                        }
                } catch (Exception e) {
                    log.error("执行任务出错！{}", taskGroup.getId(), e);
                    sizeToDecrease = taskGroup.onTaskException(finalTask);
                } finally {
                    // 当任务执行完毕，将任务数减1˛
                    this.count.addAndGet(-sizeToDecrease);
                }
            });
        }
    }

    /**
     * 开启一个线程，持续向执行任务队列添加执行任务，直到所有的任务任务添加完
     */
    private void putTaskToQueue() {
        loopExecutor.execute(() -> {
            while (this.status) {
                // 任务信息数组数量
                int length = this.taskGroups.size();
                // 执行完结束线程
                if (length == 0) {
                    return;
                }
                // 获取添加执行任务的的任务索引值
                int index = getIndexByMode();
                TaskGroup taskGroup = this.taskGroups.get(index);
                List<Runnable> tasks = taskGroup.getTaskQueue();
                if (tasks.size() > 0) {
                    try {
                        this.blockingTaskQueue.put(tasks.remove(0));
                    } catch (InterruptedException e) {
                        log.error("向执行任务队列放入任务异常", e);
                        throw new RuntimeException(e);
                    }
                } else {
                    this.taskGroups.remove(index);
                }
            }
        });
    }

    /**
     * @return 任务信息索引值
     */
    private int getIndexByMode() {
        if (this.mode == 2) {
            return RandomUtil.randomInt(0, taskGroups.size());
        }
        return 0;
    }
}
