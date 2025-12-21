package wrapper.model.option;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DoubleOption implements Option {

    private final String optionName;

    private final double value;

}
