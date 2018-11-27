package com.lxh.disruptor.event;

import com.lmax.disruptor.EventFactory;

/**
 * @author lxh
 */
public class StringEventFactory implements EventFactory<StringEvent> {

    public StringEvent newInstance() {
        return new StringEvent();
    }
}
