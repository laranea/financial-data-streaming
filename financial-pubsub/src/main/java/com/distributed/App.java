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
            final ActorRef subscriberActor = system.actorOf(Subscriber.props(), "subscriberActor");

            final ActorRef clientActor1 = system.actorOf(ClientActor.props(subscriberActor), "clientActor1");
            final ActorRef clientActor2 = system.actorOf(ClientActor.props(subscriberActor), "clientACtor2");
            clientActor1.tell(new ClientActor.SubscribeToBucket("BITFLYER_PERP_BTC_JPY"), ActorRef.noSender());
            clientActor1.tell(new ClientActor.SubscribeToBucket("BITMEX_SPOT_BTC_USD"), ActorRef.noSender());
            clientActor2.tell(new ClientActor.SubscribeToBucket("BITFLYER_PERP_BTC_JPY"), ActorRef.noSender());



            final ActorRef sorterActor = system.actorOf(Sorter.props(subscriberActor), "sorterActor");
            final ActorRef parserActor = system.actorOf(Parser.props(sorterActor), "parserActor");
            String dataFilePath = properties.getProperty(DATA_FILE);


            List<ActorRef> parsers = new ArrayList<>();
            parsers.add(parserActor);

            // Loadbalancer (data loader -> parsers)
            ActorRef rrActor = system.actorOf(RoundRobinLoadbalancerActor.props(parsers));


            // Data loader actor
            final ActorRef loaderActor = system.actorOf(DataLoader.props(dataFilePath, rrActor), "coinLoaderActor");

            long interval = Long.parseLong( properties.getProperty(INTERVAL_MS));

            loaderActor.tell(new DataLoader.Start(interval), ActorRef.noSender());

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
