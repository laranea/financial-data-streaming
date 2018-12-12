package com.distributed.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.distributed.domain.Trade;
import com.google.gson.Gson;
import scala.Option;


public class Parser extends AbstractActor {
    Gson gson;
    public ActorRef sorterRef;
    public int receivedTrades;
    public long uptimeSeconds;
    public long initializationTimestamp;
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props(ActorRef sorterRef) {
        return Props.create(Parser.class, () -> new Parser(sorterRef));
    }

    public Parser(ActorRef sorterRef){
        this.gson = new Gson();
        this.sorterRef = (sorterRef);
        this.initializationTimestamp = System.currentTimeMillis();
        this.receivedTrades = 0;
        this.uptimeSeconds = 0;
    }

    static public class RAWJson {
        public final Object json;
        public RAWJson(Object json) {
            this.json = json;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RAWJson.class, rawJson -> {
                    this.receivedTrades++;
                    this.uptimeSeconds = (System.currentTimeMillis() - this.initializationTimestamp) / 1000;
                    sorterRef.tell(new Sorter.Receiver(gson.fromJson((String) rawJson.json, Trade.class)), getSelf());
                }).build();
    }
}
