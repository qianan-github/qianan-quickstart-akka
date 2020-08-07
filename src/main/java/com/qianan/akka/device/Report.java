package com.qianan.akka.device;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 设备主动上报的数据
 * */
public interface Report extends Serializable {
    @Data
    @ToString
    class StandardReport implements Report {
        //设备id
        private String deviceId;
        //设备上报时间
        private long reportTime;
        //中控id
        private String centerControllerDeviceId;
        //中控上报时间
        private long centerControllerReportTime;

        public StandardReport(String deviceId) {
            this.deviceId = deviceId;
            this.reportTime = System.currentTimeMillis();
        }

        public boolean isCenterController() {
            return Objects.equals(deviceId, centerControllerDeviceId);
        }
    }

    @Data
    @ToString(callSuper = true)
    class HeartBeat extends StandardReport {
        public HeartBeat(String deviceId) {
            super(deviceId);
        }
    }

    @Data
    @ToString(callSuper = true)
    class LockPwdReport extends StandardReport {
        //门锁密码列表
        private List<String> pwds;

        public LockPwdReport(String deviceId, List<String> pwds) {
            super(deviceId);
            this.pwds = pwds;
        }
    }

    @Data
    @ToString(callSuper = true)
    class LockPowerReport extends StandardReport {
        //电池电量
        private int power;

        public LockPowerReport(String deviceId, int power) {
            super(deviceId);
            this.power = power;
        }
    }

    @Data
    @ToString(callSuper = true)
    class ElectricSwitchReport extends StandardReport {
        //电闸状态
        private SwitchStatus switchStatus;

        public ElectricSwitchReport(String deviceId, SwitchStatus switchStatus) {
            super(deviceId);
            this.switchStatus = switchStatus;
        }

        enum SwitchStatus {
            CLOSED,
            OPENED,
            ;
        }
    }

    @Data
    @ToString(callSuper = true)
    class ElectricDayFreezeReport extends StandardReport {
        //电表日冻结读数
        private int reading;

        public ElectricDayFreezeReport(String deviceId, int reading) {
            super(deviceId);
            this.reading = reading;
        }
    }
}
