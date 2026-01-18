package wrapper.model.option;

public enum CommonDoubleOptions {

    MIP_RELATIVE_GAP {
        String getHighsOptionName() {
            return "mip_rel_gap";
        }

        public Option getOption(double value) {
            return new DoubleOption(getHighsOptionName(), value);
        }
    },

    MIP_ABSOLUTE_GAP {
        String getHighsOptionName() {
            return "mip_abs_gap";
        }

        public Option getOption(double value) {
            return new DoubleOption(getHighsOptionName(), value);
        }
    },

    MIP_OBJECTIVE_VALUE_TARGET {
        String getHighsOptionName() {
            return "objective_target";
        }

        public Option getOption(double value) {
            return new DoubleOption(getHighsOptionName(), value);
        }
    },

    TIME_LIMIT {
        String getHighsOptionName() {
            return "time_limit";
        }

        public Option getOption(double value) {
            return new DoubleOption(getHighsOptionName(), value);
        }
    };

    abstract String getHighsOptionName();

    public abstract Option getOption(double value);

}
