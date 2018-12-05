package com.distributed.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

import java.util.List;

public class LoadbalancerActor extends AbstractActor {

    private List<ActorRef> actors;

    @Override
    public Receive createReceive() {
        return null;
    }
}
