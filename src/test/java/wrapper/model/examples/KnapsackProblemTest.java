package wrapper.model.examples;

import org.junit.jupiter.api.Test;
import wrapper.model.Model;
import wrapper.model.expression.LinearExpression;
import wrapper.model.expression.LinearExpressionException;
import wrapper.model.variable.Variable;
import wrapper.solution.Solution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static wrapper.util.Constants.EPSILON;

class KnapsackProblemTest {

    static {
        System.loadLibrary("highs");
        System.loadLibrary("highswrap");
    }

    @Test
    void example() throws LinearExpressionException {
        // Instance.
        final int nmbItems = 5;
        final int capacity = 50;
        final double[] values = {1, 2, 3.5, 4.6, 7.2};
        final double[] weights = {0.5, 1, 4.5, 1.0, 4.3};
        assertEquals(nmbItems, weights.length);
        assertEquals(nmbItems, values.length);

        // Model creation.
        final Model model = new Model();
        // x[i] = number of times item i is picked.
        final Variable[] x = new Variable[nmbItems];
        for (int i = 0; i < nmbItems; ++i) {
            x[i] = model.addIntegerVariable(0.0, Double.MAX_VALUE, values[i]);
        }
        // Knapsack capacity constraint: \sum_{i}x_{i} <= capacity.
        final LinearExpression capacityExpression = new LinearExpression();
        for (int i = 0; i < nmbItems; ++i) {
            capacityExpression.addCoefficient(x[i], weights[i]);
        }
        model.addLessThanOrEqualToConstraint(capacity, capacityExpression);

        final Solution solution = model.maximize().orElseThrow();

        assertTrue(solution.isFeasible());
        assertEquals(230.0, solution.getObjectiveValue(), EPSILON);
        final double[] xExpectedValues = {0.0, 0.0, 0.0, 50.0, 0.0};
        for (int i = 0; i < nmbItems; ++i) {
            assertEquals(xExpectedValues[i], solution.getVariableValue(x[i]), EPSILON);
        }
    }

}
