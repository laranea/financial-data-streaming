package com.distributed.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import javax.websocket.Session;
import java.util.*;


public class Subscriber extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    Map<String, List<ActorRef>> clientRefs;
    Map<String, List<ActorRef>> bucketRefs;

    public static Props props() {
        return Props.create(Subscriber.class, () -> new Subscriber());
    }

    public Subscriber(){
        this.clientRefs = getClientRefs();
        this.bucketRefs = getBucketRefs();
    }

    private Map<String, List<ActorRef>> getClientRefs(){
        Map<String, List<ActorRef>> temp = new HashMap<>();
        temp.put("BITFLYER_PERP_BTC_JPY", new ArrayList<>());
        temp.put("BITMEX_SPOT_BTC_USD", new ArrayList<>());
        return temp;
    }

    private Map<String, List<ActorRef>> getBucketRefs(){
        Map<String, List<ActorRef>> temp = new HashMap<>();
        temp.put("BITFLYER_PERP_BTC_JPY", new ArrayList<>());
        temp.put("BITMEX_SPOT_BTC_USD", new ArrayList<>());

        final ActorRef bucketActor1 = getContext().actorOf(Bucket.props("BITFLYER_PERP_BTC_JPY"), "bfBucketActor");
        final ActorRef bucketActor2 = getContext().actorOf(Bucket.props("BITMEX_SPOT_BTC_USD"), "bmBucketActor");

        temp.get("BITFLYER_PERP_BTC_JPY").add(bucketActor1);
        temp.get("BITMEX_SPOT_BTC_USD").add(bucketActor2);
        return temp;
    }
    static class SubscribeClientToTopic{
        public ActorRef client;
        public String topic;
        public SubscribeClientToTopic(ActorRef client, String topic){
            this.client = client;
            this.topic = topic;
        }
    }

    public static class GetBucketRefs{

        public GetBucketRefs(){
        }
    }

    public static class AddNewClient {
        public final Session session;

        public AddNewClient(Session session) {
            this.session = session;
        }
    }

    public static class RemoveClient {
        public final String sessionId;

        public RemoveClient(String sessionId) {
            this.sessionId = sessionId;
        }
    }

    public static class AddNewSubscriptionForClient {
        public final String clientId;
        public final Set<String> symbols;

        public AddNewSubscriptionForClient(String clientId, Set<String> symbols) {
            this.clientId = clientId;
            this.symbols = symbols;
        }
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(SubscribeClientToTopic.class, subscription-> {
                clientRefs.get(subscription.topic).add(subscription.client);
                for(ActorRef bucketRef : bucketRefs.get(subscription.topic)){
                    bucketRef.tell(new Bucket.NewClient(subscription.client), getSelf());
                }
            }).match(GetBucketRefs.class, n -> {
                getSender().tell(new Sorter.BucketRefs(this.bucketRefs), getSelf());
            })

                .build();
    }
}
