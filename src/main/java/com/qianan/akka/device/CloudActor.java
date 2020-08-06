package com.qianan.akka.device;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

import java.util.*;

public class CloudActor extends AbstractActor {
    private final ActorRef centerControllerShardRegion;
    String port ;
    //key-commandId  用于上报命令处理结果给用户
    private Map<String, ActorRef> userMap;

    private Set<String> activeDevice = new HashSet<>();

    public CloudActor(ActorRef centerControllerShardRegion) {
        this.centerControllerShardRegion = centerControllerShardRegion;
        this.userMap = new HashMap<>();
    }

    public CloudActor(ActorRef centerControllerShardRegion, String prot) {
        this.centerControllerShardRegion = centerControllerShardRegion;
        this.port = prot;
        this.userMap = new HashMap<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    System.out.println("云端收到客户端消息  port = " + port);
                    sender().tell("666", getSelf());
                })
                .match(Report.HeartBeat.class, this::processHeartBeat)
                .match(Command.class, command -> processCommand(command, sender()))
                .match(Response.StandardResponse.class, this::processResponse)
                .build();
    }

    private void processHeartBeat(Report.HeartBeat hb) {
        activeDevice.add(hb.getCenterControllerDeviceId());
        if (!hb.isCenterController()) {
            activeDevice.add(hb.getDeviceId());
        }
    }

    private void processCommand(Command command, ActorRef sender) {
        System.out.println("处理命令啦");
        if (activeDevice.contains(command.getDeviceId())) {
            centerControllerShardRegion.tell(command, self());
            userMap.put(command.getId(), sender);
        }
    }

    private void processResponse(Response.StandardResponse response) {
        System.out.println("命令回复啦");
        userMap.get(response.getCommandId()).tell(response, self());
        userMap.remove(response.getCommandId());
    }
}
