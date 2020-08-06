package com.qianan.akka.device;

import lombok.Data;

import java.io.Serializable;

public interface Response extends Serializable {

    @Data
    class StandardResponse<T> implements Response {
        private String commandId;
        private CommandExecStatusEnum commandExecStatusEnum;
        private T data;
        private String deviceId;

        enum CommandExecStatusEnum {
            SUCCESS,
            FAILURE,
            ;
        }
    }


}
