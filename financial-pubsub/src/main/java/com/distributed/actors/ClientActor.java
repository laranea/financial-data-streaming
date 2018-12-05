package com.distributed.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.distributed.domain.Trade;

public class ClientEndPoint  extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props() {
        return Props.create(ClientEndPoint.class, () -> new ClientEndPoint());
    }

    public ClientEndPoint() {
    }

    static public class Receiver {
        public final Trade trade;

        public Receiver(Trade trade) {
            this.trade = trade;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Receiver.class, receiver -> {
                    log.info(receiver.trade.symbol_id);
                }).build();

    }
}
