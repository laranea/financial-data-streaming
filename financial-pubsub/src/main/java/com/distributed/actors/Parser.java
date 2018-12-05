package com.distributed.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.distributed.actors.helloworld.Printer;
import com.distributed.domain.Trade;
import com.google.gson.Gson;

public class Parser extends AbstractActor {
    Gson gson;
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public Parser(){
        gson = new Gson();
    }

    public static Props props() {
        return Props.create(Parser.class, () -> new Parser());
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
                    System.out.println(trade);
                }).build();
    }
}
