package com.lxh.disruptor.app;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lxh.disruptor.event.StringEvent;
import com.lxh.disruptor.event.StringEventFactory;
import com.lxh.disruptor.event.StringEventHandler;
import com.lxh.disruptor.producer.StringEventProducerWithTranslator;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author lxh
 */
public class AppMain {

    public static void main(String[] args) {
        // create thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        // create event factory
        StringEventFactory factory = new StringEventFactory();
        /**
         * 创建disruptor对象
         * event factory,传入实现了eventFactory接口的工厂类
         * ringBufferSize ， 用来存储数据的，值为2^n
         * executor,线程池
         * producerType,类型，可以是多个生产者，也可以是单个生产者
         * waitStrategy， 使用什么策略，消费者如何等待生产者放入disruptor中：
         *  BlockingWaitStrategy 是最低效的策略，但其最CPU的消耗最小，并且在各种不同的环境中能提供更一致性的表现
         *  SleepingWaitStrategy 的性能表现和BlockingWaitStrategy差不多，对CPU的消耗也类似，但其对生产者的影响最小，适用于异步日志类似的场景
         *  YieldingWaitStrategy 的性能是最好的，适用于低延迟的系统。在要求极高性能且事件处理线程小于CPU逻辑核心的场景中，推荐使用此策略
         *  例如：CPU开启超线程的特性
         */
        Disruptor<StringEvent> disruptor = new Disruptor<StringEvent>(factory,(int)Math.pow(2,20), executorService, ProducerType.SINGLE,
                new YieldingWaitStrategy());
        //关联处理器，也就是消费者连接消费事件方法
        disruptor.handleEventsWith(new StringEventHandler());
        // 启动
        disruptor.start();
        // 获取 ringBuffer 模拟生产者发消息
        RingBuffer<StringEvent> ringBuffer = disruptor.getRingBuffer();

        final StringEventProducerWithTranslator producer = new StringEventProducerWithTranslator(ringBuffer);
//        StringEventProducer producer = new StringEventProducer(ringBuffer);
        final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        //闭锁控制线程同步
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        new Thread(new Runnable() {
            public void run() {
                for (int i=0; i<10; i++) {
                    //下面是进行事件触发且发布
                    byteBuffer.put(new String("生产者发布第" + i + " 条消息").getBytes());
                    producer.sendData(byteBuffer);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                countDownLatch.countDown();

            }
        },"Thread 2").start();

        // 等待
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //关闭disruptor，方法会阻塞，直至所有事件已处理
        disruptor.shutdown();
        // 关闭使用的线程池，如果需要的话，必须 手动关， disruptor 在 shutdown时不会自动关
        executorService.shutdown();

    }
}
