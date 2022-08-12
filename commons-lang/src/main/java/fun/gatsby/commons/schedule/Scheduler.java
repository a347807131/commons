package fun.gatsby.commons.schedule;


import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author Gatsby
 * @Date: 2022-8-13 23:41
 * @Description: 多线程任务调度器
 */

@Slf4j
public class Scheduler {

    /**
     * 允许并行执行的线程池
     */
    private final ExecutorService executor;
    /**
     * 还在队列外等待执行的任务
     */
    private final LinkedList<Runnable> tasks;

    public Scheduler(int nThrends, Collection<Runnable> tasks) {
        this.tasks = new LinkedList<>(tasks);
        this.executor = Executors.newFixedThreadPool(nThrends);
    }

    public static Scheduler schedule(int nThrends, Runnable... tasks) {
        return new Scheduler(nThrends, Arrays.asList(tasks));
    }

    public static Scheduler schedule(int nThrends, Collection<Runnable> tasks) {
        return new Scheduler(nThrends, tasks);
    }

    public static void scheduleNow(int nThrends, Collection<Runnable> tasks) {
        schedule(nThrends, tasks).run();
    }

    /**
     * 立即取消所有任务
     */
    public void shutDownNow() {
        this.executor.shutdownNow();
    }

    /**
     * 执行方法
     */
    public void run() {
        for (Runnable task : tasks) {
            executor.execute(task);
        }
        //停止线程池,在队列中的任务全部执行完毕后，才会停止线程池，该方法不会阻塞
        if (!executor.isShutdown())
            executor.shutdown();
        blockIfNotTerminated();
    }

    private void blockIfNotTerminated() {
        for (; ; ) {
            //只有当线程池中所有线程完成任务时才会返回true，并且需要先调用线程池的shutdown方法或者shutdownNow方法。
            boolean terminated = executor.isTerminated();
            if (terminated)
                break;
            else
                Thread.yield();
        }
    }
}
