package com.qianan.akka.device;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import scala.concurrent.duration.Duration;

public class LockDevice extends SubDevice {
    private int power = 100;
    private boolean dead = false;
    private List<String> pwds = new ArrayList<>();
    public static final String ADMIN_PWD = "admin123";

    public LockDevice(String deviceId, ActorRef centerController) {
        super(deviceId, centerController);
        pwds.add(ADMIN_PWD);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        ActorSystem system = context().system();
        //启动后，开始每两秒自动耗电
        system.scheduler().schedule(
                Duration.create(2, TimeUnit.SECONDS),
                Duration.create(2, TimeUnit.SECONDS),
                () -> {
                    if (!dead) {
                        self().tell("consumePower" , ActorRef.noSender());
                    }
                },
                system.dispatcher());

        //启动后，往中控发心跳
        system.scheduler().schedule(
                Duration.create(2, TimeUnit.SECONDS),
                Duration.create(2, TimeUnit.SECONDS),
                () -> {
                    if (!dead) {
                        centerController.tell(new Report.HeartBeat(deviceId), self());
                    }
                },
                system.dispatcher());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("consumePower", this::processConsumePower)
                .match(Command.AddLockPwdCommand.class, this::processAddLockPwdCommand)
                .match(Command.DeletePwdCommand.class, this::processDeleteLockPwdCommand)
                .build();
    }

    private void processConsumePower(String ignored) {
        if (!dead) {
            int deductPower = (int) (Math.random() * 10);
            power = Math.max(power - deductPower, 0);
            if (power == 0) {
                dead = true;
            }
        }
    }

    private void processAddLockPwdCommand(Command.AddLockPwdCommand command) {
        if (dead) {
            System.out.println("门锁没电，添加密码失败 command id <{" + command.getCommandId() + "}>");
            return;
        }

        if (!Objects.equals(command.getAdminPwd(), ADMIN_PWD)) {
            System.out.println("管理员密码错误，添加密码失败 command id <{" + command.getCommandId() + "}>");
            return;
        }

        pwds.add(command.getPwd());
    }

    private void processDeleteLockPwdCommand(Command.DeletePwdCommand command) {
        if (dead) {
            System.out.println("门锁没电，添加密码失败 command id <{" + command.getCommandId() + "}>");
            return;
        }

        if (!Objects.equals(command.getAdminPwd(), ADMIN_PWD)) {
            System.out.println("管理员密码错误，添加密码失败 command id <{" + command.getCommandId() + "}>");
            return;
        }

        pwds.remove(command.getPwd());
    }
}
