package fun.gatsby.commons.schedule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractTaskGroup extends LinkedList<Runnable> {

    protected volatile boolean cancelled = false;
    /**
     * 剩余未完成的任务的数量
     */
    protected AtomicInteger taskCountAwaitingToFinish = new AtomicInteger(0);
    /**
     * 唯一标识
     */
    int id;
    /**
     * 任务组名称
     */
    String name;

    public AbstractTaskGroup() {
        int code = UUID.randomUUID().hashCode();
        this.id = code < 0 ? -code : code;
        this.name = "task-group-" + id;
    }

    public AbstractTaskGroup(int id, String name, List<Runnable> taskQueue) {
        this.id = id;
        this.name = name;
        this.addAll(taskQueue);
        this.taskCountAwaitingToFinish.addAndGet(taskQueue.size());
    }

    public AbstractTaskGroup(List<Runnable> taskQueue) {
        this();
        this.addAll(taskQueue);
        this.taskCountAwaitingToFinish.addAndGet(taskQueue.size());
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean add(Runnable task) {
        var taskWrapper = this.wrapTask(task);
        this.taskCountAwaitingToFinish.addAndGet(1);
        return super.add(taskWrapper);
    }

    @Override
    public boolean addAll(Collection<? extends Runnable> tasks) {
        var list = new LinkedList<Runnable>();
        for (Runnable task : tasks) {
            list.add(this.wrapTask(task));
        }
        return super.addAll(list);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Runnable> tasks) {
        var list = new LinkedList<Runnable>();
        for (Runnable task : tasks) {
            list.add(this.wrapTask(task));
        }
        return super.addAll(index, list);
    }

    @Override
    public void add(int index, Runnable element) {
        super.add(index, this.wrapTask(element));
    }

    @Override
    public void addFirst(Runnable runnable) {
        super.addFirst(this.wrapTask(runnable));
    }

    @Override
    public void addLast(Runnable runnable) {
        super.addLast(this.wrapTask(runnable));
    }

    /**
     * 获取task的包装类，默认不做任何操作
     *
     * @param task 源任务
     * @return 包装类实例
     */
    private Runnable wrapTask(Runnable task) {
        return new TaskProxy(task, this);
    }

    /**
     * 立即停止所有任务，剩余任务将不会执行原逻辑。
     */
    public void cancel() {
        cancelled = true;
    }

    /**
     * 全部任务执行完后的回调函数，只会有一个线程进入，也只会运行一次
     */
    protected void afterAllDone() {
    }

    /**
     * 任务组中子任务出现异常时的回调函数，存在会有多个线程进入的情况
     */
    protected void onTaskException(Exception e) {
    }

    /**
     * 当任务组第一个的第一个任务开始执行时的函数，该函数执行完后其他任务才会开始执行<br/>
     * 只会有一个线程进入，也只会运行一次，后续不会再有线程进入
     */
    protected synchronized void beforeFirstStart() {
    }
}