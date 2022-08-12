package fun.gatsby.commons.lang;

import fun.gatsby.commons.schedule.Scheduler;
import fun.gatsby.commons.schedule.TaskGroup;
import junit.framework.TestCase;
import lombok.Data;

import java.util.LinkedList;
import java.util.stream.IntStream;

/**
 * @Date: 2022/3/9 14:52
 */
public class SchedulerTest extends TestCase {
    public void test() {
        for (int a = 0; a < 10; a++) {

            int userSize = 10;
            int jobSize = 100;

            LinkedList<Runnable> tasks = new LinkedList<>();
            IntStream.range(0, userSize).forEach(i -> {
                TaskGroup taskGroup = new TaskGroup();
                for (int j = 0; j < jobSize; j++) {
                    MyPlan myPlan = new MyPlan();
                    myPlan.setName("用户" + i + ",执行计划" + j);
                    taskGroup.add(myPlan);
                }
                tasks.addAll(taskGroup.getTaskQueue());
            });
            Scheduler scheder = new Scheduler(4, tasks);
            scheder.run();
        }
    }
}

/**
 * @Date: 2022/3/9 10:33
 */
@Data
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
//            throw new RuntimeException("error");
        }

    }
}