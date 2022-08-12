package fun.gatsby.commons.schedule;

//静态代理
public class TaskProxy implements Runnable {

    final Runnable task;
    final AbstractTaskGroup taskGroup;

    protected TaskProxy(Runnable task, AbstractTaskGroup taskGroup) {
        this.taskGroup = taskGroup;
        this.task = task;
    }

    @Override
    public void run() {
        if (taskGroup.cancelled) {
            return;
        }
        int count = taskGroup.taskCountAwaitingToFinish.decrementAndGet();
        if (count + 1 == taskGroup.size()) {
            taskGroup.beforeFirstStart();
        }
        try {
            task.run();
        } catch (Exception e) {
            taskGroup.onTaskException(e);
        } finally {
            if (count == 0 && !taskGroup.cancelled) {
                taskGroup.afterAllDone();
            }
        }
    }
}
