package fun.gatsby.commons.schedule;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author gatsby
 */
@Slf4j
public class TaskGroup<T> extends AbstractTaskGroup<Runnable> {

    protected final ReentrantLock preAndPostTaskLock = new ReentrantLock();
    AtomicBoolean preTaskDone=new AtomicBoolean(false);

    AtomicInteger startedTaskCount=new AtomicInteger();
    AtomicInteger doneTaskCount=new AtomicInteger();

    protected String name;

    Runnable preTask = null;
    Runnable postTask = null;

    volatile TaskStateEnum state = TaskStateEnum.NEW;

    public TaskGroup(){
    }

    public TaskGroup(Collection<? extends Runnable> taskQueue) {
        super(taskQueue);
    }

    /**
     * 立即停止所有任务，剩余任务将不会执行原逻辑。
     */
    public void cancel() {
        state=TaskStateEnum.CANCELLED;
    }


    @Override
    protected Runnable wrapTask(Runnable task) {
        return new TaskProxy(task);
    }

    /**
     * 全部任务执行完后的回调函数，只会有一个线程进入，也只会运行一次
     */
    public void afterAllDone() {
        log.debug("name:{} 执行完成",name);
        if (postTask != null) {
            postTask.run();
        }
    }

    public void setPreAndPostTasks(Runnable pre,Runnable post){
        this.preTask=pre;
        this.postTask=post;
    }

    /**
     * //FIXED 可能的问题，因为不能保证方法结束前没有其他任务开始执行
     * 当任务组第一个的第一个任务开始执行时的函数，该函数执行完后其他任务才会开始执行<br/>
     * 只会有一个线程进入，也只会运行一次，后续不会再有线程进入
     */
    public synchronized void beforeFirstStart() {
        log.debug("name:{} 开始执行",name);
        if (preTask != null) {
            preTask.run();
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
            
            int startedCount = startedTaskCount.incrementAndGet();
            if(!preTaskDone.get() && startedCount!=1) {
                while (!preTaskDone.get()){
                    Thread.yield();
                }
            } else if(startedCount==1 && !preTaskDone.get()){
                beforeFirstStart();
                if(preAndPostTaskLock.isLocked()){
                    preAndPostTaskLock.unlock();
                }
                preTaskDone.compareAndSet(false,true);
            }
            try {
                task.run();
                doneTaskCount.incrementAndGet();
            } catch (Exception e) {
                onTaskException(task, e);
            } finally {
                if (doneTaskCount.get() == size() && TaskStateEnum.CANCELLED!=state) {
                    state = TaskStateEnum.FINISHED;
                    afterAllDone();
                }
            }
        }
    }
}