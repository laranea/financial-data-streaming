package com.distributed.actors;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.distributed.domain.Trade;
import java.util.*;
import java.io.Serializable;

public class Bucket extends AbstractActor implements Serializable{
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    public String topic;
    public Set<ActorRef> clients;

    public static Props props(String topic) {
        return Props.create(Bucket.class, () -> new Bucket(topic));
    }
    public Bucket(String topic) {
        this.topic = topic;
        this.clients = new HashSet<>();
    }

    static class Receiver implements Serializable {
        public final Trade trade;
        public Receiver(Trade trade){
            this.trade = trade;
        }
    }
    static class  NewClient implements Serializable{
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
                .match(Receiver.class, receiver-> {
                    System.out.println("Receive message");
                    for (ActorRef ref : clients){
                        ref.tell(new ClientActor.Receiver(receiver.trade), getSelf());
                    }
                }).match(NewClient.class, newClient-> {
                    this.clients.add(newClient.client);
                }).match(RemoveClient.class, client -> {
                    this.clients.remove(client);
                })
                .build();
    }
}