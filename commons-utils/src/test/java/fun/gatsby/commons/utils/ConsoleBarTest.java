package fun.gatsby.commons.utils;

import fun.gatsby.commons.schedule.TaskGroup;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ConsoleBarTest {

    public static void main(String[] args) throws InterruptedException {
        ConsoleProgressBar bar = new ConsoleProgressBar();
        bar.showCurrent();
        for (int i = 0; i < 100; i++) {
            Thread.sleep(100);
            bar.iterate();
        }
    }
    @Test
    public void testParallelStream() {
        List<Runnable> tasks = genTasks();
        tasks.stream().parallel().forEach(Runnable::run);
    }

    List<Runnable> genTasks() {
        int userSize = 10;
        int jobSize = 100;

        ConsoleProgressBar bar = new ConsoleProgressBar(10 * 100);
        var taskGroupOfAll = new LinkedList<Runnable>();
        for (int i = 0; i < userSize; i++) {
            TaskGroup taskGroup = new TaskGroup();
            taskGroup.setTaskAfterAllDone(() -> {
                log.info(taskGroup.getName() + "done");
            });
            for (int j = 0; j < jobSize; j++) {
                MyPlan myPlan = new MyPlan();
                myPlan.setBar(bar);
                myPlan.setName("用户" + i + ",执行计划" + j);
                taskGroup.add(myPlan);
            }
        }
        return taskGroupOfAll;
    }
    /**
     * @Date: 2022/3/9 10:33
     */
    @Data
    @Slf4j
    static class MyPlan implements Runnable {
        private String name;
        public ConsoleProgressBar bar;

        @Override
        public void run() {
            try {
                Thread.sleep(10);
                bar.iterate();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (name.startsWith("用户9")) {
                throw new RuntimeException("error");
            }

        }
    }
}

//@Slf4j
 class ConsoleProgressBar {

    private static final DecimalFormat floatPercentFormater = new DecimalFormat("0.00%");
    private static final DecimalFormat floatFormater = new DecimalFormat("0.00");
    private final AtomicInteger currentValue = new AtomicInteger(0);
    private volatile long currentSpeed = 1024 * 1000;
    char progressChar = '█';
    char waitChar = '#';
    private final AtomicInteger total = new AtomicInteger(100);

    private int barLen = 50;

    private long startTime;

    public ConsoleProgressBar() {
    }

    public ConsoleProgressBar(int total) {
        this.total .set(total);
    }

    public synchronized void iterate() {
        int value = this.currentValue.addAndGet(1);
        if (value == 1) {
            this.startTime = System.currentTimeMillis();
        }
        show(value);
    }

    public void showCurrent() {
        show(this.currentValue.get());
    }


    int last=0;
    synchronized void show(int value) {
        last=value;
        int totalV=total.get();
        System.out.print('\r');
        System.out.print(ColorEnum.RED.value);
        // 比例
        float rate = value * 1f / totalV;

        int len = (int) (rate * barLen);
        StringBuilder sb = new StringBuilder("Progress: ");
        for (int i = 0; i < len; i++) {
            sb.append(progressChar);
        }
        for (int i = 0; i < barLen - len; i++) {
            sb.append(waitChar);
        }

        float secondsTotalSpent = value == 0 ? 0 : (System.currentTimeMillis() - startTime) / 1000f;
        float speed = value == 0 ? 0 : secondsTotalSpent / value;
        int secondsLeft = (int) ((totalV - value) * speed);

        sb.append(" |").append(floatPercentFormater.format(rate));
        sb.append(" |").append(floatFormater.format(speed)).append(" avg spi");
        sb.append(" |").append( genHMS(secondsLeft));
        sb.append(" |").append(totalV - value).append(" units left");
        System.out.println(sb);
    }

    String genHMS(long second) {

        String str = "00:00:00";
        if (second < 0) {
            return str;
        }

        // 得到小时
        long h = second / 3600;
        str = h > 0 ? ((h < 10 ? ("0" + h) : h) + ":") : "00:";

        // 得到分钟
        long m = (second % 3600) / 60;
        str += (m < 10 ? ("0" + m) : m) + ":";

        //得到剩余秒
        long s = second % 60;
        str += (s < 10 ? ("0" + s) : s);
        return str;
    }

    /**
     * 颜色枚举
     */
    public enum ColorEnum {

        /**
         * 白色
         */
        WHITE("\33[0m"),

        /**
         * 红色
         */
        RED("\33[1m\33[31m"),

        /**
         * 绿色
         */
        GREEN("\33[1m\33[32m"),

        /**
         * 黄色
         */
        YELLOW("\33[1m\33[33m"),

        /**
         * 蓝色
         */
        BLUE("\33[1m\33[34m"),

        /**
         * 粉色
         */
        PINK("\33[1m\33[35m"),

        /**
         * 青色
         */
        CYAN("\33[1m\33[36m");

        /**
         * 颜色值
         */
        public final String value;

        ColorEnum(String value) {
            this.value = value;
        }
    }
}
