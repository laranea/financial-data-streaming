package com.distributed.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.distributed.actors.helloworld.Printer;
import com.distributed.domain.Trade;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class Parser extends AbstractActor {
    Gson gson;
    public List<ActorRef> sorterRefs;
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public Parser(ActorRef sorterRef){
        this.gson = new Gson();
        this.sorterRefs = new ArrayList<>();
        this.sorterRefs.add(sorterRef);
    }
    public void addSorterRef(ActorRef sorterRef){
        this.sorterRefs.add(sorterRef);
    }
    public static Props props(ActorRef sorterRef) {
        return Props.create(Parser.class, () -> new Parser(sorterRef));
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
                    log.info("Trade: {}", rawJson.json);
                    Trade trade = gson.fromJson((String) rawJson.json, Trade.class);
                    for (ActorRef ref : this.sorterRefs){
                        ref.tell(new Sorter.Receiver(trade), getSelf());
                    }
                }).build();
    }
}
