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
        startupClusterNodes(Arrays.asList("2551"));
    }

    private static void startupClusterNodes(List<String> ports) {
        ports.forEach(port -> {
            ActorSystem actorSystem = ActorSystem.create("deviceSystem", setupClusterNodeConfig(port));
            ActorRef shardingRegion = setupClusterSharding(actorSystem);
            ActorRef cloudActor = actorSystem.actorOf(Props.create(CloudActor.class, shardingRegion), "cloudActor" );
//            ClusterClientReceptionist.get(actorSystem).registerService(cloudActor);
        });
    }

    private static Config setupClusterNodeConfig(String port) {
        return ConfigFactory.parseString(
                String.format("akka.remote.netty.tcp.port=%s%n", port))
                .withFallback(ConfigFactory.load("device.conf"));
    }

    private static ActorRef setupClusterSharding(ActorSystem actorSystem) {
        ClusterShardingSettings settings = ClusterShardingSettings.create(actorSystem);
        return ClusterSharding.get(actorSystem).start(
                "centerControllerActor",
                Props.create(CenterController.class, "centerController" + UUID.randomUUID().toString()),
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
                if (message instanceof Command.StandardCommand) {
                    return ((Command.StandardCommand) message).getDeviceId().hashCode() % 10 + "";
                } else {
                    return null;
                }
            }

            private String extractEntityIdFromCommands(Object message) {
                if (message instanceof Command.StandardCommand) {
                    return ((Command.StandardCommand) message).getDeviceId();
                } else {
                    return null;
                }
            }
        };
    }
}
