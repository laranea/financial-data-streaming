package com.distributed.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RoundRobinLoadbalancerActor extends AbstractActor {

    static public Props props(List<ActorRef> actors) {
        return Props.create(RoundRobinLoadbalancerActor.class, () -> new RoundRobinLoadbalancerActor(actors));
    }

    public RoundRobinLoadbalancerActor(List<ActorRef> actors) {
        this.actors = actors;
        this.rrList = actors.iterator();
    }

    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private List<ActorRef> actors;
    private Iterator<ActorRef> rrList;

    public class Broadcast {
        public final Object objectToBroadcast;

        public Broadcast(Object objectToBroadcast) {
            this.objectToBroadcast = objectToBroadcast;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Broadcast.class, broadcast -> {
            for (ActorRef a : actors) {
                getSelf().tell(broadcast.objectToBroadcast, a);
            }
        }).matchAny(o -> {

            rrList.next();

        }).build();
    }
}
