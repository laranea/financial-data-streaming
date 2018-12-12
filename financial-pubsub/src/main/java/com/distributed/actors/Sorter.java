package com.distributed.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.distributed.domain.Trade;
import java.io.Serializable;
import java.util.*;

public class Sorter  extends AbstractActor implements Serializable{
    private final ActorSelection subscribeActor;
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    public Map<String, List<ActorRef>> bucketRefs;

    public static Props props(ActorSelection subscriberActor) {
        return Props.create(Sorter.class, () -> new Sorter(subscriberActor));
    }
    public Sorter(ActorSelection subscriberActor) {
        this.subscribeActor = subscriberActor;
        this.bucketRefs = new HashMap<>();
        this.subscribeActor.tell(new Subscriber.GetBucketRefs(), getSelf());
    }

    public static class Refresh implements Serializable {
        private final ActorRef newBucket;
        private final String topic;
        public Refresh(String topic, ActorRef newBucket) {
            this.topic = topic;
            this.newBucket = newBucket;
        }
    }

    public static class BucketRefs implements Serializable {
        private final Map<String, List<ActorRef>> bucketRefs;
        public BucketRefs(Map<String, List<ActorRef>> bucketRefs) {
            this.bucketRefs = bucketRefs;
        }
    }

    static public class Receiver implements Serializable{
        public final Trade trade;
        public Receiver(Trade trade){
            this.trade = trade;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Receiver.class, receiver-> {
                    if(bucketRefs.isEmpty()){
                        return;
                    }

                    List<ActorRef> bucket = bucketRefs.get(receiver.trade.symbol_id);

                    if(bucket == null){
                        return;
                    }

                    for (ActorRef bucketRef : bucket) {
                        bucketRef.tell(new Bucket.Receiver(receiver.trade), getSelf());
                    }

                }).match(BucketRefs.class, response -> {
                    this.bucketRefs = response.bucketRefs;
                    System.out.println("Buckets " + this.bucketRefs  + " " + this.bucketRefs.size());
                }).match(Refresh.class, refresh-> {
                    List<ActorRef> buckets = this.bucketRefs.get(refresh.topic);

                    if(buckets == null){

                        List<ActorRef> newBuckets = List.of(refresh.newBucket);
                        this.bucketRefs.put(refresh.topic, newBuckets);

                        return;
                    }

                    List<ActorRef> bucketsCopy = new ArrayList<>(buckets);
                    this.bucketRefs.put(refresh.topic, bucketsCopy);
                }).build();
    }
    @Override
    public void preStart() {
        subscribeActor.tell(new Subscriber.NewSorter(getSelf()), getSelf());
    }

}
