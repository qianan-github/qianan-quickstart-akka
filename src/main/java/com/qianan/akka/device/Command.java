package com.qianan.akka.device;

import lombok.Data;

import java.io.Serializable;

public interface Command extends Serializable {
    String getDeviceId();

    String getCommandId();

    @Data
    class StandardCommand implements Command {
        private long issueTime;
        private String commandId;
        private String deviceId;
        private String centerControllerId;

        public StandardCommand(String deviceId, String centerControllerId, IdFactory idFactory) {
            this.commandId = idFactory.id();
            this.deviceId = deviceId;
            this.issueTime = System.currentTimeMillis();
            this.centerControllerId = centerControllerId;
        }
    }

    @Data
    class HelloCommand extends StandardCommand {
        public HelloCommand(String deviceId, IdFactory idFactory) {
            super(deviceId, deviceId, idFactory);
        }
    }

    @Data
    class AddLockPwdCommand extends StandardCommand {
        private String adminPwd;
        private String pwd;

        AddLockPwdCommand(String adminPwd, String pwd, String deviceId, String centerControllerId, IdFactory idFactory) {
            super(deviceId, centerControllerId, idFactory);
            this.adminPwd = adminPwd;
            this.pwd = pwd;
        }
    }

    @Data
    class DeletePwdCommand extends StandardCommand {
        private String adminPwd;
        private String pwd;

        DeletePwdCommand(String adminPwd, String pwd, String deviceId, String centerControllerId, IdFactory idFactory) {
            super(deviceId, centerControllerId, idFactory);
            this.adminPwd = adminPwd;
            this.pwd = pwd;
        }
    }
}
