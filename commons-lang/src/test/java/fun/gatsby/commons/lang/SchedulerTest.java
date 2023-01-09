package fun.gatsby.commons.lang;

import fun.gatsby.commons.schedule.Scheduler;
import fun.gatsby.commons.schedule.TaskGroup;
import junit.framework.TestCase;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * @Date: 2022/3/9 14:52
 */
public class SchedulerTest extends TestCase {
    public void testExecutor() {

        List<Runnable> tasks = genTasks();
        ExecutorService threadPool = Executors.newFixedThreadPool(4);
        tasks.forEach(threadPool::submit);
        threadPool.shutdown();
        boolean terminated;
        do {
            terminated = threadPool.isTerminated();
            Thread.yield();
        } while (!terminated);
    }

    public void testScheduler() {
        List<Runnable> tasks = genTasks();
        Scheduler.scheduleNow(4, tasks);
    }

    public void testParallelStream() {
        List<Runnable> tasks = genTasks();
        tasks.stream().parallel().forEach(Runnable::run);
    }

    List<Runnable> genTasks() {
        int userSize = 10;
        int jobSize = 100;

        LinkedList<Runnable> tasks = new LinkedList<>();
        IntStream.range(0, userSize).forEach(i -> {
            TaskGroup taskGroup = new MyTaskGroup();
            for (int j = 0; j < jobSize; j++) {
                MyPlan myPlan = new MyPlan();
                myPlan.setName("用户" + i + ",执行计划" + j);
                taskGroup.add(myPlan);
            }
            tasks.addAll(taskGroup);
        });
        return tasks;
    }
}
@Slf4j
class MyTaskGroup extends TaskGroup{
    @Override
    public synchronized void beforeFirstStart() {
        log.info("任务组{}开始前的流程执行",name);
    }

    @Override
    public void afterAllDone() {
        log.info("任务组{}结束后的流程执行",name);
    }
}

/**
 * @Date: 2022/3/9 10:33
 */
@Data
@Slf4j
class MyPlan implements Runnable {
    private String name;

    @Override
    public void run() {
        try {
            log.info("{}开始执行",name);
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (name.startsWith("用户9")) {
            throw new RuntimeException("error");
        }
    }
}