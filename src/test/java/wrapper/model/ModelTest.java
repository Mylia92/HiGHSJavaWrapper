package wrapper.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import wrapper.model.constraint.Constraint;
import wrapper.model.constraint.ConstraintException;
import wrapper.model.constraint.ConstraintType;
import wrapper.model.expression.ExpressionCoefficient;
import wrapper.model.expression.LinearExpression;
import wrapper.model.expression.LinearExpressionException;
import wrapper.model.option.*;
import wrapper.model.variable.Variable;
import wrapper.model.variable.VariableException;
import wrapper.solution.Solution;

import static org.junit.jupiter.api.Assertions.*;
import static wrapper.util.Constants.EPSILON;

class ModelTest {

    static {
        System.loadLibrary("highs");
        System.loadLibrary("highswrap");
    }

    @Test
    void addOptionMustThrowForUnknownOptionType() {
        class UnknowOption implements Option {
            @Override
            public String getOptionName() {
                return "UnknowOption";
            }
        }
        final Model model = new Model();

        final OptionException exception = assertThrows(OptionException.class, () -> model.addOption(new UnknowOption()));
        assertEquals("Option is not supported", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "parallel, on, true",
            "unknown_option, true, false"
    })
    void addStringOption(final String optionName, final String optionValue, final boolean returnValue) throws OptionException {
        final Model model = new Model();

        assertEquals(returnValue, model.addOption(new StringOption(optionName, optionValue)));
    }

    @ParameterizedTest
    @CsvSource({
            "mip_detect_symmetry, true, true",
            "unknown_option, true, false"
    })
    void addBooleanOption(final String optionName, final boolean optionValue, final boolean returnValue) throws OptionException {
        final Model model = new Model();

        assertEquals(returnValue, model.addOption(new BooleanOption(optionName, optionValue)));
    }

    @ParameterizedTest
    @CsvSource({
            "time_limit, 65.4, true",
            "unknown_option, 12.1, false"
    })
    void addDoubleOption(final String optionName, final double optionValue, final boolean returnValue) throws OptionException {
        final Model model = new Model();

        assertEquals(returnValue, model.addOption(new DoubleOption(optionName, optionValue)));
    }

    @ParameterizedTest
    @CsvSource({
            "threads, 4, true",
            "unknown_option, -1, false"
    })
    void addIntegerOption(final String optionName, final int optionValue, final boolean returnValue) throws OptionException {
        final Model model = new Model();

        assertEquals(returnValue, model.addOption(new IntegerOption(optionName, optionValue)));
    }

    @Test
    void addVariable() {
        final Model model = new Model();

        assertEquals(0, model.addContinuousVariable(14.2, 18.5, 15.0).index());
        assertEquals(1, model.addIntegerVariable(0.0, 5.2, 1.0).index());
        assertEquals(2, model.addBinaryVariable(0.0).index());
    }

    @Test
    void updateVariableCostMustChangeObjectiveValue() {
        final Model model = new Model();
        model.addContinuousVariable(1.2, 18.5, 2.3);
        final Variable x2 = model.addContinuousVariable(0.0, 10.0, 1.0);

        final Solution firstSolution = model.maximize().orElseThrow();
        assertEquals(52.55, firstSolution.getObjectiveValue(), EPSILON);

        model.updateVariableCost(2.3, x2);

        final Solution secondSolution = model.maximize().orElseThrow();
        assertEquals(65.55, secondSolution.getObjectiveValue(), EPSILON);
    }

    @Test
    void updateVariableCostMustThrowForUnknownVariable() {
        final Model model = new Model();
        model.addContinuousVariable(0.0, Double.MAX_VALUE, 1.0);
        model.addContinuousVariable(0.0, Double.MAX_VALUE, 1.0);
        final Variable fictitiousVariable = new Variable(2);

        final VariableException exception = assertThrows(VariableException.class, () -> model.updateVariableCost(5.0, fictitiousVariable));
        assertEquals("Variable with index 2 does not exist in the model", exception.getMessage());
    }

    @Test
    void updateVariableBoundsMustChangeObjectiveValue() {
        final Model model = new Model();
        final Variable x1 = model.addContinuousVariable(1.0, 2.0, 1.0);

        final Solution firstSolution = model.minimize().orElseThrow();
        assertEquals(1.0, firstSolution.getObjectiveValue(), EPSILON);

        model.updateVariableBounds(15.0, 35.0, x1);

        final Solution secondSolution = model.minimize().orElseThrow();
        assertEquals(15.0, secondSolution.getObjectiveValue(), EPSILON);
    }

    @Test
    void updateVariableBoundsMustThrowForUnknownVariable() {
        final Model model = new Model();
        model.addContinuousVariable(1.0, 2.0, 1.0);
        final Variable fictitiousVariable = new Variable(14);

        final VariableException exception = assertThrows(VariableException.class, () -> model.updateVariableBounds(5.0, 5.1, fictitiousVariable));
        assertEquals("Variable with index 14 does not exist in the model", exception.getMessage());
    }

