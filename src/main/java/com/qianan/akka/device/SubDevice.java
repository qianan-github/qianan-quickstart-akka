package com.qianan.akka.device;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

public abstract class SubDevice extends AbstractActor {
    public final String deviceId;
    private boolean registered;
    //中控
    public final ActorRef centerController;

    public SubDevice(String deviceId, ActorRef centerController) {
        this.deviceId = deviceId;
        this.centerController = centerController;
    }
}
