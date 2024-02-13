package fun.gatsby.commons.utils.utils;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

//@Slf4j
public class ConsoleProgressBar {

    private static final DecimalFormat floatPercentFormater = new DecimalFormat("0.00%");
    private static final DecimalFormat floatFormater = new DecimalFormat("0.00");
    private final AtomicInteger currentValue = new AtomicInteger(0);
    private final int totalStep;
    private final int step;
    private final AtomicInteger total = new AtomicInteger(100);
    char progressChar = '#';
    char waitChar = '-';
    int last = 0;
    private volatile long currentSpeed = 1024 * 1000;
    private int barLen = 50;

//    public ConsoleProgressBar() {
//        this.step = 0;
//        this.totalStep = 1;
//    }
    private long startTime;

    public ConsoleProgressBar(int total) {
        this.total.set(total);
        this.step = 0;
        this.totalStep = 1;
    }

    public ConsoleProgressBar(int total, int step, int totalStep) {
        this.total.set(total);
        this.step = step;
        this.totalStep = totalStep;
    }

    public static String genHMS(long second) {

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

    synchronized void show(int value) {
        last = value;
        int totalV = total.get();
        System.out.print('\r');
//        System.out.print(ColorEnum.RED.value);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.print(timestamp);

        // 比例
        float rate = value * 1f / totalV;

        int len = (int) (rate * barLen);
        StringBuilder sb = new StringBuilder(" Progress: ");
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
        sb.append(" |").append(genHMS(secondsLeft));
        sb.append(" |").append(totalV - value).append(" units left");
        sb.append(" |").append(step).append("/").append(totalStep);
        System.out.println(sb);
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