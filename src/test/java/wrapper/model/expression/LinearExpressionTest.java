package wrapper.model.expression;

import org.junit.jupiter.api.Test;
import wrapper.model.variable.Variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinearExpressionTest {

    @Test
    void getNmbCoefficients() throws LinearExpressionException {
        final LinearExpression linearExpression = new LinearExpression();
        linearExpression.addCoefficient(new Variable(4), 1.0);
        linearExpression.addCoefficient(new Variable(12), 0.5);

        assertEquals(2, linearExpression.getNmbCoefficients());
    }

    @Test
    void addCoefficientMustThrowIfSameVariableIndexAddedTwice() throws LinearExpressionException {
        final LinearExpression linearExpression = new LinearExpression();
        linearExpression.addCoefficient(new Variable(99), 1.0);

        final LinearExpressionException exception = assertThrows(LinearExpressionException.class, () -> linearExpression.addCoefficient(new Variable(99), 1.2));
        assertEquals("Variable with index 99 is already in linear expression", exception.getMessage());
    }

}