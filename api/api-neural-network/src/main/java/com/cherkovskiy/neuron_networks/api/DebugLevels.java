package com.cherkovskiy.neuron_networks.api;


//TODO: избавиться от кода
public enum DebugLevels {
    TRACE(0) {
        @Override
        public boolean doOut(long epochNumber) {
            return false;
        }
    },
    DEBUG(100) {
        @Override
        public boolean doOut(long epochNumber) {
            return false;
        }
    },
    DEBUG_EVERY_1000(101) {
        @Override
        public boolean doOut(long epochNumber) {
            return (epochNumber % 1000) == 0;
        }
    },
    DEBUG_EVERY_10_000(102) {
        @Override
        public boolean doOut(long epochNumber) {
            return (epochNumber % 10_000) == 0;
        }
    },
    DEBUG_EVERY_100_000(103) {
        @Override
        public boolean doOut(long epochNumber) {
            return (epochNumber % 100_000) == 0;
        }
    },
    DEBUG_EVERY_1_000_000(104) {
        @Override
        public boolean doOut(long epochNumber) {
            return (epochNumber % 1_000_000) == 0;
        }
    },
    INFO(200) {
        @Override
        public boolean doOut(long epochNumber) {
            return false;
        }
    },
    WARN(300) {
        @Override
        public boolean doOut(long epochNumber) {
            return false;
        }
    },
    ERROR(400) {
        @Override
        public boolean doOut(long epochNumber) {
            return false;
        }
    },
    OFF(500) {
        @Override
        public boolean doOut(long epochNumber) {
            return false;
        }
    };

    private final int level;

    DebugLevels(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isLessThanOrEqualTo(DebugLevels level) {
        return this.level <= level.getLevel();
    }

    public abstract boolean doOut(long epochNumber);
}
