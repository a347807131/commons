package fun.gatsby.commons.schedule;

import lombok.extern.slf4j.Slf4j;


/**
 * @author gatsby
 */
@Slf4j
public class TaskGroup extends AbstractTaskGroup {

    protected void afterAllDone() {
        log.info("all tasks done");
    }

    protected void onTaskException(Exception e) {
        log.error("任务组中子任务出现异常", e);
    }

    protected synchronized void beforeFirstStart() {
        log.info("任务组第一个的第一个任务开始执行");
    }
}