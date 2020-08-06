package com.qianan.akka.device;

import lombok.Data;

import java.io.Serializable;

public interface Response extends Serializable {

    @Data
    class StandardResponse<T> implements Response {
        private String deviceId;
        private String commandId;
        private CommandExecStatusEnum commandExecStatusEnum;
        private T data;

        public StandardResponse(String deviceId) {
            this.deviceId = deviceId;
        }

        enum CommandExecStatusEnum {
            SUCCESS,
            FAILURE,
            ;
        }
    }

    @Data
    class HelloResponse extends StandardResponse<Void> {
        public HelloResponse(String deviceId) {
            super(deviceId);
        }
    }
}
