package com.qianan.akka.device;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.client.ClusterClient;
import akka.cluster.client.ClusterClientSettings;

public class UserClient {
    public static void main(String[] args) {
        Config conf = ConfigFactory.load("user.conf");

        ActorSystem actorSystem = ActorSystem.create("actorSystem", conf);

        ActorRef cloudRef = actorSystem.actorOf(
            ClusterClient.props(ClusterClientSettings.create(actorSystem)),
            "cloudRef");

        ActorRef userActor = actorSystem.actorOf(Props.create(UserActor.class, cloudRef));
    }
}
