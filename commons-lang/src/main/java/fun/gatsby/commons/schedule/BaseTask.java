package fun.gatsby.commons.schedule;


import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
public abstract class BaseTask implements ITask {

    public Thread threadToRun;
    @Getter
    protected TaskStateEnum state = TaskStateEnum.NEW;
    private LocalDateTime startDate;

    @Override
    public void before() {
        state = TaskStateEnum.RUNNING;
        startDate = LocalDateTime.now();
        threadToRun = Thread.currentThread();
    }

    @Override
    public void after() {
        long between = LocalDateTimeUtil.between(startDate, LocalDateTime.now(), ChronoUnit.SECONDS);
        log.debug("任务执行时间：{}s",between);
        state = TaskStateEnum.FINISHED;
    }

    @Override
    public void onError(Throwable e) {
        state = TaskStateEnum.ERROR;
        log.error("任务执行异常",e);
    }
}
