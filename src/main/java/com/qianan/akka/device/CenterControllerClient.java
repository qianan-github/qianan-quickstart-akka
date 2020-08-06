package com.qianan.akka.device;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class CenterControllerClient {
    public static void main(String[] args) throws InterruptedException {
        ActorSystem as = ActorSystem.create("centerControllerAS");
        ActorRef centerController = as.actorOf(Props.create(CenterController.class, "center01"));

        ActorRef lock1 = as.actorOf(Props.create(LockDevice.class, "lock1", centerController));
        ActorRef lock2 = as.actorOf(Props.create(LockDevice.class, "lock2", centerController));

        Command.AddLockPwdCommand addLock = new Command.AddLockPwdCommand("admin123", "new1", "lock1", IdFactory.IncrementIdFactory.instance);

        while (true) {
            centerController.tell(addLock, ActorRef.noSender());
            Thread.sleep(1000);
        }
    }
}
