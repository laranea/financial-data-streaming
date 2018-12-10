package com.distributed.http;

import akka.actor.ActorRef;
import com.distributed.actors.Subscriber;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.HashSet;
import java.util.Set;

@ClientEndpoint
@ServerEndpoint(value="/subscribe/")
public class PubSubWebsocket {
    private final ActorRef subscribtionActor;

    public PubSubWebsocket(ActorRef subscribtionActor) {
        this.subscribtionActor = subscribtionActor;
    }

    @OnOpen
    public void onWebSocketConnect(Session sess) {
        System.out.println("Get ID: " + sess.getId());
        subscribtionActor.tell(new Subscriber.AddNewClient(sess), ActorRef.noSender());
    }

    @OnMessage
    public void onWebSocketText(String message, Session session) {
        System.out.println("Recv: " + message);

        // Extract symbols
        Set<String> symbols = getSymbolsFromMessage(message);

        subscribtionActor.tell(new Subscriber.AddNewSubscriptionForClient(session.getId(), symbols), ActorRef.noSender());
    }

    private Set<String> getSymbolsFromMessage(String message) {
        return new HashSet<>();
    }

    @OnClose
    public void onWebSocketClose(Session session, CloseReason reason) {
        System.out.println("Socket Closed: " + session.getId() + " - " + reason);
        subscribtionActor.tell(new Subscriber.RemoveClient(session.getId()), ActorRef.noSender());
    }

    @OnError
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace(System.err);
    }
}
