package com.distributed.actors;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;

import java.time.Duration;

public class Supervision extends AbstractActor {

    public static Props props() {
        return Props.create(Supervision.class);
    }

    private static SupervisorStrategy strategy =
            new OneForOneStrategy(10, Duration.ofMinutes(1),
                    DeciderBuilder
                            .match(ArithmeticException.class, e -> SupervisorStrategy.resume())
                            .match(NullPointerException.class, e -> SupervisorStrategy.restart())
                            .match(IllegalArgumentException.class, e -> SupervisorStrategy.stop())
                            .matchAny(o -> SupervisorStrategy.escalate())
                            .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Props.class, props -> {
                    getSender().tell(getContext().actorOf(props), getSelf());
                })
                .build();
    }
}
