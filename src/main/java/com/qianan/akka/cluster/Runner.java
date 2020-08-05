package com.qianan.akka.cluster;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Arrays;
import java.util.List;

public class Runner {
    public static void main(String[] args) {
        startupClusterNodes(Arrays.asList("2551", "2552", "2553"));
    }

    private static void startupClusterNodes(List<String> ports) {

        ports.forEach(port -> {
            ActorSystem actorSystem = ActorSystem.create("sharding", setupClusterNodeConfig(port));

            ActorRef shardingRegion = setupClusterSharding(actorSystem);

            actorSystem.actorOf(ClientActor.props(shardingRegion), "entityQuery");

        });
    }

    private static Config setupClusterNodeConfig(String port) {
        return ConfigFactory.parseString(
                String.format("akka.remote.netty.tcp.port=%s%n", port))
                .withFallback(ConfigFactory.load("cluster.conf"));
    }

    private static ActorRef setupClusterSharding(ActorSystem actorSystem) {
        ClusterShardingSettings settings = ClusterShardingSettings.create(actorSystem);
        return ClusterSharding.get(actorSystem).start(
                "serverActor",
                ServerActor.props(),
                settings,
                EntityMessage.messageExtractor()
        );
    }

}
