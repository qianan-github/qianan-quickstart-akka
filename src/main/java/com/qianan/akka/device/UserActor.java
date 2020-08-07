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
                Duration.create(3, TimeUnit.SECONDS),
                Duration.create(3, TimeUnit.SECONDS),
                this::sendAddLockPwdCommand,
                system.dispatcher());


        context().system().scheduler().schedule(
                Duration.create(8, TimeUnit.SECONDS),
                Duration.create(8, TimeUnit.SECONDS),
                this::sendDeleteLockPwdCommand,
                system.dispatcher());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }

    private void sendAddLockPwdCommand() {
        String newPwd = String.format("%06d", ((int) (Math.random() * 1000000)));
        System.out.println("用户：下发新增密码指令，新密码 <" + newPwd + ">");
        Command.AddLockPwdCommand command = new Command.AddLockPwdCommand(ADMIN_PWD, newPwd, "lock:1", "centerController:2551", IdFactory.IncrementIdFactory.instance);
        cloudRef.tell(new ClusterClient.Send("/user/cloudActor", command), self());
        pwdCache.add(newPwd);
    }

    private void sendDeleteLockPwdCommand() {
        String pwd = selectPwd();
        if (Objects.nonNull(pwd)) {
            System.out.println("用户：下发删除密码指令，待删除的密码 <" + pwd + ">");
            Command.DeletePwdCommand command = new Command.DeletePwdCommand(ADMIN_PWD, pwd, "lock:1", "centerController:2551", IdFactory.IncrementIdFactory.instance);
            cloudRef.tell(new ClusterClient.Send("/user/cloudActor", command), self());
            pwdCache.remove(pwd);
        } else {
            sendAddLockPwdCommand();
        }
    }

    private String selectPwd() {
        return pwdCache.isEmpty() ? null : pwdCache.get(0);
    }
}
