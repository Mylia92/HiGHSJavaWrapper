package wrapper.model.examples;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import wrapper.model.Model;
import wrapper.model.expression.LinearExpression;
import wrapper.model.expression.LinearExpressionException;
import wrapper.model.variable.Variable;
import wrapper.solution.Solution;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static wrapper.util.Constants.EPSILON;

class NonLinearWorkloadBalancingProblemTest {

    static {
        System.loadLibrary("highs");
        System.loadLibrary("highswrap");
    }

    private static double getExpectedObjectiveValue(final int balancingExponent) {
        return switch (balancingExponent) {
            case 1 -> 11.857535578957105D;
            case 2 -> 14.12605088342788D;
            case 4 -> 19.982989907106784D;
            default -> 0D;
        };
    }

    private static double[] getExpectedWorkload(final int balancingExponent) {
        return switch (balancingExponent) {
            case 1 ->
                    new double[]{1.300923212471342, 0.8940673518376931, 1.2089472813509425, 1.0348759812058683, 1.033571377809991,
                            0.9940890521423804, 1.326022002567542, 1.4225633880568358, 1.4534749951857489, 1.1890009363287601};
            case 2 ->
                    new double[]{1.1968452125678246, 1.1227704332368662, 1.1748694309993277, 1.1773497825296424, 1.164246094195359,
                            1.1499580310658608, 1.2068654772399423, 1.2574023637754894, 1.2309511662194372, 1.1982617006710863};
            case 4 ->
                    new double[]{1.1903019053464934, 1.1652043837219297, 1.182708710299297, 1.1854446864050803, 1.1804241157039228,
                            1.1757567427277709, 1.1952650279835828, 1.216007344004657, 1.2033220819039758, 1.1927597456341912};
            default -> null;
        };
    }

