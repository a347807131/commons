package fun.gatsby.commons.lang.schedule;


import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author dinghao
 * @author gatsby
 * @Date: 2020/3/9 10:33
 */
@Data
public class TaskGroup {

    /**
     * 唯一标识
     */
    private int id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 执行计划队列
     */
    private final List<ITask> taskQueue = new LinkedList<>();

    public TaskGroup() {
        int code = UUID.randomUUID().hashCode();
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

    public boolean addTask(ITask task) {
        return taskQueue.add(task);
    }
}

