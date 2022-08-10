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

    private volatile boolean cancelled = false;

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


        int mapSize = 0;
        for (TaskGroup taskGroup : taskGroups) {
            mapSize += taskGroup.getTaskQueue().size();
        }

        this.taskGroupMap = new HashMap<>(mapSize);
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

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * 执行方法
     */
    public void run() {
        putTaskToQueueAsync();
        takeAndExecuteSync();

        //停止线程池,在队列中的任务全部执行完毕后，才会停止线程池，该方法不会阻塞f
        if (!cancelled)
            loopExecutor.shutdown();
        else
            loopExecutor.shutdownNow();
        for (; ; ) {
            //只有当线程池中所有线程完成任务时才会返回true，并且需要先调用线程池的shutdown方法或者shutdownNow方法。
            if (this.loopExecutor.isTerminated()) {
                log.info("全部任务执行完毕,关闭线程池");
                break;
            } else {
                Thread.yield();
            }
        }
    }

    /**
     * 循环执行执行任务,在没有额外任务可执行时退出循环
     */
    private void takeAndExecuteSync() {
        while (!cancelled) {
            // 所有执行任务都放入队列后，退出
            if (this.taskGroups.size() == 0) {
                if (this.blockingTaskQueue.size() == 0) {
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
                TaskGroup taskGroup = taskGroupMap.get(finalTask);
                taskGroup.runTask(finalTask);
            });
        }
    }

    /**
     * 开启一个线程，持续向执行任务队列添加执行任务，直到所有的任务任务添加完
     */
    private void putTaskToQueueAsync() {
        loopExecutor.execute(() -> {
            while (!cancelled) {
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
