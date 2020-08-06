package com.qianan.akka.device;

import static com.qianan.akka.device.LockDevice.ADMIN_PWD;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.client.ClusterClient;
import scala.concurrent.duration.Duration;

public class UserActor extends AbstractActor {
    private final ActorRef cloudRef;
    private List<String> pwdCache;

    public UserActor(ActorRef cloudRef) {
        this.cloudRef = cloudRef;
        this.pwdCache = new ArrayList<>();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        ActorSystem system = context().system();
        context().system().scheduler().schedule(
                Duration.create(2, TimeUnit.SECONDS),
                Duration.create(2, TimeUnit.SECONDS),
                () -> {
                    sendAddLockPwdCommand();
                    sendDeleteLockPwdCommand();
                },
                system.dispatcher());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Report.LockPwdReport.class, this::processLockPwdReport)
                .build();
    }

    private void processLockPwdReport(Report.LockPwdReport report) {
        pwdCache = report.getPwds();
    }

    private void sendAddLockPwdCommand() {
        String newPwd = String.format("%06d", ((int) (Math.random() * 1000000)));
        Command.AddLockPwdCommand command = new Command.AddLockPwdCommand(ADMIN_PWD, newPwd, "BIG_DOOR_LOCK", IdFactory.IncrementIdFactory.instance);
        cloudRef.tell(new ClusterClient.SendToAll("/user/cloudActor", command), self());
        pwdCache.add(newPwd);
    }

    private void sendDeleteLockPwdCommand() {
        String pwd = selectPwd();
        if (Objects.nonNull(pwd)) {
            Command.DeletePwdCommand command = new Command.DeletePwdCommand(ADMIN_PWD, pwd, "BIG_DOOR_LOCK", IdFactory.IncrementIdFactory.instance);
            cloudRef.tell(new ClusterClient.SendToAll("/user/cloudActor", command), self());
        } else {
            sendAddLockPwdCommand();
        }
    }

    private String selectPwd() {
        return pwdCache.isEmpty() ? null : pwdCache.get(0);
    }
}
