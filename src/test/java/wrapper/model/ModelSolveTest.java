package wrapper.model;

import org.junit.jupiter.api.Test;
import wrapper.model.expression.ExpressionCoefficient;
import wrapper.model.expression.LinearExpression;
import wrapper.model.expression.LinearExpressionException;
import wrapper.model.variable.Variable;
import wrapper.solution.Solution;

import static org.junit.jupiter.api.Assertions.*;
import static wrapper.util.Constants.EPSILON;

class ModelSolveTest {

    static {
        System.loadLibrary("highs");
        System.loadLibrary("highswrap");
    }

    @Test
    void minimize() {
        final Model model = new Model();
        model.addContinuousVariable(1.2, 7.0, 1.0);
        model.addContinuousVariable(0.5, 4.0, 1.0);

        final Solution solution = model.minimize().orElseThrow();

        assertTrue(solution.isFeasible());
        assertEquals(1.7, solution.getObjectiveValue(), EPSILON);
    }

    @Test
    void maximize() {
        final Model model = new Model();
        model.addContinuousVariable(0.0, 3.0, 1.0);
        model.addContinuousVariable(0.0, 2.9, 1.0);

        final Solution solution = model.maximize().orElseThrow();

        assertTrue(solution.isFeasible());
        assertEquals(5.9, solution.getObjectiveValue());
    }

    @Test
    void maximizeWithSimpleConstraint() throws LinearExpressionException {
        final Model model = new Model();
        final LinearExpression linearExpression = LinearExpression.of(
                new ExpressionCoefficient(model.addContinuousVariable(0.0, Double.MAX_VALUE, 5.5), 1.0),
                new ExpressionCoefficient(model.addContinuousVariable(0.5, Double.MAX_VALUE, 1.0), 1.0)
        );
        model.addEqualityConstraint(1.0, linearExpression);

        final Solution solution = model.maximize().orElseThrow();

        assertTrue(solution.isFeasible());
        assertEquals(3.25, solution.getObjectiveValue(), EPSILON);
    }

    @Test
    void maximizeWithThreeConstraints() throws LinearExpressionException {
        final Model model = new Model();
        final Variable x1 = model.addContinuousVariable(0.0, 1.0, 1.0);
        final Variable x2 = model.addContinuousVariable(0.0, 12.0, 5.0);
        final Variable x3 = model.addContinuousVariable(0.0, 5.0, 14.0);
        model.addLessThanOrEqualToConstraint(7.0, LinearExpression.of(new ExpressionCoefficient(x1, 0.5), new ExpressionCoefficient(x3, 14.0)));
        model.addEqualityConstraint(2.0, LinearExpression.of(new ExpressionCoefficient(x2, 12.4), new ExpressionCoefficient(x3, 0.2)));

        final Solution solution = model.maximize().orElseThrow();

        assertTrue(solution.isFeasible());
        assertEquals(8.2690092166, solution.getObjectiveValue(), EPSILON);
    }

    @Test
    void maximizeWithBinaryVariables() throws LinearExpressionException {
        final Model model = new Model();
        final Variable x1 = model.addBinaryVariable(1.2);
        final Variable x2 = model.addBinaryVariable(1.3);
        model.addLessThanOrEqualToConstraint(1.2, LinearExpression.of(new ExpressionCoefficient(x1, 1.0), new ExpressionCoefficient(x2, 1.0)));

        final Solution solution = model.maximize().orElseThrow();

        assertTrue(solution.isFeasible());
        assertEquals(1.3, solution.getObjectiveValue(), EPSILON);
    }

    @Test
    void maximizeMustFailDueToInfeasibilityOnIntegralityConstraints() throws LinearExpressionException {
        final Model model = new Model();
        final Variable x1 = model.addBinaryVariable(1.0);
        final Variable x2 = model.addBinaryVariable(1.0);
        model.addEqualityConstraint(1.5, LinearExpression.of(new ExpressionCoefficient(x1, 1.0), new ExpressionCoefficient(x2, 1.0)));

        final Solution solution = model.maximize().orElseThrow();

        assertFalse(solution.isFeasible());
    }

    @Test
    void binaryVariablesMustHaveTheExpectedValues() throws LinearExpressionException {
        final Model model = new Model();
        final Variable x1 = model.addIntegerVariable(0.0, 3.0, 2.0);
        final Variable x2 = model.addIntegerVariable(0.0, Double.MAX_VALUE, 8.0);
        final Variable x3 = model.addIntegerVariable(2.0, Double.MAX_VALUE, 15.0);
        model.addGreaterThanOrEqualToConstraint(4.5, LinearExpression.of(
                new ExpressionCoefficient(x1, 0.5),
                new ExpressionCoefficient(x2, 1.0),
                new ExpressionCoefficient(x3, 1.0)
        ));

        final Solution solution = model.minimize().orElseThrow();

        assertEquals(3.0, solution.getVariableValue(x1), EPSILON);
        assertEquals(1.0, solution.getVariableValue(x2), EPSILON);
        assertEquals(2.0, solution.getVariableValue(x3), EPSILON);
    }

    @Test
    void successiveCallsToSolverMustLeadToDifferentSolutions() throws LinearExpressionException {
        final Model model = new Model();
        final Variable x1 = model.addContinuousVariable(0.0, Double.MAX_VALUE, 2.0);
        final Variable x2 = model.addIntegerVariable(0.0, Double.MAX_VALUE, 1.0);
        model.addLessThanOrEqualToConstraint(5.0, LinearExpression.of(
                new ExpressionCoefficient(x1, 1.0),
                new ExpressionCoefficient(x2, 1.0)
        ));

        final Solution firstSolution = model.maximize().orElseThrow();
        assertTrue(firstSolution.isFeasible());
        assertEquals(10.0, firstSolution.getObjectiveValue(), EPSILON);
        assertEquals(5.0, firstSolution.getVariableValue(x1));
        assertEquals(0.0, firstSolution.getVariableValue(x2));

        final Variable x3 = model.addIntegerVariable(1.0, Double.MAX_VALUE, 1.0);
        model.addEqualityConstraint(3.0, LinearExpression.of(
                new ExpressionCoefficient(x1, 1.0),
                new ExpressionCoefficient(x2, 1.0),
                new ExpressionCoefficient(x3, 1.0)
        ));

        final Solution secondSolution = model.maximize().orElseThrow();
        assertTrue(secondSolution.isFeasible());
        assertEquals(5.0, secondSolution.getObjectiveValue(), EPSILON);
        assertEquals(2.0, firstSolution.getVariableValue(x1));
        assertEquals(0.0, firstSolution.getVariableValue(x2));
        assertEquals(1.0, secondSolution.getVariableValue(x3));
    }

}
