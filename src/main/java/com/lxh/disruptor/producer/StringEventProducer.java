package com.lxh.disruptor.producer;

import com.lmax.disruptor.RingBuffer;
import com.lxh.disruptor.event.StringEvent;

import java.nio.ByteBuffer;

/**
 * 这个是disruptor 2 的方式
 * @author lxh
 */
public class StringEventProducer {

    private final RingBuffer<StringEvent> ringBuffer;

    /**
     *
     * @param ringBuffer
     */
    public StringEventProducer(RingBuffer<StringEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    /**
     * 发送数据，与事件发布（发送数据）混合在一起
     * @param byteBuffer
     */
    public void sendData(ByteBuffer byteBuffer) {
        // ringBuffer用来存放数据的，具体可以看disruptor源码的数据结构，next就是获取 下一个空事件索引
        long sequence = ringBuffer.next();
        try {
            StringEvent stringEvent = ringBuffer.get(sequence);
            // 切成读模式
            byteBuffer.flip();
            // 从byteBuffer中读出传来的值
            byte[] dst = new byte[byteBuffer.limit()];
            byteBuffer.get(dst, 0, dst.length);
            // 为stringEvent 赋值，填充数据
            stringEvent.setValue(new String(dst));
            stringEvent.setId((int) sequence);
            // clear 缓冲区
            byteBuffer.clear();
        }finally {
            //发布事件，为确保安全，放在finally中，不会造成disruptor的混乱
            ringBuffer.publish(sequence);
        }
    }
}
