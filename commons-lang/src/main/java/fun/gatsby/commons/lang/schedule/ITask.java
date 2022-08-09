package fun.gatsby.commons.lang.schedule;


import java.util.concurrent.atomic.AtomicInteger;

public interface ITask extends Runnable {

    /**
     * 线程池执行前
     */
    default void before() {
    }

    /**
     * 线程池执行后
     */
    default void after() {
    }

    default void onError(Throwable e) {
    }

    default void doWork(AtomicInteger atomicInteger) {
        try {
            before();
            run();
        } catch (Throwable e) {
            onError(e);
        } finally {
            after();
            atomicInteger.decrementAndGet();
        }
    }
}

