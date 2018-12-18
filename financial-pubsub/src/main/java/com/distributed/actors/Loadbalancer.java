package com.distributed.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.*;
public class Loadbalancer extends AbstractActor {

    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef router;

    static public Props props(ActorSelection subscribeActor) {
        return Props.create(Loadbalancer.class, () -> new Loadbalancer(subscribeActor));
    }

    public Loadbalancer(ActorSelection subscribeActor) {
        DefaultResizer resizer = new DefaultResizer(1, 15, 10,10,0,10,1000);
        router = getContext().actorOf(new RoundRobinPool(5).withResizer(resizer).props(Parser.props(getContext().actorOf(Sorter.props(subscribeActor)))));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchAny(o -> {
            router.tell(o, getSender());
        }).build();
    }
}
