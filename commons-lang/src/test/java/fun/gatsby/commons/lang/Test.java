package fun.gatsby.commons.lang;

import fun.gatsby.commons.lang.schedule.ITask;
import fun.gatsby.commons.lang.schedule.Scheduler;
import fun.gatsby.commons.lang.schedule.TaskInfo;
import junit.framework.TestCase;
import lombok.Data;

import java.util.LinkedList;
import java.util.stream.IntStream;

/**
 * @Author: dinghao
 * @Date: 2022/3/9 14:52
 */
public class Test extends TestCase {
    public void test() {
        int userSize = 100;
        int jobSize = 1000;

        TaskInfo[] taskInfos = new TaskInfo[userSize];

        IntStream.range(0, userSize).parallel().forEach(i -> {
            LinkedList<ITask> plans = new LinkedList<>();
            for (int j = 0; j < jobSize; j++) {
                MyPlan myPlan = new MyPlan();
                myPlan.setName("用户" + i + ",执行计划" + j);
                plans.add(myPlan);
            }
            taskInfos[i] = new TaskInfo(i, "用户" + i, plans);
        });


        Scheduler scheder = new Scheduler(taskInfos, 3, 10, 100, 3, 2);
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
        //        throw new RuntimeException("异常");
    }

    @Override
    public void onError(Throwable e) {
        System.out.println(Thread.currentThread().getName() + ":" + name + "出错");
    }
}