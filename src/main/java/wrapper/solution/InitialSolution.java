package wrapper.solution;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import wrapper.model.variable.Variable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.ObjDoubleConsumer;

@NoArgsConstructor
public class InitialSolution {

    private final Map<Variable, Double> initialValueByVariable = new HashMap<>();

    public static InitialSolution of(@NonNull final Map<Variable, Double> initialValueByVariable) {
        final InitialSolution initialSolution = new InitialSolution();
        initialValueByVariable.forEach(initialSolution::addVariable);
        return initialSolution;
    }

    public void consumeSolution(@NonNull final ObjDoubleConsumer<Variable> consumer) {
        this.initialValueByVariable.forEach(consumer::accept);
    }

    public void addVariable(@NonNull final Variable variable, double value) {
        this.initialValueByVariable.putIfAbsent(variable, value);
    }

    public int getNmbVariables() {
        return this.initialValueByVariable.size();
    }


}
