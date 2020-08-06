package com.qianan.akka.device;

import java.util.*;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

public class CloudActor extends AbstractActor {
    private ActorRef centerControllerRef;
    //key-commandId  用于上报命令处理结果给用户
    private Map<String, ActorRef> userMap;
    //活跃设备
    private Set<String> activeDevice = new HashSet<>();

    public CloudActor() {
        this.userMap = new HashMap<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Report.HeartBeat.class, this::processHeartBeat)
                .match(Command.class, command -> processCommand(command, sender()))
                .match(Response.StandardResponse.class, this::processResponse)
                .match(Report.StandardReport.class, this::processReport)
                .build();
    }

    private void processReport(Report.StandardReport report) {
        System.out.println("设备上报数据！！！！！！！！！");
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
            centerControllerRef.tell(command, self());
            userMap.put(command.getCommandId(), sender);
        }
    }

    private void processResponse(Response.StandardResponse<?> response) {
        System.out.println("命令回复啦");
        ActorRef userRef = userMap.get(response.getCommandId());
        if (Objects.nonNull(userRef)) {
            userRef.tell(response, self());
            userMap.remove(response.getCommandId());
        }
    }
}
