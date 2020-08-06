package com.qianan.akka.device;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import akka.cluster.client.ClusterClientReceptionist;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;

public class CloudClient {
    public static void main(String[] args) {
        startupClusterNodes(Arrays.asList("2551", "2552", "2553"));
    }

    private static void startupClusterNodes(List<String> ports) {

        ports.forEach(port -> {
            ActorSystem actorSystem = ActorSystem.create("sharding", setupClusterNodeConfig(port));

            ActorRef shardingRegion = setupClusterSharding(actorSystem);

            ActorRef centerRef = actorSystem.actorOf(Props.create(CenterController.class, "center:" + UUID.randomUUID().toString(), shardingRegion), "entityQuery" );
            ClusterClientReceptionist.get(actorSystem).registerService(shardingRegion);
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
                Props.create(CloudActor.class),
                settings,
                messageExtractor()
        );
    }

    static ShardRegion.MessageExtractor messageExtractor() {

        return new ShardRegion.MessageExtractor() {
            @Override
            public String shardId(Object message) {
                return extractShardIdFromCommands(message);
            }

            @Override
            public String entityId(Object message) {
                return extractEntityIdFromCommands(message);
            }

            @Override
            public Object entityMessage(Object message) {
                return message;
            }

            private String extractShardIdFromCommands(Object message) {
                if (message instanceof Report.StandardReport) {
                    return ((Report.StandardReport) message).getDeviceId().hashCode() % 10 + "";
                } else if (message instanceof Command.StandardCommand) {
                    return ((Command.StandardCommand) message).getDeviceId().hashCode() % 10 + "";
                } else if (message instanceof Response.StandardResponse) {
                    return ((Response.StandardResponse) message).getDeviceId().hashCode() % 10 + "";
                } else {
                    return null;
                }
            }

            private String extractEntityIdFromCommands(Object message) {
                if (message instanceof Report.StandardReport) {
                    return ((Report.StandardReport) message).getDeviceId();
                } else if (message instanceof Command.StandardCommand) {
                    return ((Command.StandardCommand) message).getDeviceId();
                } else if (message instanceof Response.StandardResponse) {
                    return ((Response.StandardResponse) message).getDeviceId();
                } else {
                    return null;
                }
            }
        };
    }
}
