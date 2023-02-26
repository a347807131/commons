package fun.gatsby.commons.schedule;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.LinkedBlockingQueue;

public class MyTaskJoinPool extends ForkJoinPool {

    public MyTaskJoinPool(int parallelism) {
        super(parallelism);
    }

    LinkedBlockingQueue<Entry> entries = new LinkedBlockingQueue<>();

    public void addABatch(List<Runnable>... tasksArr) {
        for (int i = 0; i < tasksArr.length; i++) {
            List<Runnable> tasks = tasksArr[i];
            Entry entry = new Entry();
            entry.denpendOnLast = i != 0;
            entry.childrens.addAll(tasks);
            entries.add(entry);

        }
        for (List<Runnable> tasks : tasksArr) {
            Entry entry = new Entry();
            entry.denpendOnLast = true;
            entry.childrens.addAll(tasks);
            entries.add(entry);
        }

    }

    public void start() throws ExecutionException, InterruptedException {
        for (Entry entry : entries) {
            entry.state = TaskStateEnum.RUNNING;
            ForkJoinTask<?> forkJoinTask = runTaskGroup(entry.childrens);
            if (entry.denpendOnLast)
                forkJoinTask.get();
            entry.state = TaskStateEnum.FINISHED;
        }
    }

    ForkJoinTask<?> runTaskGroup(List<Runnable> tasks) throws ExecutionException, InterruptedException {
        return this.submit(() -> tasks.parallelStream().forEach(Runnable::run));
    }

    class Entry {
        boolean denpendOnLast = false;
        TaskStateEnum state = TaskStateEnum.WAITING;
        List<Runnable> childrens = new LinkedList<>();
    }

}
