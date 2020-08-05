package com.qianan.akka.cluster;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

class ServerActor extends AbstractLoggingActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(EntityMessage.Query.class, this::query)
                .build();
    }

    private void query(EntityMessage.Query query) {
        System.out.println(query + " " + sender());
    }

    static Props props() {
        return Props.create(ServerActor.class);
    }
}
