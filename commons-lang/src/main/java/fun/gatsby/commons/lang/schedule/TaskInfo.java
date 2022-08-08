package fun.gatsby.commons.lang.schedule;


import lombok.Data;

import java.util.LinkedList;
import java.util.UUID;

/**
 * @Author: dinghao
 * @Date: 2022/3/7 15:31
 */
@Data
public class TaskInfo {

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


    public TaskInfo(int id, String name, LinkedList<ITask> taskQueue) {
        this.id = id;
        this.name = name;
        this.taskQueue = taskQueue;
    }

    public TaskInfo(LinkedList<ITask> taskQueue) {
        this.id = UUID.randomUUID().hashCode();
        this.taskQueue = taskQueue;
    }
}

