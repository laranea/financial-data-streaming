package com.distributed.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.Iterator;
import java.util.List;

public class RoundRobinLoadbalancerActor extends AbstractActor {

    static public Props props(List<ActorRef> actors) {
        return Props.create(RoundRobinLoadbalancerActor.class, () -> new RoundRobinLoadbalancerActor(actors));
    }

    public RoundRobinLoadbalancerActor(List<ActorRef> actors) {
        this.actors = actors;
    }

    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private List<ActorRef> actors;
    private int pointer = 0;

    public static class Broadcast {
        public final Object objectToBroadcast;

        public Broadcast(Object objectToBroadcast) {
            this.objectToBroadcast = objectToBroadcast;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Broadcast.class, broadcast -> {
            for (ActorRef a : actors) {
                a.forward(broadcast.objectToBroadcast, getContext());
            }
        }).matchAny(o -> {

            int len = actors.size();

            if(len == 0){
                log.error("No actors to send message too");
                return;
            }

            if(pointer >= len){
                pointer = 0;
            }

            ActorRef actor = actors.get(pointer);

            log.info("{} sent message {} to {}", getSender(), o, actor);
            actor.forward(o, getContext());
            pointer++;
        }).build();
    }
}
