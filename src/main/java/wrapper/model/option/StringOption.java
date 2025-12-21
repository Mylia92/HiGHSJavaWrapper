package wrapper.model.option;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StringOption implements Option {

    private final String optionName;

    private final String value;

}
