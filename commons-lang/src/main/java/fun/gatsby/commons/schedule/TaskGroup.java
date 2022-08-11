package fun.gatsby.commons.schedule;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author gatsby
 */
@Data
@Slf4j
public class TaskGroup {

    /**
     * 唯一标识
     */
    private int id;

    /**
     * 增强任务队列
     */

    private final List<Runnable> taskQueue = new LinkedList<>();
    /**
     * 任务组名称
     */
    private String name;
    /**
     * 任务组模式
     * 1,出现失败任务后直接中断后续任务，并执行补偿函数。包括已在队列中的任务
     * 2,出现失败后，跳过继续。
     */
    private int mod = 1;

    boolean cancelled = false;

    AtomicInteger countRunnableoFinish = new AtomicInteger(0);

    public TaskGroup() {
        int code = UUID.randomUUID().hashCode();
        this.id = code < 0 ? -code : code;
        this.name = "task group - " + id;
    }

    public TaskGroup(int id, String name, List<Runnable> taskQueue) {
        this.id = id;
        this.name = name;
        this.addAll(taskQueue);
        this.countRunnableoFinish.addAndGet(taskQueue.size());
    }

    public TaskGroup(List<Runnable> taskQueue) {
        this();
        this.addAll(taskQueue);
        this.countRunnableoFinish.addAndGet(taskQueue.size());
    }

    public static TaskGroup of(Runnable... tasks) {
        return new TaskGroup(List.of(tasks));
    }

    public boolean add(Runnable task) {
        var taskWrapper = this.new WrapperedTask(task);
        this.countRunnableoFinish.addAndGet(1);
        return taskQueue.add(taskWrapper);
    }

    protected void addAll(Collection<Runnable> tasks) {
        for (Runnable task : tasks) {
            add(this.new WrapperedTask(task));
        }
    }

    public List<Runnable> getTaskQueue() {
        return new LinkedList<>(taskQueue);
    }

    protected void onAllDone() {
        System.out.println("group:" + name + " all done");

    }

    protected int onTaskException() {
        if (mod == 1) {
            cancelled = true;
            int size = taskQueue.size();
            taskQueue.clear();
            return size == 0 ? 1 : size;
        } else {
            return 1;
        }
    }

    //静态代理
    protected class WrapperedTask implements Runnable {

        private final Runnable taskIn;

        protected WrapperedTask(Runnable runnable) {
            this.taskIn = runnable;
        }

        @Override
        public void run() {
            int sizeRunnableoDecrease = 1;
            try {
                if (cancelled) {
                    return;
                }
                taskIn.run();
            } catch (Exception e) {
                log.error("执行任务出错！{}", id, e);
                sizeRunnableoDecrease = onTaskException();
            } finally {
                // 当任务执行完毕，将任务数减1
                countRunnableoFinish.addAndGet(-sizeRunnableoDecrease);
                if (getCountRunnableoFinish().get() == 0) {
                    onAllDone();
                }
            }
        }
    }
}