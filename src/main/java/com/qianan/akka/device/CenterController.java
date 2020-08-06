package com.qianan.akka.device;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CenterController extends AbstractActor {
    private Map<String, ActorRef> subDevices;
    public final String deviceId;
    private ActorRef cloudRef;

    public CenterController(String deviceId) {
        this.deviceId = deviceId;
        this.subDevices = new HashMap<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Report.HeartBeat.class, this::forwardHeartBeat)
                .match(Command.StandardCommand.class, this::processCommand)
                .build();
    }

    private void forwardHeartBeat(Report.HeartBeat hb) {
        hb.setCenterControllerDeviceId(deviceId);
        hb.setCenterControllerReportTime(System.currentTimeMillis());
//        cloudRef.forward(hb, getContext());
        subDevices.put(hb.getDeviceId(), sender());
        System.out.println("|转发心跳");
    }

    private void processCommand(Command.StandardCommand command) {
        ActorRef subDevice = subDevices.get(command.getDeviceId());
        if (Objects.isNull(subDevice)) {
            System.out.println("设备离线");
            return;
        }

        subDevice.tell(command, self());
        System.out.println("处理命令");
    }
}
