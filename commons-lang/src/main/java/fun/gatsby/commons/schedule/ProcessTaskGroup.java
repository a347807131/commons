package fun.gatsby.commons.schedule;

import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class ProcessTaskGroup extends TaskGroup<Runnable> {

    LocalDateTime startDateTime;
    List<String> errTasks = new LinkedList<>();

    public ProcessTaskGroup(String name) {
        super();
        this.name = name;

        preTask = () -> {
            log.debug("任务组:[{}] 开始执行，共计任务数[{}]", name, size());
            startDateTime = LocalDateTime.now();
        };

        postTask = () -> {
            long between = LocalDateTimeUtil.between(startDateTime, LocalDateTime.now(), ChronoUnit.SECONDS);
            log.info("任务组:[{}]  执行完毕，共计任务数：{}, 平均单个任务耗时: {} s, 出错任务数:{}",
                    name, size(), between / this.size(), errTasks.size()
            );
            if (!errTasks.isEmpty())
                log.warn("任务组:[{}] 出错任务统计,共计:{}条,详情如下:\n{}",
                        name,
                        errTasks.size(),
                        String.join("\n", errTasks)
                );
        };
    }

    public ProcessTaskGroup(String name, Runnable funcPerTaskDone) {
        this(name);
        this.taskPerDone = funcPerTaskDone;
    }

    public ProcessTaskGroup(String name, Runnable funcPerTaskDone, Consumer<String> postTask) {
        this(name);
        this.taskPerDone = funcPerTaskDone;
        var oldPostTask = this.postTask;
        this.postTask = () -> {
            oldPostTask.run();
            long between = LocalDateTimeUtil.between(startDateTime, LocalDateTime.now(), ChronoUnit.SECONDS);
            String msg = String.format("任务组:[%s]  执行完毕，共计任务数：%d, 平均单个任务耗时: %d s, 出错任务数:%d",
                    name, size(), between / this.size(), errTasks.size()
            );
            postTask.accept(msg);
        };
    }

    @Override
    protected void onTaskException(Runnable task, Exception e) {
        String line = task.toString() + " 失败原因:" + e.getMessage();
        errTasks.add(line);
    }

}