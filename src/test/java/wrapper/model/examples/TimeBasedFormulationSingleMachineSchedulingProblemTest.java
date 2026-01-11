package wrapper.model.examples;

import org.junit.jupiter.api.Test;
import wrapper.model.Model;
import wrapper.model.expression.LinearExpression;
import wrapper.model.expression.LinearExpressionException;
import wrapper.model.variable.Variable;
import wrapper.solution.Solution;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static wrapper.util.Constants.EPSILON;

class TimeBasedFormulationSingleMachineSchedulingProblemTest {

    static {
        System.loadLibrary("highs");
        System.loadLibrary("highswrap");
    }

    @Test
    void example() throws LinearExpressionException {
        // Instance.
        final Random random = new Random(0);
        final int nmbJobs = 10;
        final int nmbPeriods = 50;
        final int[] processingTimePerJob = new int[nmbJobs];
        final int[][] costPerJobPerPeriod = new int[nmbJobs][nmbPeriods];
        for (int j = 0; j < nmbJobs; ++j) {
            processingTimePerJob[j] = 10;
            for (int t = 0; t < nmbPeriods; ++t) {
                costPerJobPerPeriod[j][t] = random.nextInt(10, 20);
            }
        }
        // Model creation.
        final Model model = new Model();
        final Variable[][] x = new Variable[nmbJobs][nmbPeriods];
        for (int j = 0; j < nmbJobs; ++j) {
            for (int t = 0; t < nmbPeriods; ++t) {
                x[j][t] = model.addBinaryVariable(costPerJobPerPeriod[j][t]);
            }
        }
        // Jobs can only be processed once: For job j, \sum_{t}x_{j,t} <= 1.0.
        for (int j = 0; j < nmbJobs; ++j) {
            final LinearExpression expression = new LinearExpression();
            for (int t = 0; t < nmbPeriods; ++t) {
                expression.addCoefficient(x[j][t], 1.0);
            }
            model.addLessThanOrEqualToConstraint(1.0, expression);
        }
        // The machine can only process one job at a time: For period t, \sum_{j}\sum_{s}^{min(nmbPeriods, t + p_{j} - 1)}x_{j,s} <= 1.0.
        for (int t = 0; t < nmbPeriods; ++t) {
            final LinearExpression expression = new LinearExpression();
            for (int j = 0; j < nmbJobs; ++j) {
                for (int s = t; s < Math.min(nmbPeriods, t + processingTimePerJob[j] - 1); ++s) {
                    expression.addCoefficient(x[j][s], 1.0);
                }
            }
            model.addLessThanOrEqualToConstraint(1.0, expression);
        }

        final Solution solution = model.maximize().orElseThrow();

        assertTrue(solution.isFeasible());
        assertEquals(113.0, solution.getObjectiveValue(), EPSILON);
        int nmbScheduledJobs = 0;
        for (int j = 0; j < nmbJobs; ++j) {
            for (int t = 0; t < nmbPeriods; ++t) {
                if (solution.getVariableValue(x[j][t]) > 0.5) {
                    ++nmbScheduledJobs;
                }
            }
        }
        assertEquals(6, nmbScheduledJobs);
    }

}
