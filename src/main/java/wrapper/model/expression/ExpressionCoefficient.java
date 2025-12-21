package wrapper.model.expression;

import lombok.NonNull;
import wrapper.model.variable.Variable;

public record ExpressionCoefficient(@NonNull Variable variable, double value) {
}
