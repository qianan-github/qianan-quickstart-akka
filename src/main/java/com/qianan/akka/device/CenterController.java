package com.qianan.akka.device;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class CenterController extends AbstractActor {
    private Map<String, ActorRef> subDevices;
    public final String deviceId;
    private ActorRef cloudRef;
    private static AtomicInteger counter = new AtomicInteger(0);

    public CenterController(String deviceId) {
        this.deviceId = deviceId;
        this.subDevices = new HashMap<>();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        ActorSystem system = getContext().getSystem();
        system.actorOf(Props.create(LockDevice.class, "lock:" + counter.getAndIncrement(), self()));
        system.actorOf(Props.create(LockDevice.class, "lock:" + counter.getAndIncrement(), self()));

//        system.scheduler().schedule(
//                Duration.create(1, TimeUnit.SECONDS),
//                Duration.create(1, TimeUnit.SECONDS),
//                () -> {
//                    if (!dead) {
//                        //启动后，开始每两秒自动耗电
//                        self().tell("consumePower" , ActorRef.noSender());
//                        //启动后，往中控发心跳
//                        centerController.tell(new Report.HeartBeat(deviceId), self());
//                    }
//                },
//                system.dispatcher()
//        );
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Command.HelloCommand.class, this::processHelloCommand)
                .match(Report.HeartBeat.class, this::forwardHeartBeat)
                .match(Command.StandardCommand.class, this::processCommand)
                .match(Report.LockPwdReport.class, this::processLockPwdReport)
                .build();
    }

    private void processHelloCommand(Command.HelloCommand command) {
        Response.HelloResponse response = new Response.HelloResponse(deviceId);
        cloudRef = sender();
        cloudRef.tell(response, self());
    }

    private void forwardHeartBeat(Report.HeartBeat hb) {
        subDevices.put(hb.getDeviceId(), sender());
        if (Objects.isNull(cloudRef)) {
            System.out.println("中控：云端离线 转发 <" + hb.getDeviceId() + "> 心跳失败！");
            return;
        }
        hb.setCenterControllerDeviceId(deviceId);
        hb.setCenterControllerReportTime(System.currentTimeMillis());
        cloudRef.forward(hb, getContext());
    }

    private void processLockPwdReport(Report.LockPwdReport report) {
        if (Objects.isNull(cloudRef)) {
            System.out.println("中控：云端离线 上报 <" + report.getDeviceId() + "> 数据失败！");
            return;
        }
        report.setCenterControllerDeviceId(deviceId);
        report.setCenterControllerReportTime(System.currentTimeMillis());
        cloudRef.forward(report, getContext());
    }

    private void processCommand(Command.StandardCommand command) {
        ActorRef subDevice = subDevices.get(command.getDeviceId());
        if (Objects.isNull(subDevice)) {
//            System.out.println("中控：设备 <" + command.getDeviceId() + "> 离线 下发命令失败！");
            return;
        }

        subDevice.tell(command, self());
    }
}
