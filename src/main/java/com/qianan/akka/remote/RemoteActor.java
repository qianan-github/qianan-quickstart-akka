package com.qianan.akka.remote;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class RemoteActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(String.class,this::processString)
                .build();
    }

    private void processString(String msg) {
        System.out.println(msg);
    }

    public static void main(String[] args) {
        Config config = ConfigFactory.load("remote.conf");
        ActorSystem system = ActorSystem.create("remoteSystem", config);
        system.actorOf(Props.create(RemoteActor.class), "remoteActor");
    }
}
