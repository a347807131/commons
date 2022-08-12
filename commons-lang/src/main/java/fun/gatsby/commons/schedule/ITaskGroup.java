package fun.gatsby.commons.schedule;

public interface ITaskGroup {
    /**
     * 全部任务执行完后的回调函数，只会有一个线程进入，也只会运行一次
     */
    void onAllDone();

    /**
     * 任务组中子任务出现异常时的回调函数，存在会有多个线程进入的情况
     */
    void onTaskException(Exception e);

    /**
     * 当任务组第一个的第一个任务开始执行时的函数，该函数执行完后其他任务才会开始执行<br/>
     * 只会有一个线程进入，也只会运行一次，后续不会再有线程进入
     */
    void onFirstStart();
}
