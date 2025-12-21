package wrapper.model.expression;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import wrapper.model.variable.Variable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
public class LinearExpression {

    private final Set<Integer> variableIndices = new HashSet<>();

    @Getter
    private final List<ExpressionCoefficient> coefficients = new ArrayList<>();

    public static LinearExpression of(final ExpressionCoefficient... coefficients) throws LinearExpressionException {
        final LinearExpression expression = new LinearExpression();
        for (final ExpressionCoefficient coefficient : coefficients) {
            expression.addCoefficient(coefficient.variable(), coefficient.value());
        }
        return expression;
    }

    public void addCoefficient(@NonNull final Variable variable, double coefficient) throws LinearExpressionException {
        if (!this.variableIndices.add(variable.index())) {
            throw new LinearExpressionException(String.format("Variable with index %d is already in linear expression", variable.index()));
        }
        this.coefficients.add(new ExpressionCoefficient(variable, coefficient));
    }

    public int getNmbNonZeros() {
        return this.coefficients.size();
    }

}
