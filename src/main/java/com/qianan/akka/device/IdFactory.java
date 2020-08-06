package com.qianan.akka.device;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public interface IdFactory {
    String id();

    class IncrementIdFactory implements IdFactory {
        private final AtomicInteger counter;
        public static final IdFactory instance = new IncrementIdFactory();

        public IncrementIdFactory() {
            this.counter = new AtomicInteger(0);
        }

        public String id() {
            return String.valueOf(counter.getAndIncrement());
        }
    }

    class UUIDIdFactory implements IdFactory {
        public static final IdFactory instance = new UUIDIdFactory();

        public String id() {
            return UUID.randomUUID().toString();
        }
    }
}
