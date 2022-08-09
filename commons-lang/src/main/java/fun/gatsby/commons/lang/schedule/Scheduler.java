package fun.gatsby.commons.lang.schedule;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;


/**
 * @Author: dinghao
 * @Date: 2022/3/7 15:29
 * @see <a href="https://blog.csdn.net/haohao_ding/article/details/123404531"</a>
 * @see <img src="https://img-blog.csdnimg.cn/883009304fb942b2b20a14d14d85c9fd.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAaGFvaGFvX2Rpbmc=,size_20,color_FFFFFF,t_70,g_se,x_16">
 */
@Slf4j
public class Scheduler {

    /**
     * 执行计划队列数组
     */
    private final LinkedBlockingQueue<ITask>[] taskQueueArray;
    /**
     * 允许并行执行的线程池
     */
    private final ExecutorService loopExecutor;
    /**
     * 允许并行执行的线程数
     */
    private final int nThrends;
    /**
     * 队列的数量
     */
    int queueNum;
    /**
     * 队列的容量
     */
    int queueSize;
    /**
     * 协助判断，是否线程池的任务全部结束
     */
    private final AtomicInteger count;
    /**
     * 执行模式：
     * 1：所有任务信息都执行
     * 2：先执行部分任务，执行完后再执行其他任务
     * 3：顺序执行任务中的计划
     * 4：顺序先执行执行任务中的计划，执行完后再顺序执行其他任务
     */
    private int model;

    /**
     * 每批执行的任务数量
     */
    private int modelSize;

    /**
     * model = 2,4 时有效
     */
    private ArrayList<Integer> indexList = new ArrayList<>();
    /**
     * 任务信息数组
     */
    private TaskGroup[] taskGroups;

    /**
     * 调度器的执行状态
     */
    private volatile boolean status = false;


    /**
     * 构造器
     *
     * @param taskGroups 任务数组
     * @param nThrends   同时执行任务中的计划线程数
     * @param queueSize  计划执行队列
     * @param model      执行模式 1：所有任务信息都执行 2：先执行部分任务，执行完后再执行其他任务
     * @param modelSize  每批执行任务的数量
     */
    public Scheduler(TaskGroup[] taskGroups, int nThrends, int queueNum, int queueSize, int model, Integer modelSize) {
        this.taskGroups = taskGroups;
        this.nThrends = nThrends;
        this.queueNum = queueNum;
        this.queueSize = queueSize;
        this.loopExecutor = Executors.newFixedThreadPool(this.nThrends);
        this.model = model;

        if (this.model < 3) {
            this.taskQueueArray = new LinkedBlockingQueue[1];
            this.taskQueueArray[0] = new LinkedBlockingQueue<>(this.queueSize);
        } else {
            // 初始化队列数组
            this.taskQueueArray = new LinkedBlockingQueue[this.queueNum];
            IntStream.range(0, this.queueNum).forEach(i -> this.taskQueueArray[i] = new LinkedBlockingQueue<>(this.queueSize));
        }

        // modelSize只有在等于2,4有效
        if (this.model == 2 || this.model == 4) {
            this.modelSize = modelSize > taskGroups.length ? taskGroups.length : modelSize;
        }

        count = countTask();
    }

    public Scheduler(TaskGroup[] taskGroups, int nThrends, int model, Integer modelSize) {
        this(taskGroups, nThrends, nThrends, 100, model, modelSize);
    }

    public Scheduler(TaskGroup[] taskGroups, int nThrends) {
        this(taskGroups, nThrends, nThrends, 100, 1, null);
    }

    public Scheduler(TaskGroup[] taskGroups) {
        this(taskGroups, 10, 10, 100, 1, null);
    }

    /**
     * 计算一共有多少执行计划
     *
     * @return /
     */
    private AtomicInteger countTask() {
        int sum = 0;
        for (TaskGroup taskGroup : this.taskGroups) {
            sum += taskGroup.getTaskQueue().size();
        }
        return new AtomicInteger(sum);
    }

    public int getModel() {
        return model;
    }

    public void setModel(int model) {
        this.model = model;
    }

    public int getModelSize() {
        return modelSize;
    }

    public void setModelSize(int modelSize) {
        this.modelSize = modelSize;
    }

    public ArrayList<Integer> getIndexList() {
        return indexList;
    }


    public AtomicInteger getCount() {
        return count;
    }

    public boolean isStatus() {
        return status;
    }

