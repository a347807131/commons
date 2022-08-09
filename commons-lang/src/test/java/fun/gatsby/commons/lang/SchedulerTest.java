package fun.gatsby.commons.lang;

import fun.gatsby.commons.lang.schedule.ITask;
import fun.gatsby.commons.lang.schedule.Scheduler;
import fun.gatsby.commons.lang.schedule.TaskGroup;
import junit.framework.TestCase;
import lombok.Data;

import java.util.LinkedList;
import java.util.stream.IntStream;

/**
 * @Author: dinghao
 * @Date: 2022/3/9 14:52
 */
public class SchedulerTest extends TestCase {
    public void test() {
        int userSize = 10;
        int jobSize = 100;

        TaskGroup[] taskGroups = new TaskGroup[userSize];

        IntStream.range(0, userSize).parallel().forEach(i -> {
            LinkedList<ITask> plans = new LinkedList<>();
            for (int j = 0; j < jobSize; j++) {
                MyPlan myPlan = new MyPlan();
                myPlan.setName("用户" + i + ",执行计划" + j);
                plans.add(myPlan);
            }
            taskGroups[i] = new TaskGroup(plans);
        });


        Scheduler scheder = new Scheduler(taskGroups, 4, 10, 100, 1, 2);
//        Scheduler scheder = new Scheduler(20, taskGroups);
        scheder.run();
    }

}

/**
 * @Author: dinghao
 * @Date: 2022/3/9 10:33
 */
@Data
class MyPlan implements ITask {
    private String name;

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + ":" + name);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //        throw nw RuntimeException("异常");
    }
}