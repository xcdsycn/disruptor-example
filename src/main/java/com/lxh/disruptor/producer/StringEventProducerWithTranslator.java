package com.lxh.disruptor.producer;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.lxh.disruptor.event.StringEvent;

import java.nio.ByteBuffer;

/**
 * 这个是disruptor 3 推荐的方法
 * 发送数据与填充数据被translator分离开
 * @author lxh
 */
public class StringEventProducerWithTranslator {

    private final RingBuffer<StringEvent> ringBuffer;

    /**
     * 填充数据
     */
    public static final EventTranslatorOneArg<StringEvent, ByteBuffer> TRANSLATOR_ONE_ARG =  new EventTranslatorOneArg<StringEvent, ByteBuffer>() {
        public void translateTo(StringEvent stringEvent, long sequence, ByteBuffer byteBuffer) {
            // reading data from byteBuffer
            byteBuffer.flip();
            byte[] dst = new byte[byteBuffer.limit()];
            byteBuffer.get(dst, 0, dst.length);
            byteBuffer.clear();
            //padding StringEvent with data
            stringEvent.setValue(new String(dst));
            stringEvent.setId((int)sequence);
        }
    };

    public StringEventProducerWithTranslator(RingBuffer<StringEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }


    /**
     * 发布事件，就是发送数据
     * @param byteBuffer
     */
    public void sendData(ByteBuffer byteBuffer) {
        ringBuffer.publishEvent(TRANSLATOR_ONE_ARG,byteBuffer);
    }
}
