package com.distributed.http;

import akka.actor.ActorRef;

import javax.websocket.server.ServerEndpointConfig;

public class PubSubEndpointConfiguration extends ServerEndpointConfig.Configurator {
    private ActorRef subscribtionActor;

    public PubSubEndpointConfiguration(ActorRef subscribtionActor) {
        this.subscribtionActor = subscribtionActor;
    }

    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
        return (T)new PubSubWebsocket(this.subscribtionActor);
    }
}
