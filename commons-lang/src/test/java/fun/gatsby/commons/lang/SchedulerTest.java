package fun.gatsby.commons.lang;

import fun.gatsby.commons.schedule.Scheduler;
import fun.gatsby.commons.schedule.TaskGroup;
import junit.framework.TestCase;
import lombok.Data;

import java.util.List;
import java.util.stream.IntStream;

/**
 * @Date: 2022/3/9 14:52
 */
public class SchedulerTest extends TestCase {
    public void test() {
        int userSize = 10;
        int jobSize = 100;

        TaskGroup[] taskGroups = new TaskGroup[userSize];

        IntStream.range(0, userSize).parallel().forEach(i -> {
            TaskGroup taskGroup = new TaskGroup();
            taskGroups[i] = taskGroup;
            for (int j = 0; j < jobSize; j++) {
                MyPlan myPlan = new MyPlan();
                myPlan.setName("用户" + i + ",执行计划" + j);
                taskGroup.append(myPlan);
            }
        });

        Scheduler scheder = new Scheduler(4, 1, List.of(taskGroups));
        scheder.run();

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
            throw new RuntimeException("error");
        }

    }
}