package com.qianan.akka.cluster;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.UUID;

class ClientActor extends AbstractLoggingActor {
    private final ActorRef shardRegion;

    private ClientActor(ActorRef shardRegion) {
        this.shardRegion = shardRegion;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("notice", t -> tickSending()).build();
    }

    private void tickSending() throws InterruptedException {
        while (true) {
            final EntityMessage.Query query = new EntityMessage.Query(UUID.randomUUID().toString());
            shardRegion.tell(query, self());
            Thread.sleep(1000L);
        }
    }


    @Override
    public void preStart() {
        self().tell("notice", self());
    }

    @Override
    public void postStop() {
    }

    static Props props(ActorRef shardRegion) {
        return Props.create(ClientActor.class, shardRegion);
    }
}
