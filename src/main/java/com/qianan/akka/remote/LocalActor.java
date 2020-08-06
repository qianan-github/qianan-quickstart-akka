package com.qianan.akka.remote;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.HashMap;
import java.util.Map;

public class LocalActor extends AbstractActor {
    private ActorSelection remoteActor = context().actorSelection("akka.tcp://remoteSystem@127.0.0.1:2553/user.conf/remoteActor");

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(String.class,this::processString)
                .build();
    }

    private void processString(String msg) {
        remoteActor.tell(msg + "from local", getSelf());
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("akka.remote.netty.tcp.port", 0);
        map.put("akka.actor.provider", "akka.remote.RemoteActorRefProvider");
        Config config = ConfigFactory.parseMap(map);
        ActorSystem system = ActorSystem.create("remoteSystem", config);
        system.actorOf(Props.create(LocalActor.class), "remoteActor").tell("hello", ActorRef.noSender());
    }
}