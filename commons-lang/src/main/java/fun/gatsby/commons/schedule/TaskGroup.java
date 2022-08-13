package fun.gatsby.commons.schedule;

import lombok.extern.slf4j.Slf4j;


/**
 * @author gatsby
 */
@Slf4j
public class TaskGroup extends AbstractTaskGroup {

    protected volatile boolean cancelled = false;

    /**
     * 立即停止所有任务，剩余任务将不会执行原逻辑。
     */
    public void cancel() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * 全部任务执行完后的回调函数，只会有一个线程进入，也只会运行一次
     */
    public void afterAllDone() {
        log.info("afterAllDone");
    }

    /**
     * 任务组中子任务出现异常时的回调函数，存在会有多个线程进入的情况
     */
    public void onTaskException(Exception e) {
        log.info("onTaskException", e);
    }

    /**
     * 当任务组第一个的第一个任务开始执行时的函数，该函数执行完后其他任务才会开始执行<br/>
     * 只会有一个线程进入，也只会运行一次，后续不会再有线程进入
     */
    public synchronized void beforeFirstStart() {
        log.info("beforeFirstStart");
    }

    @Override
    protected Runnable wrapTask(Runnable task) {
        return new TaskProxy(task);
    }

    //静态代理
    class TaskProxy implements Runnable {

        final Runnable task;

        protected TaskProxy(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            if (cancelled) {
                return;
            }
            int count = taskCountAwaitingToFinish.decrementAndGet();
            if (count + 1 == size()) {
                beforeFirstStart();
            }
            try {
                task.run();
            } catch (Exception e) {
                onTaskException(e);
            } finally {
                if (count == 0 && !cancelled) {
                    afterAllDone();
                }
            }
        }
    }
}