package com.qianan.akka.simple;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import lombok.AllArgsConstructor;
import lombok.Data;

public class SimpleActor extends AbstractActor {

    @Data
    @AllArgsConstructor
    public static class Event {
        private ActorRef otherRef;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Event.class, this::processOtherRef)
                .match(String.class, this::processString).build();
    }

    private void processOtherRef(Event event) {
        event.otherRef.tell("hello", self());
    }

    private void processString(String msg) {
        System.out.println(msg);
    }

    public static void main(String[] args) {
        ActorSystem actorSystem = ActorSystem.create("simpleSystem");
        ActorRef actorRef = actorSystem.actorOf(Props.create(SimpleActor.class));
        ActorRef otherRef = actorSystem.actorOf(Props.create(SimpleActor.class));
        actorRef.tell(new Event(otherRef), actorRef);
    }
}
