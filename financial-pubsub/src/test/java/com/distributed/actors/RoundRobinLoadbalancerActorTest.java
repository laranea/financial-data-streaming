package com.distributed.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RoundRobinLoadbalancerActorTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void test_rr(){
            final TestKit a1 = new TestKit(system);
            final TestKit a2 = new TestKit(system);

            List<ActorRef> actors = new ArrayList<>();
            actors.add(a1.getRef());
            actors.add(a2.getRef());

            final Props props = RoundRobinLoadbalancerActor.props(actors);
            final ActorRef subject = system.actorOf(props);

            subject.tell("done", a1.getRef());
            a1.expectMsg(Duration.ofSeconds(1), "done");
            a2.expectNoMessage(Duration.ofSeconds(1));

            subject.tell("done", a2.getRef());
            a1.expectNoMessage(Duration.ofSeconds(1));
            a2.expectMsg(Duration.ofSeconds(1), "done");

            subject.tell("done", a1.getRef());
            a2.expectNoMessage(Duration.ofSeconds(1));
            a1.expectMsg(Duration.ofSeconds(1), "done");
    }

}