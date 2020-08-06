package com.qianan.akka.device;

import akka.actor.*;
import akka.cluster.client.ClusterClient;
import akka.cluster.client.ClusterClientSettings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UserClient {
    public static void main(String[] args) {
        Config conf = ConfigFactory.load("user.conf");

        ActorSystem actorSystem = ActorSystem.create("actorSystem", conf);

        ActorRef cloudRef = actorSystem.actorOf(
            ClusterClient.props(ClusterClientSettings.create(actorSystem)),
            "cloudRef");

        ActorRef userActor = actorSystem.actorOf(Props.create(UserActor.class, cloudRef));
    }

    public static Set<ActorPath> initialContacts() {
        return new HashSet<>(Collections
            .singletonList(ActorPaths.fromString("akka.tcp://deviceSystem@127.0.0.1:2551/system/receptionist")));
    }
}