    /**
     * 执行方法
     */
    public void run() {
        if (this.status) {
            log.warn("Scheuler未就绪");
            return;
        }
        this.status = true;
        // 开启向队列中添加执行计划线程
        init();
        // 循环执行执行计划
        while (this.status) {
            // 所有执行计划执行完后，退出
            if (this.taskGroups.length == 0) {
                if (this.model < 3) {
                    if (this.taskQueueArray[0].size() == 0) {
                        this.status = false;
                        break;
                    }
                } else {
                    ArrayList<Integer> notEmptyIndex = getNotEmptyIndex(this.taskQueueArray);
                    if (CollUtil.isEmpty(notEmptyIndex)) {
                        this.status = false;
                        break;
                    }
                }
            }

            // 执行计划
            execute();

        }
//        int size;
//        // 所有线程执行完毕出循环
//        do {
//            size = this.count.get();
//        } while (size != 0);

        //停止线程池
        this.loopExecutor.shutdown();
        System.out.println("任务执行完毕");
//        for (; ; ) {
//            //只有当线程池中所有线程完成任务时才会返回true，并且需要先调用线程池的shutdown方法或者shutdownNow方法。
//            if (this.loopExecutor.isTerminated()) {
//                System.out.println("执行结束！");
//                break;
//            }
//        }
    }

    private void execute() {
        if (this.model < 3) {
            try {
                // 获取一个执行计划
                ITask plan = this.taskQueueArray[0].take();
                // 执行计划
                this.loopExecutor.execute(() -> plan.doWork(this.count));
            } catch (InterruptedException e) {
                log.error("任务执行中发生异常", e);
            }
        } else {
            this.loopExecutor.execute(() -> {
                try {
                    // 获取一个执行计划
                    ITask plan;
                    // 获取线程id
                    String name = Thread.currentThread().getName();
                    int lastIndexOf = name.lastIndexOf("-");
                    int id = Integer.parseInt(name.substring(lastIndexOf + 1));
                    ArrayList<Integer> notEmptyIndex2 = getNotEmptyIndex(this.taskQueueArray);
                    Integer index = notEmptyIndex2.stream().filter(item -> item % this.nThrends == (id - 1)).findAny().orElse(null);
                    if (index == null) {
                        return;
                    }
                    LinkedBlockingQueue<ITask> plans = this.taskQueueArray[index];
                    if (plans.size() > 0) {
                        plan = plans.take();
                        plan.doWork(this.count);
                    }
                } catch (InterruptedException e) {
                    log.error("任务执行中发生异常", e);
                }
            });
        }
    }

    private ArrayList<Integer> getNotEmptyIndex(LinkedBlockingQueue<ITask>[] taskQueueArray) {
        ArrayList<Integer> indexArray = new ArrayList<>();
        for (int i = 0; i < taskQueueArray.length; i++) {
            if (!taskQueueArray[i].isEmpty()) {
                indexArray.add(i);
            }
        }
        return indexArray;
    }

    /**
     * 开启一个线程，持续向执行计划队列添加执行计划，直到所有的计划任务添加完
     */
    private void init() {
        new Thread(() -> {
            while (this.status) {
                // 任务信息数组数量
                int length = this.taskGroups.length;
                // 执行完结束线程
                if (length == 0) {
                    break;
                }
                // 获取添加执行计划的的任务索引值
                int index = getIndexOfModel(this.model, length);
                TaskGroup taskGroup = this.taskGroups[index];

                LinkedList<ITask> plans = taskGroup.getTaskQueue();
                if (plans.size() > 0) {
                    try {
                        if (this.model >= 3) {
                            int index2 = taskGroup.getId() % this.taskQueueArray.length;
                            this.taskQueueArray[index2].put(plans.removeFirst());
                        } else {
                            this.taskQueueArray[0].put(plans.removeFirst());
                        }
                    } catch (InterruptedException e) {
                        log.error("向执行计划队列放入计划异常", e);
                        throw new RuntimeException(e);
                    }
                } else {
                    this.taskGroups = reBuildTaskInfos(this.taskGroups, index);
                }
            }
        }).start();
    }

    /**
     * 根据执行模式获取添加执行计划的的任务信息索引值
     *
     * @param model  执行模式
     * @param length 任务信息数组数量
     * @return 任务信息索引值
     */
    private int getIndexOfModel(int model, int length) {
        if (model == 1 || model == 3) {
            return RandomUtil.randomInt(0, length) % length;
        } else {
            this.indexList.removeIf(item -> item >= length);
            if (this.indexList.size() < this.modelSize) {
                int index = RandomUtil.randomInt(0, length) % length;
                this.indexList.add(index);
                return index;
            } else {
                return this.indexList.get(RandomUtil.randomInt(0, length) % this.indexList.size());
            }
        }
    }

    /**
     * 重新构建任务信息数组
     *
     * @param taskGroups 原来任务信息数组
     * @param index      需要移除的任务信息
     * @return 新的任务信息数组
     */
    private TaskGroup[] reBuildTaskInfos(TaskGroup[] taskGroups, int index) {
        TaskGroup[] newTaskINfo = new TaskGroup[taskGroups.length - 1];
        for (int j = 0, i = 0; i < taskGroups.length; i++) {
            if (i != index) {
                newTaskINfo[j] = taskGroups[i];
                j++;
            }
        }
        return newTaskINfo;
    }

}
