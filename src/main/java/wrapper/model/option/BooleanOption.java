package wrapper.model.option;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class BooleanOption implements Option {

    @Getter
    private final String optionName;

    private final boolean value;

    public boolean getValue() {
        return value;
    }

}
