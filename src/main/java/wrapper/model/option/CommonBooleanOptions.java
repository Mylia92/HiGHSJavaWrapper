package wrapper.model.option;

public enum CommonBooleanOptions {

    SOLVER_OUTPUT {
        String getHighsOptionName() {
            return "output_flag";
        }
    },

    MIP_DETECT_SYMMETRY {
        String getHighsOptionName() {
            return "mip_detect_symmetry";
        }
    },

    MIP_ALLOW_RESTART {
        String getHighsOptionName() {
            return "mip_allow_restart";
        }
    };

    abstract String getHighsOptionName();

    public Option getOption(boolean value) {
        return new BooleanOption(getHighsOptionName(), value);
    }

}
