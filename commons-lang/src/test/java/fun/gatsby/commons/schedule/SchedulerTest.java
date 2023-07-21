package fun.gatsby.commons.schedule;

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
@Slf4j
public class SchedulerTest extends TestCase {
    public void testExecutor() {

        List<Runnable> tasks = genTasks();
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
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
        Scheduler scheduler = Scheduler.schedule(3, tasks);
        scheduler.start();
        scheduler.await();

    }

    public void testParallelStream() {
        List<Runnable> tasks = genTasks();
        tasks.stream().parallel().forEach(Runnable::run);
    }

    public List<Runnable> genTasks() {
        int userSize = 10;
        int jobSize = 100;

        var taskGroupOfAll = new LinkedList<Runnable>();
        IntStream.range(0, userSize).forEach(i -> {
            TaskGroup<Runnable> taskGroup = new TaskGroup();
            taskGroup.setName("组" + i);
            for (int j = 0; j < jobSize; j++) {
                MyPlan myPlan = new MyPlan();
                myPlan.setName("用户" + i + ",执行计划" + j);
                taskGroup.add(myPlan);
            }
            taskGroupOfAll.addAll(taskGroup);
        });
        return taskGroupOfAll;
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
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (name.startsWith("用户9")) {
            log.warn("模拟出错");
            int i = 1 / 0;
        }

    }
}