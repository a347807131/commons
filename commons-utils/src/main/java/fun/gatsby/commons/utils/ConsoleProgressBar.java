package fun.gatsby.commons.utils;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

//@Slf4j
public class ConsoleProgressBar {

    private static final DecimalFormat floatPercentFormater = new DecimalFormat("0.00%");
    private static final DecimalFormat floatFormater = new DecimalFormat("0.00");
    private final AtomicInteger currentValue = new AtomicInteger();
    private final AtomicInteger currentSpeed = new AtomicInteger(Integer.MAX_VALUE);
    char progressChar = '█';
    char waitChar = '#';
    private int total = 100;

    private int barLen = 50;

    public ConsoleProgressBar() {
    }

    public ConsoleProgressBar(int total) {
        this.total = total;
    }

    public synchronized void iterate() {
        this.currentValue.addAndGet(1);
    }

    public synchronized void iterate(int speed) {
        this.currentValue.addAndGet(1);
        this.currentSpeed.set(speed);
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
        float minsLeft = (1 - rate) * total / currentSpeed.get();

        System.out.print(" |" + floatPercentFormater.format(rate));
        System.out.print(" |" + floatFormater.format(minsLeft) + "mins");
        if (value == total)
            System.out.println();
    }

    public void showCurrent() {
        show(currentValue.get());
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
