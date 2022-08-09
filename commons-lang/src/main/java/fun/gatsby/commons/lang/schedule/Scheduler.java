package fun.gatsby.commons.lang.schedule;


import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author dinghao
 * @author Gatsby
 * @date 2022/3/7 15:29
 * @see <a href="https://blog.csdn.net/haohao_ding/article/details/123404531"</a>
 * @see <img src="https://img-blog.csdnimg.cn/883009304fb942b2b20a14d14d85c9fd.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAaGFvaGFvX2Rpbmc=,size_20,color_FFFFFF,t_70,g_se,x_16">
 */
//  TODO 实现taskgroup中出现失败任务取消剩余任务并后置处理的需求
@Slf4j
public class Scheduler {

    /**
     * 执行计划队列数组
     */
    private final LinkedBlockingQueue<ITask> taskQueue;
    /**
     * 允许并行执行的线程池
     */
    private final ExecutorService loopExecutor;
    /**
     * 允许并行执行的线程数
     */
    private final int nThrends;
    /**
     * 队列的数量
     */
    int queueNum;
    /**
     * 队列的容量
     */
    int queueSize;
    /**
     * 协助判断，是否线程池的任务全部结束
     */
    private final AtomicInteger count;
    /**
     * 任务信息数组
     */
    private final List<TaskGroup> taskGroups;
    /**
     * 执行模式：
     * 1：所有任务信息都执行,一个任务执行完成后，接着执行下一个任务，知道全部完成
     * 2：每次随机取一个任务组的任务开shi
     */
    private int mode;
    /**
     * 每批执行的任务数量
     */
    private int batchSize;

    /**
     * 调度器的执行状态
     */
    private volatile boolean status = false;


    /**
     * 构造器
     *
     * @param taskGroups 任务数组
     * @param nThrends   同时执行任务中的计划线程数
     * @param queueSize  计划执行队列
     * @param mode       执行模式 1：所有任务信息都执行 2：先执行部分任务，执行完后再执行其他任务
     * @param batchSize  每批执行任务的数量
     */
    public Scheduler(TaskGroup[] taskGroups, int nThrends, int queueNum, int queueSize, int mode, Integer batchSize) {
        this.taskGroups = new LinkedList<>(CollUtil.newArrayList(taskGroups));
        this.nThrends = nThrends;
        this.queueNum = queueNum;
        this.queueSize = queueSize;
        this.loopExecutor = Executors.newFixedThreadPool(this.nThrends);
        this.mode = mode;

        this.taskQueue = new LinkedBlockingQueue<>(queueSize);

        count = countTask();
    }

    public Scheduler(TaskGroup[] taskGroups, int nThrends, int mode, Integer batchSize) {
        this(taskGroups, nThrends, nThrends, 100, mode, batchSize);
    }

    public Scheduler(int nThrends, TaskGroup... taskGroups) {
        this(taskGroups, nThrends, nThrends, 100, 1, null);
    }

    public Scheduler(TaskGroup[] taskGroups) {
        this(taskGroups, 10, 10, 100, 1, null);
    }

    /**
     * 计算一共有多少执行计划
     *
     * @return /
     */
    private AtomicInteger countTask() {
        int sum = 0;
        for (TaskGroup taskGroup : this.taskGroups) {
            sum += taskGroup.getTaskQueue().size();
        }
        return new AtomicInteger(sum);
    }

    public int getModel() {
        return mode;
    }

    public void setModel(int mode) {
        this.mode = mode;
    }

    public int getModelSize() {
        return batchSize;
    }

    public void setModelSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public AtomicInteger getCount() {
        return count;
    }

    public boolean isStatus() {
        return status;
    }

    /**
     * 执行方法
     */
    public void run() {
        if (this.status) {
            log.warn("Scheuler未就绪");
            return;
        }
        this.status = true;
        // 开启向队列中添加执行计划线程
        init();
        // 循环执行执行计划
        while (this.status) {
            // 所有执行计划执行完后，退出
            if (this.taskGroups.size() == 0) {
                if (this.taskQueue.size() == 0) {
                    this.status = false;
                    break;
                }
            }

            // 执行计划
            this.taskAndExecute();
        }
        int size;
        // 所有线程执行完毕出循环
        do {
            Thread.yield();
            size = this.count.get();
        } while (size != 0);

        //停止线程池
        this.loopExecutor.shutdown();
        log.info("全部任务执行完毕");
//        for (; ; ) {
//            //只有当线程池中所有线程完成任务时才会返回true，并且需要先调用线程池的shutdown方法或者shutdownNow方法。
//            if (this.loopExecutor.isTerminated()) {
//                System.out.println("执行结束！");
//                break;
//            }
//        }
    }

    private void taskAndExecute() {
        try {
            // 获取一个执行计划
            ITask task = this.taskQueue.take();
            this.loopExecutor.execute(() -> task.doWork(this.count));
            // 执行计划
        } catch (InterruptedException e) {
            log.error("任务执行中发生异常", e);
        }
    }

    /**
     * 开启一个线程，持续向执行计划队列添加执行计划，直到所有的计划任务添加完
     */
    private void init() {
        new Thread(() -> {
            while (this.status) {
                // 任务信息数组数量
                int length = this.taskGroups.size();
                // 执行完结束线程
                if (length == 0) {
                    break;
                }
                // 获取添加执行计划的的任务索引值
                int index = getIndexByMode();
                TaskGroup taskGroup = this.taskGroups.get(index);

                List<ITask> plans = taskGroup.getTaskQueue();
                if (plans.size() > 0) {
                    try {
                        if (this.mode >= 3) {
                            this.taskQueue.put(plans.remove(0));
                        } else {
                            this.taskQueue.put(plans.remove(0));
                        }
                    } catch (InterruptedException e) {
                        log.error("向执行计划队列放入计划异常", e);
                        throw new RuntimeException(e);
                    }
                } else {
                    var group = this.taskGroups.remove(index);
//                    log.debug("taskGroup:{} 全部处理完成后移除执行任务组", group.getName());
                }
            }
        }).start();
    }

    /**
     * @return 任务信息索引值
     */
    private int getIndexByMode() {
        switch (this.mode) {
//            case 1:
//                return this.count.getAndIncrement() % this.taskGroups.size();
//            case 2:
//                return this.count.getAndIncrement() % this.taskGroups.size();
            default:
                return 0;
        }
    }
}
