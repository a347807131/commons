package fun.gatsby.commons.schedule;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author gatsby
 */
@Slf4j
public class TaskGroup<T> extends AbstractTaskGroup<Runnable> {

    protected final ReentrantLock preAndPostTaskLock = new ReentrantLock();
    Condition preTaskDoneCondition = preAndPostTaskLock.newCondition();
    Boolean preTaskDone=false;

    AtomicInteger startedTaskCount=new AtomicInteger();
    AtomicInteger doneTaskCount=new AtomicInteger();

    protected String name;

    Runnable taskBeforeFirstStart = null;

    Runnable taskAfterAllDone = null;

    volatile TaskStateEnum state = TaskStateEnum.NEW;

    boolean denpendOnLast = false;

    public TaskGroup() {
        int code = UUID.randomUUID().hashCode();
        this.name = "task-group-" + code;
    }

    public TaskGroup(int id, String name, Collection<? extends Runnable> tasks) {
        super(tasks);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 立即停止所有任务，剩余任务将不会执行原逻辑。
     */
    public void cancel() {
        state=TaskStateEnum.CANCELLED;
    }

    public void setTaskBeforeFirstStart(Runnable taskBeforeFirstStart) {
        this.taskBeforeFirstStart = taskBeforeFirstStart;
    }

    public void setTaskAfterAllDone(Runnable taskAfterAllDone) {
        this.taskAfterAllDone = taskAfterAllDone;
    }

    @Override
    protected Runnable wrapTask(Runnable task) {
        return new TaskProxy(task);
    }

    /**
     * 全部任务执行完后的回调函数，只会有一个线程进入，也只会运行一次
     */
    public void afterAllDone() {
        if (taskAfterAllDone != null) {
            taskAfterAllDone.run();
        }
    }

    /**
     * //FIXED 可能的问题，因为不能保证方法结束前没有其他任务开始执行
     * 当任务组第一个的第一个任务开始执行时的函数，该函数执行完后其他任务才会开始执行<br/>
     * 只会有一个线程进入，也只会运行一次，后续不会再有线程进入
     */
    public synchronized void beforeFirstStart() {
        if (taskBeforeFirstStart != null) {
            taskBeforeFirstStart.run();
        }
    }

    /**
     * 任务组中子任务出现异常时的回调函数，存在会有多个线程进入的情况
     */
    public void onTaskException(Runnable task, Exception e) {
    }

    //静态代理
    class TaskProxy implements Runnable {

        final Runnable task;

        protected TaskProxy(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            if (TaskStateEnum.CANCELLED==state) {
                return;
            }
            
            int doneCount = doneTaskCount.get();
            if(!preTaskDone && doneCount!=0) {
                try {
                    preTaskDoneCondition.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else if(startedCount==0 && !preTaskDone){
                beforeFirstStart();
                preTaskDone=true;
                // FIXME: 2023/7/24
                preTaskDoneCondition.signalAll();
            }
            try {
                task.run();
                doneTaskCount
            } catch (Exception e) {
                onTaskException(task, e);
            } finally {
                int doneCount = doneTaskCount.incrementAndGet();
                if (doneCount == size() && TaskStateEnum.CANCELLED!=state) {
                    state = TaskStateEnum.FINISHED;
                    afterAllDone();
                }
            }
        }
    }
}