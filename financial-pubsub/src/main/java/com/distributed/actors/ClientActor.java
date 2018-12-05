package com.distributed.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.distributed.domain.Trade;

public class ClientActor  extends AbstractActor {
    private final ActorRef subscriberActor;

    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props(ActorRef subscriberActor) {
        return Props.create(ClientActor.class, () -> new ClientActor(subscriberActor));
    }

    public ClientActor(ActorRef subscriberActor) {
        this.subscriberActor = subscriberActor;

    }
    static public class SubscribeToBucket{
        String topic;
        public SubscribeToBucket(String topic) {
            this.topic = topic;
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
                    log.info(receiver.trade.symbol_id);
                }).match(SubscribeToBucket.class, subscription -> {
                    this.subscriberActor.tell(new Subscriber.SubscribeClientToTopic(getSelf(), subscription.topic), getSelf());
                }).build();

    }
}
