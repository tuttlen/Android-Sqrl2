package com.tuttlen.android_sqrl;

/**
 * Created by nathan on 4/2/16.
 */
public enum TIF {
    ID_MATCH(0x01) {
        @Override
        public String tifResult() {
            return "(Current) ID match";
        }
    },

    PREVIOUS__ID_MATCH(0x02) {
        @Override
        public String tifResult() {
            return "Previous ID match";
        }
    },
    IPS_MATCH(0x04) {
        @Override
        public String tifResult() {
            return "IPs Match";
        }
    },
    SQRL_disabled(0x08) {
        @Override
        public String tifResult() {
            return "SQRL Disabled";
        }
    },
    NOT_SUPPORTED(0x10) {
        @Override
        public String tifResult() {
            return "Function(s) not supported";
        }
    },
    TRANSIENT_ERR(0x20) {
        @Override
        public String tifResult() {
            return "Transient error";
        }
    },
    COMMAND_FAILED(0x40) {
        @Override
        public String tifResult() {
            return "Command failed";
        }
    },
    CLIENT_FAILURE(0x80) {
        @Override
        public String tifResult() {
            return "Client failure";
        }
    },
    BAD_ID_ASOC(0x100) {
        @Override
        public String tifResult() {
            return "Bad ID Association";
        }
    };

    public abstract String tifResult();

    public int value;

    private TIF(int value) {
        this.value = value;
    }

}
