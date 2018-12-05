package com.distributed;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.distributed.actors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.distributed.properties.Tokens.*;
import static java.nio.file.StandardOpenOption.READ;

/**
 * Hello world!
 *
 */
public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    public static void main( String[] args ) throws IOException {
        if(args.length < 1){
            LOGGER.error("Missing argument for configuration file");
            System.exit(1);
        }

        String propertiesFilePath = args[0];

        Optional<Properties> oProperties = loadProperties(propertiesFilePath);

        if(!oProperties.isPresent()){
            System.exit(1);
        }

        Properties properties = oProperties.get();

        ActorSystem system = ActorSystem.create(properties.getProperty(ACTORSYSTEM_NAME));

        try {
            final ActorRef clientActor1 = system.actorOf(ClientEndPoint.props(), "clientActor1");
            final ActorRef clientActor2 = system.actorOf(ClientEndPoint.props(), "clientACtor2");
            List<ActorRef> JPYclients = new ArrayList<>();
            JPYclients.add(clientActor1);
            JPYclients.add(clientActor2);
            List<ActorRef> USDClients = new ArrayList<>();
            USDClients.add(clientActor1);

            Map<String, List<ActorRef>> bucketRefs = new HashMap<>();
            bucketRefs.put("BITFLYER_PERP_BTC_JPY", new ArrayList<>());
            bucketRefs.put("BITMEX_SPOT_BTC_USD", new ArrayList<>());

            final ActorRef bucketActor1 = system.actorOf(Bucket.props("BF Bucket", JPYclients), "bfBucketActor");
            final ActorRef bucketActor2 = system.actorOf(Bucket.props("BM Bucket", USDClients), "bmBucketActor");

            bucketRefs.get("BITFLYER_PERP_BTC_JPY").add(bucketActor1);
            bucketRefs.get("BITMEX_SPOT_BTC_USD").add(bucketActor2);
            final ActorRef sorterActor = system.actorOf(Sorter.props(bucketRefs), "sorterActor");
            final ActorRef parserActor = system.actorOf(Parser.props(sorterActor), "parserActor");
            String dataFilePath = properties.getProperty(DATA_FILE);

            final ActorRef loaderActor = system.actorOf(CoinLoader.props(dataFilePath, parserActor), "coinLoaderActor");

            long interval = Long.parseLong( properties.getProperty(INTERVAL_MS));

            loaderActor.tell(new CoinLoader.Start(interval), ActorRef.noSender());

            System.out.println("Press ENTER to exit the system");
            System.in.read();
        } finally {
            system.terminate();
        }
    }

    public static Optional<Properties> loadProperties(String propertiesFileName) {
        Path propertiesPath = Paths.get(propertiesFileName);
        if (!Files.exists(propertiesPath)) {
            LOGGER.error("{} not found", propertiesPath);
            return Optional.empty();
        }
        Properties properties = new Properties();
        try {
            InputStream inputStream = Files.newInputStream(propertiesPath, READ);
            properties.load(inputStream);
        } catch (IOException e) {
            LOGGER.error("could not load properties: {} exception: {}", propertiesPath, e);
            return Optional.empty();
        }
        return Optional.of(properties);
    }
}
