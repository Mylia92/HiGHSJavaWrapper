package wrapper.model.option;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import wrapper.model.Model;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonDoubleOptionsTest {

    static {
        System.loadLibrary("highs");
        System.loadLibrary("highswrap");
    }

    private static int computeNmbOptions() {
        return Arrays.stream(CommonDoubleOptions.values())
                .map(CommonDoubleOptions::getHighsOptionName)
                .collect(Collectors.toSet())
                .size();
    }

    @Test
    void commonDoubleOptionsMustNotHaveDuplicates() {
        assertEquals(CommonDoubleOptions.values().length, computeNmbOptions());
    }

    @ParameterizedTest
    @EnumSource(value = CommonDoubleOptions.class)
    void allCommonDoubleOptionsMustBeValidOptions(final CommonDoubleOptions commonDoubleOptions) throws OptionException {
        final Model model = new Model();

        System.out.println(commonDoubleOptions.toString());
        assertTrue(model.addOption(commonDoubleOptions.getOption(35)));
    }

}
