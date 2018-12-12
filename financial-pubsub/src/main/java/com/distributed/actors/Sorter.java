package com.distributed.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.distributed.domain.Trade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sorter  extends AbstractActor implements Serializable{
    private final ActorSelection subscribeActor;
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    public Map<String, List<ActorRef>> bucketRefs;

    public static Props props(ActorSelection subscriberActor) {
        return Props.create(Sorter.class, () -> new Sorter(subscriberActor));
    }
    public Sorter(ActorSelection subscriberActor) {
        this.subscribeActor = subscriberActor;
        this.bucketRefs = new HashMap<>();
        this.subscribeActor.tell(new Subscriber.GetBucketRefs(), getSelf());
    }

    public static class BucketRefs implements Serializable {
        private final Map<String, List<ActorRef>> bucketRefs;
        public BucketRefs(Map<String, List<ActorRef>> bucketRefs) {
            this.bucketRefs = bucketRefs;
        }
    }

    static public class Receiver implements Serializable{
        public final Trade trade;
        public Receiver(Trade trade){
            this.trade = trade;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Receiver.class, receiver-> {
                    if(bucketRefs.isEmpty()){
                        return;
                    }

                    for (ActorRef bucketRef : bucketRefs.get(receiver.trade.symbol_id)) {
                        bucketRef.tell(new Bucket.Receiver(receiver.trade), getSelf());
                    }

                }).match(BucketRefs.class, response -> {
                    this.bucketRefs = response.bucketRefs;
                }).build();
    }

}
