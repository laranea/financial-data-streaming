package com.distributed.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.distributed.domain.Trade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Sorter  extends AbstractActor {
    private final ActorRef subscribeActor;
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    public Map<String, List<ActorRef>> bucketRefs;

    public static Props props(ActorRef subscriberActor) {
        return Props.create(Sorter.class, () -> new Sorter(subscriberActor));
    }
    public Sorter(ActorRef subscriberActor) {
        this.subscribeActor = subscriberActor;
        this.subscribeActor.tell(new Subscriber.GetBucketRefs(), getSelf());
    }

    public static class BucketRefs {
        private final Map<String, List<ActorRef>> bucketRefs;

        public BucketRefs(Map<String, List<ActorRef>> bucketRefs) {
            this.bucketRefs = bucketRefs;
        }
    }

    static public class Receiver {
        public final Trade trade;
        public Receiver(Trade trade){
            this.trade = trade;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Receiver.class, receiver-> {
                    for (ActorRef bucketRef: bucketRefs.get(receiver.trade.symbol_id)) {
                        bucketRef.tell(new Bucket.Receiver(receiver.trade), getSelf());
                    }
                }).match(BucketRefs.class, response -> {
                    this.bucketRefs = response.bucketRefs;
                }).build();
    }

}
