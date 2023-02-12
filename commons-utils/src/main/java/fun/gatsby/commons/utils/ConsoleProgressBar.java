package fun.gatsby.commons.utils;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

//@Slf4j
public class ConsoleProgressBar {

    private static final DecimalFormat floatPercentFormater = new DecimalFormat("0.00%");
    private static final DecimalFormat floatFormater = new DecimalFormat("0.00");
    private final AtomicInteger currentValue = new AtomicInteger(0);
    private volatile long currentSpeed = 1024 * 1000;
    char progressChar = '█';
    char waitChar = '#';
    private int total = 100;

    private int barLen = 50;

    private long startTime;

    public ConsoleProgressBar() {
    }

    public ConsoleProgressBar(int total) {
        this.total = total;
    }

    public synchronized void iterate() {
        int value = this.currentValue.addAndGet(1);
        if (value == 1) {
            this.startTime = System.currentTimeMillis();
        }
        show(value);
    }

    public void showCurrent() {
        show(0);
    }


    synchronized void show(int value) {
        System.out.print('\r');
        System.out.print(ColorEnum.RED.value);
        // 比例
        float rate = value * 1f / total;

        int len = (int) (rate * barLen);
        System.out.print("Progress: ");
        for (int i = 0; i < len; i++) {
            System.out.print(progressChar);
        }
        for (int i = 0; i < barLen - len; i++) {
            System.out.print(waitChar);
        }

        float secondsTotalSpent = value == 0 ? 0 : (System.currentTimeMillis() - startTime) / 1000f;
        float speed = value == 0 ? 0 : secondsTotalSpent / value;
        int secondsLeft = (int) ((total - value) * speed);

        System.out.print(" |" + floatPercentFormater.format(rate));
        System.out.print(" |" + floatFormater.format(speed) + " avg spi");
        System.out.print(" |" + genHMS(secondsLeft));
        System.out.print(" |" + (total - value) + " units left");

        if (value == total)
            System.out.println();
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
