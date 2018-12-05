package com.distributed.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPInputStream;

public class DataLoader extends AbstractActor {


    static public Props props(String dataFilePath, ActorRef ref) {
        return Props.create(DataLoader.class, () -> new DataLoader(dataFilePath, ref));
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

    public DataLoader(String dataFilePath, ActorRef ref) throws IOException {
        if(dataFilePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }

        this.dataFilePath = dataFilePath;
        this.data = loadCoinsData();
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

                    String randomTrade = data.get(index);

                    ref.tell(new Parser.RAWJson(randomTrade), getSelf());
                })
                .build();
    }

    private List<String> loadCoinsData() throws IOException {
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

        List<String> lines = new ArrayList<>();
        String line;
        while ((line = buffered.readLine()) != null) {
            lines.add(line);
        }

        LOGGER.debug("Read {} coin trades", lines.size());
        return lines;
    }
}
