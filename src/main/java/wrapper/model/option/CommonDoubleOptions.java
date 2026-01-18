package wrapper.model.option;

public enum CommonDoubleOptions {

    MIP_RELATIVE_GAP {
        String getHighsOptionName() {
            return "mip_rel_gap";
        }
    },

    MIP_ABSOLUTE_GAP {
        String getHighsOptionName() {
            return "mip_abs_gap";
        }
    },

    MIP_OBJECTIVE_VALUE_TARGET {
        String getHighsOptionName() {
            return "objective_target";
        }
    },

    TIME_LIMIT {
        String getHighsOptionName() {
            return "time_limit";
        }
    };

    abstract String getHighsOptionName();

    public Option getOption(double value) {
        return new DoubleOption(getHighsOptionName(), value);
    }
}
