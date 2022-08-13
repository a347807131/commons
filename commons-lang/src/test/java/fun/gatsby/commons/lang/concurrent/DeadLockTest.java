package fun.gatsby.commons.lang.concurrent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeadLockTest {
    public static void main(String[] args) {

        Object o1 = new Object();
        Object o2 = new Object();
        new Thread(() -> {
            synchronized (o1) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                synchronized (o2) {
                    log.info("线程1：{} 执行中", Thread.currentThread().getName());
                }
            }
        }).start();
        new Thread(() -> {
            synchronized (o2) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                synchronized (o1) {
                    log.info("线程2：{} 执行中", Thread.currentThread().getName());
                }
            }
        }).start();
    }
}
