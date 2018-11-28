package com.distributed;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.distributed.actors.Greeter;
import com.distributed.actors.Printer;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        // Create Actor system where the actors will "live"
        ActorSystem system = ActorSystem.create("financial-pubsub");

        try {
            // Create actors (note the ActorRef and not actual actor objects.
            final ActorRef printerActor = system.actorOf(Printer.props(), "printerActor");

            final ActorRef howdyGreeter =  system.actorOf(Greeter.props("Howdy", printerActor), "howdyGreeter");
            final ActorRef helloGreeter = system.actorOf(Greeter.props("Hello", printerActor), "helloGreeter");
            final ActorRef goodDayGreeter = system.actorOf(Greeter.props("Good day", printerActor), "goodDayGreeter");

            // Send some messages
            howdyGreeter.tell(new Greeter.WhoToGreet("Tim"), ActorRef.noSender());
            howdyGreeter.tell(new Greeter.Greet(), ActorRef.noSender());


            helloGreeter.tell(new Greeter.WhoToGreet("Laurens"), ActorRef.noSender());
            helloGreeter.tell(new Greeter.Greet(), ActorRef.noSender());

            goodDayGreeter.tell(new Greeter.WhoToGreet("sir"), ActorRef.noSender());
            goodDayGreeter.tell(new Greeter.Greet(), ActorRef.noSender());

            printerActor.tell(new Printer.Greeting("Direct message"), ActorRef.noSender());

            System.out.println("Press ENTER to exit the system");
            System.in.read();
        } finally {
            system.terminate();
        }
    }
}
