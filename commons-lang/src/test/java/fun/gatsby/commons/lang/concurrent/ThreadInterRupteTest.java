package fun.gatsby.commons.lang.concurrent;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ThreadInterRupteTest extends TestCase {

    public static void main(String[] args) {

        class RWLock {
            boolean writing = false;
            Object writeLock = new Object();
            private AtomicInteger readingCount = new AtomicInteger();

            public synchronized void readLock() {
                for (; ; ) {
                    if (writing) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    int i = readingCount.addAndGet(1);
                    if (i == 0) {
                        writeLock.notifyAll();
                        break;
                    }

                }
            }

            public synchronized void readUnlock() {
                readingCount.addAndGet(-1);
                notifyAll();
            }

            public synchronized void writeLock() {
                for (; ; ) {
                    if (readingCount.get() == 0) {
                        synchronized (writeLock) {
                            try {
                                writing = true;
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    try {
                        writeLock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            }

            public void writeUnlock() {
                writeLock.notifyAll();

            }
        }

        RWLock rwLock = new RWLock();

        new Thread(() -> {
            rwLock.readLock();
            System.out.println("read lock");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            rwLock.readUnlock();
            System.out.println("read unlock");
        }).start();

        new Thread(() -> {
            rwLock.writeLock();
            System.out.println("write lock");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            rwLock.writeUnlock();
        }).start();


    }

    public void testInterrupt() {
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.info("thread is interrupted");
                return;
            }
            System.out.println("thread1 is running...");
        });
        t.start();
        t.interrupt();
        Thread.interrupted();
        log.info("go on after doing interrupt");
    }
}
