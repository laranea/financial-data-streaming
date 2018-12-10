package com.distributed.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import javax.websocket.Session;
import java.util.*;


public class Subscriber extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final Map<String, ActorRef> clients;

    Map<String, List<ActorRef>> clientRefs;
    Map<String, List<ActorRef>> bucketRefs;

    public static Props props() {
        return Props.create(Subscriber.class, () -> new Subscriber());
    }

    public Subscriber(){
        this.clientRefs = getClientRefs();
        this.bucketRefs = new HashMap<>();
        clients = new HashMap<>();
    }

    private Map<String, List<ActorRef>> getClientRefs(){
        Map<String, List<ActorRef>> temp = new HashMap<>();
        temp.put("BITFLYER_PERP_BTC_JPY", new ArrayList<>());
        temp.put("BITMEX_SPOT_BTC_USD", new ArrayList<>());
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
            }).match(AddNewClient.class, c -> {
                String id = c.session.getId();
                ActorRef newClient = getContext().getSystem().actorOf(ClientActor.props(c.session), "client-" + id);
                this.clients.put(id, newClient);
                log.info("Added client actor with id {}", id);
            }).match(RemoveClient.class, c -> {
                    // Unsubscribe from bucket


                    // Kill client actor
                    ActorRef client = this.clients.get(c.sessionId);
                    client.tell(PoisonPill.getInstance(), getSelf());

                    log.info("Removed client actor with id {}", c.sessionId);
                })
                .match(AddNewSubscriptionForClient.class, sub -> {
                    ActorRef client = this.clients.get(sub.clientId);

                    for(String symbol : sub.symbols){
                        List<ActorRef> buckets = bucketRefs.getOrDefault(symbol, new ArrayList<>());

                        if (buckets.isEmpty()) {
                            log.info("Bucket for symbol {} does not exist. Creating new one.", symbol);

                            ActorRef newBucket = getContext().actorOf(Bucket.props(symbol), "bucket-" + symbol);
                            buckets.add(newBucket);

                            bucketRefs.put(symbol, buckets);
                        }

                        for (ActorRef bucket : buckets){
                            bucket.tell(new Bucket.NewClient(client), getSelf());
                        }


                        log.info("Added client {} to symbol subscription {}", sub.clientId, symbol);
                    }

//                    client.tell(new ClientActor.Message("Success"), ActorRef.noSender());


                }).build();
    }
}
