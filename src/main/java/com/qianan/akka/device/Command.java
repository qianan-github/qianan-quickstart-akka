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

        public StandardCommand(String deviceId, IdFactory idFactory) {
            this.commandId = idFactory.id();
            this.deviceId = deviceId;
            this.issueTime = System.currentTimeMillis();
        }
    }

    @Data
    class AddLockPwdCommand extends StandardCommand {
        private String adminPwd;
        private String pwd;

        AddLockPwdCommand(String adminPwd, String pwd, String deviceId, IdFactory idFactory) {
            super(deviceId, idFactory);
            this.adminPwd = adminPwd;
            this.pwd = pwd;
        }
    }

    @Data
    class DeletePwdCommand extends StandardCommand {
        private String adminPwd;
        private String pwd;

        DeletePwdCommand(String adminPwd, String pwd, String deviceId, IdFactory idFactory) {
            super(deviceId, idFactory);
            this.adminPwd = adminPwd;
            this.pwd = pwd;
        }
    }

    @Data
    class OpenElectricCommand extends StandardCommand {
        public OpenElectricCommand(String deviceId, IdFactory idFactory) {
            super(deviceId, idFactory);
        }
    }

    @Data
    class CloseElectricCommand extends StandardCommand {
        public CloseElectricCommand(String deviceId, IdFactory idFactory) {
            super(deviceId, idFactory);
        }
    }
}
