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

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        MyTaskJoinPool taskJoinPool = new MyTaskJoinPool(10);
//        Thread thread = new Thread(() -> {
//            for (int i = 0; i < 10; i++) {
//                TaskGroup<Runnable> taskGroup = genTasks("任务组" + i);
//                taskJoinPool.addABatch(taskGroup);
//                System.out.println("任务组" + i + "添加完毕");
//            }
//        });
//        thread.start();
        // FIXME: 2023/2/27 存在运行时添加任务，任务不执行的问题
        for (int i = 0; i < 5; i++) {
            TaskGroup<Runnable> taskGroup = genTasks("任务组" + i);
            taskJoinPool.addABatch(taskGroup);
        }
        taskJoinPool.start();
    }

    static TaskGroup genTasks(String groupName) {
        int jobSize = 2;
        TaskGroup taskGroup = new TaskGroup();
        taskGroup.name = groupName;
        taskGroup.taskAfterAllDone = () -> log.debug("任务组{}执行完毕", groupName);
        taskGroup.taskBeforeFirstStart = () -> log.debug("任务组{}开始执行", groupName);
        for (int j = 0; j < jobSize; j++) {
            PlanB planB = new PlanB();
            planB.name = "执行计划" + j;
            taskGroup.add(planB);
        }
        return taskGroup;
    }

    public void test2() throws ExecutionException, InterruptedException {
        MyTaskJoinPool taskJoinPool = new MyTaskJoinPool(10);
        Thread thread = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                TaskGroup<Runnable> taskGroup = genTasks("任务组" + i);
                taskJoinPool.addABatch(taskGroup);
            }
        });
        thread.start();
//        for (int i = 0; i < 10; i++) {
//            TaskGroup<Runnable> taskGroup = genTasks("任务组" + i);
//            taskJoinPool.addABatch(taskGroup);
//        }
        taskJoinPool.start();
        thread.join();
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
//           log.debug("PlanB"+name+"执行完毕");
        }
    }
}
