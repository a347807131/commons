package fun.gatsby.commons.schedule;


import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * @author Gatsby
 * @see <img src="https://img-blog.csdnimg.cn/883009304fb942b2b20a14d14d85c9fd.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAaGFvaGFvX2Rpbmc=,size_20,color_FFFFFF,t_70,g_se,x_16">
 */

//TODO 将taskGroupList改造为collection
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
     * 还在队列外等待执行的任务
     */
    private final List<Runnable> tasks;


    /**
     * 队列的容量
     */
    int queueSize;

    private volatile boolean cancelled = false;

    /**
     * 构造器
     *
     * @param nThrends  同时执行任务中的任务线程数
     * @param queueSize 任务执行队列
     * @param mode      执行模式 1：所有任务信息都执行 2：先执行部分任务，执行完后再执行其他任务
     */
    public Scheduler(int nThrends, int queueSize, int mode, List<Runnable> tasks) {
        this.tasks = new CopyOnWriteArrayList<>(tasks);
        this.nThrends = nThrends;
        this.queueSize = queueSize;
        this.loopExecutor = Executors.newFixedThreadPool(this.nThrends);
        this.blockingTaskQueue = new LinkedBlockingQueue<>(queueSize);
    }

    public Scheduler(int nThrends, int mode, List<Runnable> tasks) {
        this(nThrends, 2 << 7, mode, tasks);
    }

    public Scheduler(int nThrends, List<Runnable> tasks) {
        this(nThrends, 1, tasks);
    }

    public Scheduler(int nThrends, Runnable... runnables) {
        this(nThrends, 1, List.of(runnables));
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
            if (this.tasks.size() == 0) {
                if (this.blockingTaskQueue.size() == 0) {
                    return;
                }
            }
            // 获取一个执行任务,并执行
            try {
                var task = this.blockingTaskQueue.take();
                this.loopExecutor.execute(task);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 开启一个线程，持续向执行任务队列添加执行任务，直到所有的任务任务添加完
     */
    private void putTaskToQueueAsync() {
        new Thread(() -> {
            while (!cancelled) {
                // 任务信息数组数量
                int length = this.tasks.size();
                // 执行完结束线程
                if (length == 0) {
                    return;
                }
                // 获取添加执行任务的的任务索引值
                try {
                    this.blockingTaskQueue.put(tasks.remove(0));
                } catch (InterruptedException e) {
                    log.error("向执行任务队列放入任务异常", e);
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
