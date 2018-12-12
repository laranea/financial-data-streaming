package com.distributed;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.distributed.actors.*;
<<<<<<< HEAD:financial-pubsub/src/main/java/com/distributed/App.java
import com.distributed.http.PubSubEndpointConfiguration;
import com.distributed.http.PubSubResource;
=======
>>>>>>> 54ea479bd0987978db9435b493b4c2750ed18209:financial-pubsub/src/main/java/com/distributed/DataUseApp.java
import com.distributed.http.PubSubWebsocket;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;
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
public class DataUseApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataUseApp.class);
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
        Config applicationConfig = ConfigFactory.load("application.conf");
        ActorSystem system = ActorSystem.create(properties.getProperty(ACTORSYSTEM_NAME), applicationConfig.getConfig("data-use-app"));

        try {
            final ActorRef subscriberActor = system.actorOf(Subscriber.props(), "subscriberActor");
<<<<<<< HEAD:financial-pubsub/src/main/java/com/distributed/App.java

//            final ActorRef clientActor1 = system.actorOf(ClientActor.props(subscriberActor), "clientActor1");
//            final ActorRef clientActor2 = system.actorOf(ClientActor.props(subscriberActor), "clientACtor2");
//            clientActor1.tell(new ClientActor.SubscribeToBucket("BITFLYER_PERP_BTC_JPY"), ActorRef.noSender());
//            clientActor1.tell(new ClientActor.SubscribeToBucket("BITMEX_SPOT_BTC_USD"), ActorRef.noSender());
//            clientActor2.tell(new ClientActor.SubscribeToBucket("BITFLYER_PERP_BTC_JPY"), ActorRef.noSender());



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

=======
            final ActorRef clientActor1 = system.actorOf(ClientActor.props(subscriberActor), "clientActor1");
            final ActorRef clientActor2 = system.actorOf(ClientActor.props(subscriberActor), "clientACtor2");
            clientActor1.tell(new ClientActor.SubscribeToBucket("BITFLYER_PERP_BTC_JPY"), ActorRef.noSender());
            clientActor1.tell(new ClientActor.SubscribeToBucket("BITMEX_SPOT_BTC_USD"), ActorRef.noSender());
            clientActor2.tell(new ClientActor.SubscribeToBucket("BITFLYER_PERP_BTC_JPY"), ActorRef.noSender());
>>>>>>> 54ea479bd0987978db9435b493b4c2750ed18209:financial-pubsub/src/main/java/com/distributed/DataUseApp.java
            /*
                Start websocket server
             */
            Server server = new Server();
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(8080);
            server.addConnector(connector);

            // Setup the basic application "context" for this application at "/"
            // This is also known as the handler tree (in jetty speak)
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);

            try
            {
                // Initialize javax.websocket layer
                ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);

                // Add WebSocket endpoint to javax.websocket layer
                wscontainer.addEndpoint(ServerEndpointConfig.Builder.create(PubSubWebsocket.class, "/subscribe/")
                        .configurator(new PubSubEndpointConfiguration(subscriberActor)).build());


                server.start();
                server.dump(System.out);
                server.join();

            }
            catch (Throwable t)
            {
                t.printStackTrace(System.err);
            }

            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
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
