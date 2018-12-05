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
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
//    public Map<String, List<Trade>> sortedTrades;
    public Map<String, List<ActorRef>> bucketRefs;
    public static Props props(Map bucketRefs) {
        return Props.create(Sorter.class, () -> new Sorter(bucketRefs));
    }

    static public class Receiver {
        public final Trade trade;
        public Receiver(Trade trade){
            this.trade = trade;
        }
    }
    public Sorter(Map bucketRefs) {
        this.bucketRefs = bucketRefs;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Receiver.class, receiver-> {
//                    sortedTrades.computeIfAbsent(receiver.trade.symbol_id, (x -> new ArrayList<>())).add(receiver.trade);
//
                    for (ActorRef bucketRef: bucketRefs.get(receiver.trade.symbol_id)) {
                        bucketRef.tell(new Bucket.Receiver(receiver.trade), getSelf());
                    }
                }).build();
    }
}
