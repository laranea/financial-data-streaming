package com.distributed.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.distributed.domain.Trade;

public class Bucket extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props() {
        return Props.create(Subscriber.class, () -> new Subscriber());
    }

    static public class Receiver {
        public final Trade trade;
        public Receiver(Trade trade){
            this.trade = trade;
        }
    }
    public Bucket() {

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Sorter.Receiver.class, receiver-> {
                    log.info("Object: {}, Trade: {}", this, receiver.trade);
                }).build();
    }
}