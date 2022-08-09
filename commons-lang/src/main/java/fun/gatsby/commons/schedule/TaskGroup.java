package fun.gatsby.commons.schedule;


import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * @author dinghao
 * @author gatsby
 * @Date: 2020/3/9 10:33
 */
//TODO 多种模式实现，
// 1,出现失败任务后直接中断后续任务，包括已在队列中的任务，并执行补偿函数。
// 2,出现失败后，跳过继续。
@Data
public class TaskGroup {

    /**
     * 唯一标识
     */
    private int id;

    /**
     * 执行计划队列
     */
    private final List<Runnable> taskQueue = new LinkedList<>();
    /**
     * 任务组名称
     */
    private String name;
    /**
     * 任务组模式
     */
    private int mode;

    public TaskGroup() {
        int code = hashCode();
        this.id = code < 0 ? -code : code;
        this.name = "task group - " + id;
    }

    public TaskGroup(int id, String name, List<ITask> taskQueue) {
        this.id = id;
        this.name = name;
        this.taskQueue.addAll(taskQueue);
    }

    public TaskGroup(List<ITask> taskQueue) {
        this();
        this.taskQueue.addAll(taskQueue);
    }

    public static TaskGroup of(ITask... tasks) {
        return new TaskGroup(List.of(tasks));
    }

    public boolean addTask(Runnable task) {
        return taskQueue.add(task);
    }

    public synchronized int onTaskException(Runnable task) {
        return 1;
    }
}

