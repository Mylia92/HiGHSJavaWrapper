package wrapper.model.examples;

import org.junit.jupiter.api.Test;
import wrapper.model.Model;
import wrapper.model.expression.LinearExpression;
import wrapper.model.expression.LinearExpressionException;
import wrapper.model.variable.Variable;
import wrapper.solution.Solution;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static wrapper.util.Constants.EPSILON;

class FacilityLocationProblemTest {

    static {
        System.loadLibrary("highs");
        System.loadLibrary("highswrap");
    }

    @Test
    void example() throws LinearExpressionException {
        // Instance.
        final Random random = new Random(0);
        final int nmbFacilities = 5;
        final int nmbCustomers = 33;
        final int[] openingCostPerFacility = new int[nmbFacilities];
        final int[][] shipCostPerFacilityPerCustomer = new int[nmbFacilities][nmbCustomers];
        final int[] capacityPerFacility = new int[nmbFacilities];
        final int[] demandPerCustomer = new int[nmbCustomers];
        for (int f = 0; f < nmbFacilities; ++f) {
            openingCostPerFacility[f] = random.nextInt(10_000, 50_000);
            capacityPerFacility[f] = random.nextInt(1_000, 1_200);
            for (int c = 0; c < nmbCustomers; ++c) {
                shipCostPerFacilityPerCustomer[f][c] = random.nextInt(1, 10);
            }
        }
        for (int c = 0; c < nmbCustomers; ++c) {
            demandPerCustomer[c] = random.nextInt(50, 70);
        }
        final double totalDemand = Arrays.stream(demandPerCustomer).sum();
        // Model creation.
        final Model model = new Model();
        // x[f] = 1 if facility f must be opened, 0 otherwise.
        final Variable[] x = new Variable[nmbFacilities];
        // y[f][c] = demand served to customer c by facility f.
        final Variable[][] y = new Variable[nmbFacilities][nmbCustomers];
        for (int f = 0; f < nmbFacilities; ++f) {
            x[f] = model.addBinaryVariable(openingCostPerFacility[f]);
            for (int c = 0; c < nmbCustomers; ++c) {
                y[f][c] = model.addIntegerVariable(0.0, Double.MAX_VALUE, shipCostPerFacilityPerCustomer[f][c]);
            }
        }
        // Facility capacity constraints.
        // For facility f, \sum_{c}y_{f,c} <= capacityPerFacility_{f}.
        for (int f = 0; f < nmbFacilities; ++f) {
            final LinearExpression expression = new LinearExpression();
            for (int c = 0; c < nmbCustomers; ++c) {
                expression.addCoefficient(y[f][c], 1.0);
            }
            model.addLessThanOrEqualToConstraint(capacityPerFacility[f], expression);
        }
        // Demand satisfaction constraints.
        // For customer c, \sum_{f}y_{f,c} == demandPerCustomer_{c}.
        for (int c = 0; c < nmbCustomers; ++c) {
            final LinearExpression expression = new LinearExpression();
            for (int f = 0; f < nmbFacilities; ++f) {
                expression.addCoefficient(y[f][c], 1.0);
            }
            model.addEqualityConstraint(demandPerCustomer[c], expression);
        }
        // Opening cost facility constraints.
        // For facility f, \sum_{c}y_{f,c} <= M.x_{f}, where M = totalDemand.
        for (int f = 0; f < nmbFacilities; ++f) {
            final LinearExpression expression = new LinearExpression();
            for (int c = 0; c < nmbCustomers; ++c) {
                expression.addCoefficient(y[f][c], 1.0);
            }
            expression.addCoefficient(x[f], -totalDemand);
            model.addLessThanOrEqualToConstraint(0.0, expression);
        }

        final Solution solution = model.minimize().orElseThrow();

        assertTrue(solution.isFeasible());
        assertEquals(52969.0, solution.getObjectiveValue(), EPSILON);
    }

}
