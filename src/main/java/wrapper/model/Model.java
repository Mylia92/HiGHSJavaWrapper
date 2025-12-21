package wrapper.model;


import highs.*;
import lombok.NonNull;
import wrapper.model.constraint.Constraint;
import wrapper.model.constraint.ConstraintException;
import wrapper.model.constraint.ConstraintType;
import wrapper.model.expression.ExpressionCoefficient;
import wrapper.model.expression.LinearExpression;
import wrapper.model.option.*;
import wrapper.model.variable.Variable;
import wrapper.model.variable.VariableException;
import wrapper.solution.Solution;

import java.util.List;
import java.util.Optional;


public class Model {

    private static final String OUTPUT_FLAG_OPTION = "output_flag";

    private final Highs highs = new Highs();

    public Model() {
        this.highs.setOptionValue(OUTPUT_FLAG_OPTION, false);
    }

    public boolean addOption(@NonNull final Option option) throws OptionException {
        switch (option) {
            case StringOption stringOption -> {
                return this.highs.setOptionValue(option.getOptionName(), stringOption.getValue()) == HighsStatus.kOk;
            }
            case BooleanOption booleanOption -> {
                return this.highs.setOptionValue(option.getOptionName(), booleanOption.getValue()) == HighsStatus.kOk;
            }
            case DoubleOption doubleOption -> {
                return this.highs.setOptionValue(option.getOptionName(), doubleOption.getValue()) == HighsStatus.kOk;
            }
            case IntegerOption integerOption -> {
                return this.highs.setOptionValue(option.getOptionName(), integerOption.getValue()) == HighsStatus.kOk;
            }
            default -> throw new OptionException("Option is not supported");
        }
    }

    public Variable addContinuousVariable(double lb, double ub, double cost) {
        this.highs.addCol(cost, lb, ub, 0, null, null);
        return new Variable(this.highs.getNumCols() - 1);
    }

    public Variable addBinaryVariable(double cost) {
        return addIntegerVariable(0.0, 1.0, cost);
    }

    public Variable addIntegerVariable(double lb, double ub, double cost) {
        this.highs.addCol(cost, lb, ub, 0, null, null);
        final int variableIndex = this.highs.getNumCols() - 1;
        this.highs.changeColIntegrality(variableIndex, HighsVarType.kInteger);
        return new Variable(variableIndex);
    }

    public void updateVariableCost(double newCost, @NonNull final Variable variable) {
        checkVariable(variable);
        this.highs.changeColCost(variable.index(), newCost);
    }

    public void updateVariableBounds(double lb, double ub, @NonNull final Variable variable) {
        checkVariable(variable);
        this.highs.changeColBounds(variable.index(), lb, ub);
    }

    public void updateConstraintCoefficient(@NonNull final ExpressionCoefficient newCoefficient, @NonNull final Constraint constraint) throws ConstraintException {
        checkConstraint(constraint);
        final Variable variable = newCoefficient.variable();
        checkVariable(variable);
        this.highs.changeCoeff(constraint.index(), variable.index(), newCoefficient.value());
    }

    public void updateConstraintRightHandSide(double rhs, @NonNull final Constraint constraint) throws ConstraintException {
        checkConstraint(constraint);
        switch (constraint.type()) {
            case EQUALITY -> this.highs.changeRowBounds(constraint.index(), rhs, rhs);
            case GREATER_THAN_OR_EQUAL_TO -> this.highs.changeRowBounds(constraint.index(), rhs, Double.MAX_VALUE);
            case LESS_THAN_OR_EQUAL_TO -> this.highs.changeRowBounds(constraint.index(), -Double.MAX_VALUE, rhs);
        }
    }

    /**
     * LinearExpression = RHS. Example: 2x1 + 5x2 = 4.
     */
    public Constraint addEqualityConstraint(double rhs, @NonNull final LinearExpression linearExpression) {
        return addConstraint(rhs, rhs, linearExpression, ConstraintType.EQUALITY);
    }

    /**
     * LinearExpression <= RHS. Example: 2x1 + 5x2 <= 4.
     */
    public Constraint addLessThanOrEqualToConstraint(double rhs, @NonNull final LinearExpression linearExpression) {
        return addConstraint(-Double.MAX_VALUE, rhs, linearExpression, ConstraintType.LESS_THAN_OR_EQUAL_TO);
    }

    /**
     * LinearExpression >= RHS. Example: 2x1 + 5x2 >= 4.
     */
    public Constraint addGreaterThanOrEqualToConstraint(double rhs, @NonNull final LinearExpression linearExpression) {
        return addConstraint(rhs, Double.MAX_VALUE, linearExpression, ConstraintType.GREATER_THAN_OR_EQUAL_TO);
    }

    public Optional<Solution> minimize() {
        this.highs.changeObjectiveSense(ObjSense.kMinimize);
        return solve();
    }

    public Optional<Solution> maximize() {
        this.highs.changeObjectiveSense(ObjSense.kMaximize);
        return solve();
    }

    private Optional<Solution> solve() {
        if (this.highs.run() == HighsStatus.kError) {
            return Optional.empty();
        }
        return Optional.of(new Solution(this.highs.getSolution(), this.highs.getModelStatus(), this.highs.getObjectiveValue()));
    }

    private Constraint addConstraint(double lhs, double rhs, final LinearExpression linearExpression, final ConstraintType constraintType) {
        checkLinearExpression(linearExpression);
        final int nmbNonZeros = linearExpression.getNmbNonZeros();
        final DoubleArray values = new DoubleArray(nmbNonZeros);
        final IntegerArray indices = new IntegerArray(nmbNonZeros);
        final List<ExpressionCoefficient> coefficients = linearExpression.getCoefficients();
        for (int i = 0; i < nmbNonZeros; ++i) {
            final ExpressionCoefficient coefficient = coefficients.get(i);
            values.setitem(i, coefficient.value());
            indices.setitem(i, coefficient.variable().index());
        }
        this.highs.addRow(lhs, rhs, nmbNonZeros, indices.cast(), values.cast());
        return new Constraint(this.highs.getNumRows() - 1, constraintType);
    }

    private void checkLinearExpression(final LinearExpression linearExpression) {
        final int nmbNonZeros = linearExpression.getNmbNonZeros();
        final List<ExpressionCoefficient> coefficients = linearExpression.getCoefficients();
        for (int i = 0; i < nmbNonZeros; ++i) {
            checkVariable(coefficients.get(i).variable());
        }
    }

    private void checkVariable(final Variable variable) throws VariableException {
        if (variable.index() >= this.highs.getNumCols()) {
            throw new VariableException(String.format("Variable with index %d does not exist in the model", variable.index()));
        }
    }

    private void checkConstraint(final Constraint constraint) throws ConstraintException {
        if (constraint.index() >= this.highs.getNumRows()) {
            throw new ConstraintException(String.format("Constraint with index %d does not exist in the model", constraint.index()));
        }
    }

}
