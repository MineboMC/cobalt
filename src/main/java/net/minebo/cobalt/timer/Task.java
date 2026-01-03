package net.minebo.cobalt.timer;

import lombok.AllArgsConstructor;
import lombok.Data;

    @Data
    @AllArgsConstructor
    public class Task {

        private final int taskId;
        private final long time;
    }