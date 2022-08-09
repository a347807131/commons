package fun.gatsby.commons.lang.schedule;


import lombok.Data;

import java.util.LinkedList;
import java.util.UUID;

/**
 * @Author: dinghao
 * @Date: 2022/3/7 15:31
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
    private LinkedList<ITask> taskQueue;


    public TaskGroup(int id, String name, LinkedList<ITask> taskQueue) {
        this.id = id;
        this.name = name;
        this.taskQueue = taskQueue;
    }

    public TaskGroup(LinkedList<ITask> taskQueue) {
        int code = UUID.randomUUID().hashCode();
        this.id = code < 0 ? -code : code;
        this.name = "任务组" + id;
        this.taskQueue = taskQueue;
    }
}

