import com.distributed.http.PubSubWebsocket;
import org.eclipse.jetty.util.component.LifeCycle;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

public class Client {
    public static void main(String[] args) throws Exception {
        URI uri = URI.create("ws://localhost:8080/subscribe/");


            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            try {
                // Attempt Connect
                Session session = container.connectToServer(PubSubWebsocket.class, uri);
                // Send a message
                session.getBasicRemote().sendText("['BITFLYER_PERP_BTC_JPY', 'BITMEX_SPOT_BTC_USD']]");


                session.addMessageHandler(new MessageHandler.Whole<>() {
                    @Override
                    public void onMessage(Object o) {
                        System.out.println("RX " + o);
                    }
                });

                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e) {
                }

                // Close session
                session.close();
            } catch (DeploymentException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Force lifecycle stop when done with container.
                // This is to free up threads and resources that the
                // JSR-356 container allocates. But unfortunately
                // the JSR-356 spec does not handle lifecycles (yet)
                if (container instanceof LifeCycle) {
                    ((LifeCycle) container).stop();
                }
            }
    }
}
