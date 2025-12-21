package wrapper.model.option;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IntegerOption implements Option {

    private final String optionName;

    private final int value;

}
