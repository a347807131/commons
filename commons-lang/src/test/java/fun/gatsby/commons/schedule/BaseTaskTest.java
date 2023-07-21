package fun.gatsby.commons.schedule;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

@Slf4j
public class BaseTaskTest extends TestCase {


    public void testBefore() {
        TaskGroup<Runnable> taskGroup1 = genTasks("任务组1");
        TaskGroup<Runnable> taskGroup2 = genTasks("任务组2");
        TaskGroup<Runnable> taskGroup3 = genTasks("任务组3");
        ForkJoinPool pool3 = new ForkJoinPool(4);
    }

    static TaskGroup<Runnable> genTasks(String groupName) {
        int jobSize = 200;
        TaskGroup<Runnable> taskGroup = new TaskGroup<>();
        taskGroup.name = groupName;
        for (int j = 0; j < jobSize; j++) {
            PlanB planB = new PlanB();
            planB.name = "执行计划" + j;
            taskGroup.add(planB);
        }
        return taskGroup;
    }

    public void test2() throws ExecutionException, InterruptedException {
    }

    static class PlanB extends BaseTask {
        String name;

        @Override
        public void doWork() {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.debug("PlanB" + name + "执行完毕");
        }
    }
}
