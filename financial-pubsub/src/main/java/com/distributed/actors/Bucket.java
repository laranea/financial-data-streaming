package com.distributed.actors;
import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.distributed.domain.Trade;

import java.util.ArrayList;
import java.util.List;

public class Bucket extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    public String topic;
    public List<ActorRef> clients;

    public static Props props(String topic) {
        return Props.create(Bucket.class, () -> new Bucket(topic));
    }
    public Bucket(String topic) {
        this.topic = topic;
        this.clients = new ArrayList<>();
    }

    static public class Receiver {
        public final Trade trade;
        public Receiver(Trade trade){
            this.trade = trade;
        }
    }
    static public class  NewClient{
        public final ActorRef client;
        public NewClient(ActorRef client){
            this.client = client;
        }
    }

    static public class RemoveClient {
        public final ActorRef client;

        public RemoveClient(ActorRef client) {
            this.client = client;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Bucket.Receiver.class, receiver-> {
                    for (ActorRef ref : clients){
                        ref.tell(new ClientActor.Receiver(receiver.trade), getSelf());
                    }
                }).match(Bucket.NewClient.class, newClient-> {
                    this.clients.add(newClient.client);
                })

                .build();
    }
}