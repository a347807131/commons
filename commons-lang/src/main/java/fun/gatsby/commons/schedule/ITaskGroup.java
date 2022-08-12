package fun.gatsby.commons.schedule;

import java.util.concurrent.atomic.AtomicInteger;

public interface ITaskGroup {
    /**
     * 全部任务执行完后的回调函数，只会有一个线程进入，也只会运行一次
     */
    void afterAllDone();

    /**
     * 任务组中子任务出现异常时的回调函数，存在会有多个线程进入的情况
     */
    void onTaskException(Exception e);

    /**
     * 当任务组第一个的第一个任务开始执行时的函数，该函数执行完后其他任务才会开始执行<br/>
     * 只会有一个线程进入，也只会运行一次，后续不会再有线程进入
     */
    void beforeFirstStart();

    boolean isCancelled();

    int size();

    AtomicInteger getTaskCountAwaitingToFinish();

    //静态代理
    class TaskProxy implements Runnable {

        final Runnable task;
        final ITaskGroup taskGroup;

        protected TaskProxy(Runnable task, ITaskGroup taskGroup) {
            this.taskGroup = taskGroup;
            this.task = task;
        }

        @Override
        public void run() {
            if (taskGroup.isCancelled()) {
                return;
            }
            int count = taskGroup.getTaskCountAwaitingToFinish().decrementAndGet();
            if (count + 1 == taskGroup.size()) {
                taskGroup.beforeFirstStart();
            }
            try {
                task.run();
            } catch (Exception e) {
                taskGroup.onTaskException(e);
            } finally {
                if (count == 0 && !taskGroup.isCancelled()) {
                    taskGroup.afterAllDone();
                }
            }
        }
    }
}