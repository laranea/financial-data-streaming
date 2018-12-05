package com.distributed.actors;
import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.distributed.domain.Trade;

import java.util.List;

public class Bucket extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    public String symbol;
    public List<ActorRef> clients;

    public static Props props(String symbol, List<ActorRef> clients) {
        return Props.create(Bucket.class, () -> new Bucket(symbol, clients));
    }
    public Bucket(String symbol, List<ActorRef> clients) {
        this.symbol = symbol;
        this.clients = clients;
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
                .match(Bucket.Receiver.class, receiver-> {
                    for (ActorRef ref : clients){
                        ref.tell(new ClientEndPoint.Receiver(receiver.trade), getSelf());
                    }
                }).build();
    }
}