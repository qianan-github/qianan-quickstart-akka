package com.qianan.akka.device;

import java.util.Arrays;
import java.util.List;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;

public class CloudClient {
    public static void main(String[] args) {
        startupClusterNodes(Arrays.asList("2551", "2552"));
    }

    private static void startupClusterNodes(List<String> ports) {
        ports.forEach(port -> {
            ActorSystem actorSystem = ActorSystem.create("deviceSystem", setupClusterNodeConfig(port));

            String centerControllerId = "centerController:" + port;
            ActorRef shardingRegion =  ClusterSharding.get(actorSystem).start(
                    "centerControllerActor",
                    Props.create(CenterController.class, centerControllerId),
                    ClusterShardingSettings.create(actorSystem),
                    messageExtractor()
            );
            ActorRef cloudActor = actorSystem.actorOf(Props.create(CloudActor.class, shardingRegion, centerControllerId), "cloudActor");
            ClusterClientReceptionist.get(actorSystem).registerService(cloudActor);
        });
    }

    private static Config setupClusterNodeConfig(String port) {
        return ConfigFactory.parseString(
                String.format("akka.remote.netty.tcp.port=%s%n", port))
                .withFallback(ConfigFactory.load("device.conf"));
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
                    return ((Command.StandardCommand) message).getCenterControllerId().hashCode() % 10 + "";
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
                    return ((Command.StandardCommand) message).getCenterControllerId();
                } else if (message instanceof Response.StandardResponse) {
                    return ((Response.StandardResponse) message).getDeviceId();
                } else {
                    return null;
                }
            }
        };
    }
}
