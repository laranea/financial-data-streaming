package com.distributed.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Sorter  extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props() {
        return Props.create(Subscriber.class, () -> new Subscriber());
    }

    public Sorter() {
    }

    @Override
    public Receive createReceive() {
        return null;
    }
}
