package fun.gatsby.commons.lang;

public class NotifyAndWaitTest {
    public static void main(String[] args) {
        var waitThread = new WaitThread(0);
        waitThread.start();
        System.out.println("waitThread   状态1:   线程ID=" + waitThread.getId() + "     线程状态：" + waitThread.getState());
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("waitThread   状态2:   线程ID=" + waitThread.getId() + "     线程状态：" + waitThread.getState());
        synchronized (NotifyAndWaitTest.class) {
            System.out.println("notify before 状态:   线程ID=" + waitThread.getId() + "     线程状态：" + waitThread.getState());
            NotifyAndWaitTest.class.notify();
            System.out.println("notify after  状态:   线程ID=" + waitThread.getId() + "     线程状态：" + waitThread.getState());
        }
    }

    static class WaitThread extends Thread {
        long timeOut;

        public WaitThread(long timeOut) {
            this.timeOut = timeOut;
        }

        @Override
        public void run() {
            synchronized (NotifyAndWaitTest.class) {
                System.out.println("wait before   状态:   线程ID=" + Thread.currentThread().getId() + "     线程状态：" + Thread.currentThread().getState());
                try {
                    NotifyAndWaitTest.class.wait(timeOut);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("wait after    状态:   线程ID=" + Thread.currentThread().getId() + "     线程状态：" + Thread.currentThread().getState());
            }
        }
    }

}
