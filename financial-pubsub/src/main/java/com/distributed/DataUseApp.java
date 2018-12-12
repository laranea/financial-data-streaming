package com.distributed;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.distributed.actors.*;
import com.distributed.http.PubSubEndpointConfiguration;
import com.distributed.http.PubSubResource;
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
            ActorRef supervisionActor = system.actorOf(Supervision.props(), "supervisionActor");
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
