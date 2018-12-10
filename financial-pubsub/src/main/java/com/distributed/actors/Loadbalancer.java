package com.distributed.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.*;

import java.util.ArrayList;
import java.util.List;

public class Loadbalancer extends AbstractActor {

    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private List<ActorRef> actors;
    private Router router;

    static public Props props(List<ActorRef> actors, ActorSelection subscribeActor) {
        return Props.create(Loadbalancer.class, () -> new Loadbalancer(actors, subscribeActor));
    }

    public Loadbalancer(List<ActorRef> actors, ActorSelection subscribeActor) {
        this.actors = actors;
        {
            List<Routee> routees = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                ActorRef sorter = getContext().actorOf(Sorter.props(subscribeActor));
                ActorRef parser = getContext().actorOf(Parser.props(sorter));
                getContext().watch(parser);
                routees.add(new ActorRefRoutee(parser));
            }
            router = new Router(new SmallestMailboxRoutingLogic(), routees);
        }
    }

    public static class Broadcast {
        public final Object objectToBroadcast;

        public Broadcast(Object objectToBroadcast) {
            this.objectToBroadcast = objectToBroadcast;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchAny(o -> {
            router.route(o, getSender());
        }).build();
    }
}
