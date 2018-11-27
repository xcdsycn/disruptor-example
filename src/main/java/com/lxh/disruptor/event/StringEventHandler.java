package com.lxh.disruptor.event;


import com.lmax.disruptor.EventHandler;

/**
 * @author lxh
 */
public class StringEventHandler implements EventHandler<StringEvent> {


    public void onEvent(StringEvent stringEvent, long sequence, boolean bool) throws Exception {
        System.out.println("StringEventHandler(消费者）：" + stringEvent + ", sequence=" + sequence + ",bool=" + bool);
    }
}
