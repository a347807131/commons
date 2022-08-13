package fun.gatsby.commons.lang;

public class WaitTest extends Thread {

    private final Object self;

    private final Object last;

    public WaitTest(String name, Object self, Object last) {
        super(name);
        this.self = self;
        this.last = last;
    }

    public static void main(String[] args) {
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();

        WaitTest waitA = new WaitTest("A", a, b);
        WaitTest waitB = new WaitTest("B", b, c);
        WaitTest waitC = new WaitTest("C", c, a);

        waitA.start();
        waitB.start();
        waitC.start();
    }

    public void run() {
        for (int i = 0; i < 10; i++) {
            // 锁住下一个对象
            synchronized (last) {
                // 锁住当前对象
                synchronized (self) {
                    if (super.getName().equals("A")) {
                        System.out.println("第 " + (i + 1) + " 次运行！");
                    }

                    System.out.println(super.getName());

                    // 等待一轮循环结束后唤醒当前线程
                    self.notify();
                }

                try {
                    // 释放下一个线程的对象锁
                    last.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