    @Test
    void updateConstraintCoefficient() throws ConstraintException, LinearExpressionException {
        final Model model = new Model();
        final Variable x1 = model.addBinaryVariable(1.0);
        final Variable x2 = model.addBinaryVariable(1.0);
        final Constraint constraint = model.addLessThanOrEqualToConstraint(1.0, LinearExpression.of(new ExpressionCoefficient(x1, 1.0)));

        final Solution firstSolution = model.maximize().orElseThrow();
        assertEquals(2.0, firstSolution.getObjectiveValue(), EPSILON);

        model.updateConstraintCoefficient(new ExpressionCoefficient(x2, 1.0), constraint);

        final Solution secondSolution = model.maximize().orElseThrow();
        assertEquals(1.0, secondSolution.getObjectiveValue(), EPSILON);
    }

    @Test
    void updateConstraintCoefficientMustThrowForUnknownVariable() throws LinearExpressionException {
        final Model model = new Model();
        final Variable x1 = model.addBinaryVariable(1.0);
        final Constraint constraint = model.addLessThanOrEqualToConstraint(4.0, LinearExpression.of(new ExpressionCoefficient(x1, 0.5)));
        final ExpressionCoefficient newExpressionCoefficient = new ExpressionCoefficient(new Variable(12), 0.5);

        final VariableException exception = assertThrows(VariableException.class, () -> model.updateConstraintCoefficient(newExpressionCoefficient, constraint));
        assertEquals("Variable with index 12 does not exist in the model", exception.getMessage());
    }

    @Test
    void updateConstraintCoefficientMustThrowForUnknownConstraint() {
        final Model model = new Model();
        final Variable x1 = model.addBinaryVariable(1.0);

        final ConstraintException exception = assertThrows(ConstraintException.class, () -> model.updateConstraintCoefficient(new ExpressionCoefficient(x1, 0.5), new Constraint(0, ConstraintType.EQUALITY)));
        assertEquals("Constraint with index 0 does not exist in the model", exception.getMessage());
    }

    @Test
    void updateConstraintRightHandSideForEqualityConstraint() throws ConstraintException, LinearExpressionException {
        final Model model = new Model();
        final Variable x1 = model.addContinuousVariable(0.0, Double.MAX_VALUE, 1.0);
        final Variable x2 = model.addContinuousVariable(0.0, Double.MAX_VALUE, 1.0);
        final Constraint constraint = model.addEqualityConstraint(1.0, LinearExpression.of(new ExpressionCoefficient(x1, 1.0), new ExpressionCoefficient(x2, 1.0)));
        model.updateConstraintRightHandSide(18.0, constraint);

        final Solution solution = model.maximize().orElseThrow();

        assertEquals(18.0, solution.getObjectiveValue(), EPSILON);
    }

    @Test
    void updateConstraintRightHandSideMustThrowForUnknowConstraint() {
        final Model model = new Model();

        final ConstraintException exception = assertThrows(ConstraintException.class, () -> model.updateConstraintRightHandSide(37.0, new Constraint(1, ConstraintType.GREATER_THAN_OR_EQUAL_TO)));
        assertEquals("Constraint with index 1 does not exist in the model", exception.getMessage());
    }

    @Test
    void updateConstraintRightHandSideForLessThanOrEqualToConstraint() throws ConstraintException, LinearExpressionException {
        final Model model = new Model();
        final Variable x1 = model.addContinuousVariable(0.0, Double.MAX_VALUE, 1.0);
        final Variable x2 = model.addContinuousVariable(0.0, Double.MAX_VALUE, 1.0);
        final Constraint constraint = model.addLessThanOrEqualToConstraint(10.0, LinearExpression.of(new ExpressionCoefficient(x1, 1.0), new ExpressionCoefficient(x2, 1.0)));
        model.updateConstraintRightHandSide(37.0, constraint);

        final Solution solution = model.maximize().orElseThrow();

        assertEquals(37.0, solution.getObjectiveValue(), EPSILON);
    }

    @Test
    void updateConstraintRightHandSideForGreaterThanOrEqualToConstraint() throws ConstraintException, LinearExpressionException {
        final Model model = new Model();
        final Variable x1 = model.addIntegerVariable(12.0, Double.MAX_VALUE, 2.0);
        final Variable x2 = model.addContinuousVariable(0.0, Double.MAX_VALUE, 1.0);
        final Constraint constraint = model.addGreaterThanOrEqualToConstraint(20.0, LinearExpression.of(new ExpressionCoefficient(x1, 1.0), new ExpressionCoefficient(x2, 1.0)));
        model.updateConstraintRightHandSide(12.0, constraint);

        final Solution solution = model.minimize().orElseThrow();

        assertEquals(24.0, solution.getObjectiveValue(), EPSILON);
    }

    @Test
    void addConstraint() throws LinearExpressionException {
        final Model model = new Model();
        final LinearExpression expression = new LinearExpression();
        expression.addCoefficient(model.addContinuousVariable(1.0, 2.0, 0.0), 1.0);

        assertEquals(0, model.addLessThanOrEqualToConstraint(50.0, expression).index());
        assertEquals(1, model.addEqualityConstraint(25.0, expression).index());
        assertEquals(2, model.addGreaterThanOrEqualToConstraint(1.9, expression).index());
    }

    @Test
    void addConstraintMustThrowIfLinearExpressionContainsUnknownVariable() throws LinearExpressionException {
        final Model model = new Model();
        final LinearExpression expression = new LinearExpression();
        expression.addCoefficient(new Variable(0), 1.0);

        final VariableException exception = assertThrows(VariableException.class, () -> model.addEqualityConstraint(2.4, expression));
        assertEquals("Variable with index 0 does not exist in the model", exception.getMessage());
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