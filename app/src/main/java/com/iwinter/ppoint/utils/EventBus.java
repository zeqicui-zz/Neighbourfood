package com.iwinter.ppoint.utils;

import com.squareup.otto.Bus;

/**
 * Created by sandi on 04.01.2016..
 */
public class EventBus extends Bus {
    private static final EventBus bus = new EventBus();

    public static Bus getInstance(){return bus;}

    private EventBus(){}

}
