package wrapper.model.option;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import wrapper.model.Model;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonBooleanOptionsTest {

    static {
        System.loadLibrary("highs");
        System.loadLibrary("highswrap");
    }

    private static int computeNmbOptions() {
        return Arrays.stream(CommonBooleanOptions.values())
                .map(CommonBooleanOptions::getHighsOptionName)
                .collect(Collectors.toSet())
                .size();
    }

    @Test
    void commonBooleanOptionsMustNotHaveDuplicates() {
        assertEquals(CommonBooleanOptions.values().length, computeNmbOptions());
    }

    @ParameterizedTest
    @EnumSource(value = CommonBooleanOptions.class)
    void allCommonBooleanOptionsMustBeValidOptions(final CommonBooleanOptions commonBooleanOptions) throws OptionException {
        final Model model = new Model();

        assertTrue(model.addOption(commonBooleanOptions.getOption(true)));
    }

}
