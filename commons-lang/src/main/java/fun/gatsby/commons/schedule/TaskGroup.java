package fun.gatsby.commons.schedule;


import cn.hutool.core.lang.func.Func0;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dinghao
 * @author gatsby
 * @Date: 2020/3/9 10:33
 */
//TODO 多种模式实现，
// 1,出现失败任务后直接中断后续任务，包括已在队列中的任务，并执行补偿函数。
// 2,出现失败后，跳过继续。
@Data
@Slf4j
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
     * 1,出现失败任务后直接中断后续任务，并执行补偿函数。包括已在队列中的任务
     * 2,出现失败后，跳过继续。
     */
    private int mod = 1;

    boolean cancelled = false;

    private Func0 callback;

    AtomicInteger countToFinish = new AtomicInteger(0);


    public TaskGroup() {
        int code = UUID.randomUUID().hashCode();
        this.id = code < 0 ? -code : code;
        this.name = "task group - " + id;
    }

    public TaskGroup(int id, String name, List<ITask> taskQueue) {
        this.id = id;
        this.name = name;
        this.taskQueue.addAll(taskQueue);
        this.countToFinish.addAndGet(taskQueue.size());
    }

    public TaskGroup(List<ITask> taskQueue) {
        this();
        this.taskQueue.addAll(taskQueue);
        this.countToFinish.addAndGet(taskQueue.size());
    }

    public static TaskGroup of(ITask... tasks) {
        return new TaskGroup(List.of(tasks));
    }

    public boolean append(Runnable task) {
        this.countToFinish.addAndGet(1);
        return taskQueue.add(task);
    }

    public Runnable pollFirst() {
        return this.taskQueue.remove(0);
    }

    public synchronized int onTaskException(Runnable task) {
        if (mod == 1) {
            int size = taskQueue.size();
            taskQueue.clear();
            return size == 0 ? 1 : size;
        } else {
            return 1;
        }
    }

    public void callback() {
        System.out.println("group:" + name + " callback");
        callback.callWithRuntimeException();
    }

    public void onAllDone() {
        System.out.println("group:" + name + " all done");

    }

    public void runTask(Runnable finalTask) {
        int sizeToDecrease = 1;
        try {
            if (isCancelled()) {
                return;
            }
            finalTask.run();
        } catch (Exception e) {
            log.error("执行任务出错！{}", id, e);
            sizeToDecrease = onTaskException(finalTask);
        } finally {
            // 当任务执行完毕，将任务数减1˛
            countToFinish.addAndGet(-sizeToDecrease);
            if (getCountToFinish().get() == 0) {
                onAllDone();
            }
        }


    }
//静态代理？
//    public class Aop {
//
//    }
}

