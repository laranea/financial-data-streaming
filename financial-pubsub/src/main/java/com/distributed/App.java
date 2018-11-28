package com.distributed;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.distributed.actors.CoinLoader;
import com.distributed.actors.helloworld.Greeter;
import com.distributed.actors.helloworld.Printer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

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

        // Create Actor system where the actors will "live"
        ActorSystem system = ActorSystem.create(properties.getProperty(ACTORSYSTEM_NAME));

        try {
            // Create actors (note the ActorRef and not actual actor objects.
            final ActorRef printerActor = system.actorOf(Printer.props(), "printerActor");

            String dataFilePath = properties.getProperty(DATA_FILE);

            final ActorRef loaderActor = system.actorOf(CoinLoader.props(dataFilePath, printerActor), "coinLoaderActor");


            System.out.println(properties.getProperty(INTERVAL_MS));

            long interval = Long.parseLong( properties.getProperty(INTERVAL_MS));
            loaderActor.tell(new CoinLoader.Start(interval), ActorRef.noSender());

//            printerActor.tell(new Printer.Greeting("Direct message"), ActorRef.noSender());

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
