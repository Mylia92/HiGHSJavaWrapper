package wrapper.model.option;

public enum CommonBooleanOptions {

    SOLVER_OUTPUT {
        String getHighsOptionName() {
            return "output_flag";
        }

        public Option getOption(boolean value) {
            return new BooleanOption(getHighsOptionName(), value);
        }
    },

    MIP_DETECT_SYMMETRY {
        String getHighsOptionName() {
            return "mip_detect_symmetry";
        }

        public Option getOption(boolean value) {
            return new BooleanOption(getHighsOptionName(), value);
        }
    },

    MIP_ALLOW_RESTART {
        String getHighsOptionName() {
            return "mip_allow_restart";
        }

        public Option getOption(boolean value) {
            return new BooleanOption(getHighsOptionName(), value);
        }
    };

    abstract String getHighsOptionName();

    public abstract Option getOption(boolean value);

}
