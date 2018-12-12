package com.distributed.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.codahale.metrics.Meter;
import com.distributed.DataUseApp;
import com.distributed.domain.Trade;
import com.google.gson.Gson;

import javax.websocket.Session;

public class ClientActor  extends AbstractActor {

    private final Session session;
    private final Meter requestsMeter;
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Gson jsonParser = new Gson();

    public static Props props(Session session) {
        return Props.create(ClientActor.class, () -> new ClientActor(session));
    }

    public ClientActor(Session session) {
        this.session = session;

        this.requestsMeter = DataUseApp.metrics.meter("throughput");
    }
    static public class SubscribeToBucket{
        String topic;
        public SubscribeToBucket(String topic) {
            this.topic = topic;
        }
    }

    static public class Message {
        public String message;

        public Message(String message) {
            this.message = message;
        }
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
                    requestsMeter.mark();
                    log.info(receiver.trade.toString());
                    this.session.getBasicRemote().sendText(jsonParser.toJson(receiver.trade));
                }).match(Message.class, m -> {
                            this.session.getBasicRemote().sendText(m.message);
                }
                ).build();

    }
}