    private static boolean compareWorkloads(final double[] expectedWorkload, final double[] computedWorkload) {
        if (expectedWorkload.length != computedWorkload.length || expectedWorkload.length == 0) {
            return false;
        }
        for (int m = 0; m < expectedWorkload.length; ++m) {
            if (Math.abs(expectedWorkload[m] - computedWorkload[m]) > EPSILON) {
                return false;
            }
        }
        return true;
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4})
    void example(final int balancingExponent) throws LinearExpressionException {
        final WorkloadBalancing workloadBalancing = new WorkloadBalancing(balancingExponent);

        final Solution solution = workloadBalancing.solve();

        assertTrue(solution.isFeasible());
        assertEquals(getExpectedObjectiveValue(balancingExponent), solution.getObjectiveValue(), EPSILON);
        final double[] expectedWorkload = getExpectedWorkload(balancingExponent);
        assertNotNull(expectedWorkload);
        final double[] computedWorkload = workloadBalancing.getWorkload(solution);
        assertTrue(compareWorkloads(expectedWorkload, computedWorkload));
    }

    private static class WorkloadBalancing {

        private static final double RELATIVE_GAP_TARGET = 1E-7;

        // Instance data.
        private final int nmbMachines = 10;
        private final int nmbProducts = 500;
        private final double[] capacityPerMachine = new double[this.nmbMachines];
        private final int[] demandPerProduct = new int[this.nmbProducts];
        private final int[][] qualificationPerProductPerMachine = new int[this.nmbProducts][this.nmbMachines];
        private final double[][] processTimePerProductPerMachine = new double[this.nmbProducts][this.nmbMachines];
        // Model and decision variables.
        private final Model model = new Model();
        private final Variable[][] x = new Variable[this.nmbProducts][this.nmbMachines];
        private final Variable[] w = new Variable[this.nmbMachines];
        private final Variable[] wl = new Variable[this.nmbMachines];
        private final double balancingExponent;

        WorkloadBalancing(double balancingExponent) throws LinearExpressionException {
            this.balancingExponent = balancingExponent;
            createInstance();
            createModel();
        }

        private void createInstance() {
            final Random random = new Random(0);
            double totalDemand = 0D;
            double averageProcessTime = 0D;
            for (int p = 0; p < this.nmbProducts; ++p) {
                final int productDemand = random.nextInt(100, 1000);
                this.demandPerProduct[p] = productDemand;
                totalDemand += productDemand;
                final int defaultQualifiedMachineForProduct = random.nextInt(0, this.nmbMachines);
                for (int m = 0; m < nmbMachines; ++m) {
                    final boolean isQualified = m == defaultQualifiedMachineForProduct || random.nextDouble() < 0.5;
                    this.qualificationPerProductPerMachine[p][m] = isQualified ? 1 : 0;
                    if (isQualified) {
                        final double processTime = random.nextDouble(15, 50);
                        this.processTimePerProductPerMachine[p][m] = processTime;
                        averageProcessTime += processTime;
                    }
                }
            }
            averageProcessTime = averageProcessTime / (double) (this.nmbMachines * this.nmbProducts);
            Arrays.fill(this.capacityPerMachine, (totalDemand * averageProcessTime) / (double) (this.nmbMachines));
        }

        private void createModel() throws LinearExpressionException {
            for (int p = 0; p < this.nmbProducts; ++p) {
                for (int m = 0; m < this.nmbMachines; ++m) {
                    final double ub = this.demandPerProduct[p] * this.qualificationPerProductPerMachine[p][m];
                    // The upper bound is used for qualification constraints.
                    // For product p and machine m: x_{p,m} <= qualificationPerProductPerMachine_{p,m}.
                    this.x[p][m] = this.model.addContinuousVariable(0D, ub, 0D);
                }
            }
            for (int m = 0; m < this.nmbMachines; ++m) {
                this.w[m] = this.model.addContinuousVariable(-Double.MAX_VALUE, Double.MAX_VALUE, 0D);
                this.wl[m] = this.model.addContinuousVariable(0D, Double.MAX_VALUE, 1D);
            }
            // Workload computation constraints.
            // For machine m: \sum_{p}(qualificationPerProductPerMachine_{p,m} * this.processTimePerProductPerMachine[p][m] * x_{p,m}) = capacityPerMachine_{m} w_{m}.
            for (int m = 0; m < this.nmbMachines; ++m) {
                final LinearExpression expression = new LinearExpression();
                expression.addCoefficient(this.w[m], this.capacityPerMachine[m]);
                for (int p = 0; p < this.nmbProducts; ++p) {
                    final double coefficient = this.qualificationPerProductPerMachine[p][m] * this.processTimePerProductPerMachine[p][m];
                    expression.addCoefficient(this.x[p][m], -coefficient);
                }
                this.model.addEqualityConstraint(0D, expression);
            }
            // Demand satisfaction constraints.
            // For product p: \sum_{p}(qualificationPerProductPerMachine_{p,m} *x_{p,m}) = demandPerProduct_{p}.
            for (int p = 0; p < this.nmbProducts; ++p) {
                final LinearExpression expression = new LinearExpression();
                for (int m = 0; m < this.nmbMachines; ++m) {
                    expression.addCoefficient(this.x[p][m], this.qualificationPerProductPerMachine[p][m]);
                }
                this.model.addEqualityConstraint(this.demandPerProduct[p], expression);
            }
            // Linearization constraints.
            for (double x0 = 0D; x0 <= 1D; x0 += 0.1D) {
                for (int m = 0; m < this.nmbMachines; ++m) {
                    // For machine m: wl_{m} - exponent*x0^{exponent-1}w_{m} >= x0^{exponent}*(1 - exponent).
                    addLinearizationConstraint(m, x0);
                }
            }
        }

        private void addLinearizationConstraints(final double[] workload) throws LinearExpressionException {
            for (int m = 0; m < this.nmbMachines; ++m) {
                final double x0 = workload[m];
                addLinearizationConstraint(m, x0);
            }
        }

        private void addLinearizationConstraint(int m, double x0) throws LinearExpressionException {
            final LinearExpression expression = new LinearExpression();
            expression.addCoefficient(this.wl[m], 1D);
            expression.addCoefficient(this.w[m], -this.balancingExponent * Math.pow(x0, this.balancingExponent - 1D));
            this.model.addGreaterThanOrEqualToConstraint(Math.pow(x0, this.balancingExponent) * (1D - this.balancingExponent), expression);
        }

        private boolean isRelativeGapReached(final double objectiveValue, final double[] workload) {
            double linearizedObjectiveValue = 0D;
            for (int m = 0; m < this.nmbMachines; ++m) {
                linearizedObjectiveValue += Math.pow(workload[m], this.balancingExponent);
            }
            final double gap = (linearizedObjectiveValue - objectiveValue) / (linearizedObjectiveValue + 1E-10);
            return linearizedObjectiveValue <= RELATIVE_GAP_TARGET || objectiveValue <= RELATIVE_GAP_TARGET || gap <= RELATIVE_GAP_TARGET;
        }

        public Solution solve() throws LinearExpressionException {
            boolean isRelativeGapReached;
            Solution solution;
            do {
                solution = this.model.minimize().orElseThrow();
                final double[] workload = getWorkload(solution);
                isRelativeGapReached = isRelativeGapReached(solution.getObjectiveValue(), workload);
                if (!isRelativeGapReached) {
                    addLinearizationConstraints(workload);
                }
            } while (!isRelativeGapReached);
            return solution;
        }

        public double[] getWorkload(final Solution solution) {
            double[] workload = new double[this.nmbMachines];
            for (int m = 0; m < this.nmbMachines; ++m) {
                workload[m] = solution.getVariableValue(this.w[m]);
            }
            return workload;
        }

    }

}
