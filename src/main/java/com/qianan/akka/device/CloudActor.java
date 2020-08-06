package com.qianan.akka.device;

import java.util.*;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

public class CloudActor extends AbstractActor {
    private final ActorRef centerControllerShardRegion;
    private final String centerControllerId;
    //key-commandId  用于上报命令处理结果给用户
    private Map<String, ActorRef> userMap;
    //活跃设备
    private Set<String> activeDevice = new HashSet<>();

    public CloudActor(ActorRef centerControllerShardRegion, String centerControllerId) {
        this.centerControllerShardRegion = centerControllerShardRegion;
        this.userMap = new HashMap<>();
        this.centerControllerId = centerControllerId;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        Command.HelloCommand hello = new Command.HelloCommand(centerControllerId, IdFactory.IncrementIdFactory.instance);
        centerControllerShardRegion.tell(hello, self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Report.HeartBeat.class, this::processHeartBeat)
                .match(Response.HelloResponse.class, this::processHelloResponse)
                .match(Command.StandardCommand.class, this::processCommand)
                .match(Response.StandardResponse.class, this::processResponse)
                .match(Report.StandardReport.class, this::processReport)
                .build();
    }

    private void processHelloResponse(Response.HelloResponse response) {
        System.out.println("hello !");
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

    private void processCommand(Command.StandardCommand command) {
        System.out.println("处理命令啦");
        if (activeDevice.contains(command.getDeviceId())) {
            centerControllerShardRegion.tell(command, self());
            userMap.put(command.getCommandId(), sender());
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
