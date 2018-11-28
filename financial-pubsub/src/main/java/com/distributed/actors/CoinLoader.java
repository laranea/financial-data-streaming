package com.distributed.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.distributed.actors.helloworld.Printer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPInputStream;

public class CoinLoader extends AbstractActor {


    static public Props props(String dataFilePath, ActorRef ref) {
        return Props.create(CoinLoader.class, () -> new CoinLoader(dataFilePath, ref));
    }

    static public class Start {
        public final long interval_millis;

        public Start(long interval_millis) {
            this.interval_millis = interval_millis;
        }
    }

    static private class Send { }

    static public class Stop {}

    private LoggingAdapter LOGGER = Logging.getLogger(getContext().getSystem(), this);

    private final ActorRef ref;
    private final String dataFilePath;
    private final List<String> data;
    private Cancellable cancellable;

    public CoinLoader(String dataFilePath, ActorRef ref) throws IOException {
        if(dataFilePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }

        this.dataFilePath = dataFilePath;
        this.data = new ArrayList<>();
        loadCoinsData();
        this.ref = ref;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Start.class, start -> {

                    ActorSystem sys = this.getContext().getSystem();

                    cancellable = sys.scheduler()
                            .schedule(Duration.ZERO,
                                Duration.ofMillis(start.interval_millis),
                                    getSelf(),
                                    new Send(),
                                    sys.dispatcher(),
                                    getSelf());


                    LOGGER.info("Sending trades every {} millis", start.interval_millis);
                })
                .match(Stop.class, stop -> {
                    if(cancellable != null) {
                        boolean canceled = cancellable.cancel();

                        if(canceled){
                            LOGGER.info("Stopped sending messages");
                        } else {
                            LOGGER.error("Could not cancel message loop");
                        }
                    }
                }).match(Send.class, send -> {
                    Random rand = new Random();

                    int index = rand.nextInt(data.size());

                    Object randomTrade = data.get(index);

                    ref.tell(new Parser.RAWJson(randomTrade), getSelf());
                })
                .build();
    }

    private void loadCoinsData() throws IOException {
        LOGGER.info("Reading file {}", this.dataFilePath);
        // Check if file exists else fail
        Path filePath = Paths.get(dataFilePath);
        if (!Files.exists(filePath)) {
            LOGGER.error("{} not found", filePath);
            throw new FileNotFoundException("Could not find file");
        }

        InputStream fileStream  = new FileInputStream(dataFilePath);
        InputStream gzipStream  = new GZIPInputStream(fileStream);
        Reader decoder          = new InputStreamReader(gzipStream);
        BufferedReader buffered = new BufferedReader(decoder);

        Gson gson = new GsonBuilder().create();
        LinkedTreeMap[] temp = gson.fromJson(buffered, LinkedTreeMap[].class);

        for(LinkedTreeMap m : temp) {
            this.data.add(m.toString());
        }

        LOGGER.debug("Read {} coin trades", data.size());
    }
}
