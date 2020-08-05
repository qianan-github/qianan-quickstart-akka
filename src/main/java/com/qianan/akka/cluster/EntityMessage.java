package com.qianan.akka.cluster;

import akka.cluster.sharding.ShardRegion;

import java.io.Serializable;

class EntityMessage {

    static class Query implements Serializable {
        final long time;
        final String id;

        Query(String id) {
            time = System.nanoTime();
            this.id = id;
        }

        @Override
        public String toString() {
            return "time = " + time + "   id = " + id;
        }
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
                if (message instanceof Query) {
                    return ((Query) message).id.hashCode() % 3 + "";
                } else {
                    return null;
                }
            }

            private String extractEntityIdFromCommands(Object message) {
                if (message instanceof Query) {
                    return ((Query) message).id;
                } else {
                    return null;
                }
            }
        };
    }
}
