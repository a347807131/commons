package fun.gatsby.commons.utils.imagetask;

import cn.hutool.core.date.LocalDateTimeUtil;
import fun.gatsby.commons.schedule.TaskGroup;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
public class ProcessTaskGroup extends TaskGroup<Runnable> {

    LocalDateTime startDateTime;

    Runnable preTask=()->{
        startDateTime = LocalDateTime.now();
    };
    Runnable postTask=()->{
        long between = LocalDateTimeUtil.between(startDateTime, LocalDateTime.now(), ChronoUnit.SECONDS);
        String hms = genHMS(between);
        log.info("任务组: {} 执行完毕，总共耗时: {}, 单个任务耗时: {} s", name, hms, between / this.size());
    };

    public ProcessTaskGroup(String name) {
        super();
        this.name = name;

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
}
