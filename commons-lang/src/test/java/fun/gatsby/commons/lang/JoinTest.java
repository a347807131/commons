package fun.gatsby.commons.lang;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * join方法是Thread类中的一个方法，该方法的定义是等待该线程执行直到终止。
 * 其实就说join方法将挂起调用线程的执行，直到被调用的对象完成它的执行。
 */
public class JoinTest extends TestCase {
    /**
     * @author wcc
     * @date 2021/8/21 20:46
     * 现在有T1、T2、T3三个线程，你怎样保证T2在T1执行完后执行，T3在T2执行完后执行？
     */
    public void test() {
        //初始化线程1，由于后续有匿名内部类调用这个局部变量，需要用final修饰
        //这里不用final修饰也不会报错的原因 是因为jdk1.8对其进行了优化
    /*
    在局部变量没有重新赋值的情况下，它默认局部变量为final类型，认为你只是忘记了加final声明了而已。
    如果你重新给局部变量改变了值或者引用，那就无法默认为final了
     */
        Thread t1 = new Thread(() -> System.out.println("t1 is running..."));

        //初始化线程二
        Thread t2 = new Thread(() -> {
            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("t2 is running...");
            }
        });

        //初始化线程三
        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    t2.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("t3 is running...");
                }
            }
        });

        t1.start();
        t2.start();
        t3.start();
    }

    @Test
    public void test2() throws InterruptedException {
        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("thread 1");
            }
        });
        Thread thread2 = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("thread 2");
            }
        });
        Thread thread3 = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("thread 3");
            }
        });

        thread1.start();
        thread1.join();
        thread2.start();
        thread2.join();
        thread3.start();
        thread3.join();

        System.out.println("thread Main");
    }

}
