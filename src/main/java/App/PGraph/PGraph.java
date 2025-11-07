package App.PGraph;

import App.PGraph.Entities.Material;
import App.PGraph.Entities.OperatingUnit;
import App.PGraph.Utils.VariableState;
import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.util.*;

/**
 * This class represents the graph of the process network synthesis optimization problem.
 *
 * @author Pablo Hernández
 * @author Juan Camilo Narváez
 */
public class PGraph {
    protected Node root;
    protected Node bestNode;
    protected int depth;
    protected ArrayList<OperatingUnit> units;

    /**
     * Default constructor.
     */
    public PGraph() {
        depth = 0;
    }

    /**
     * Constructor with units.
     *
     * @param units The list of operating units.
     */
    public PGraph(ArrayList<OperatingUnit> units) {
        this();
        this.units = units;
        depth = units.size() + 1;
        this.build();
    }

    /**
     * Build the graph using branch and bound.
     */
    private void build() {
        root = branchAndBound(null, null, null);
    }

    /**
     * Branch and bound recursive method.
     *
     * @param fixedValues         The fixed values for the variables.
     * @param parent              The parent node.
     * @param lastChangedVariable The last changed variable.
     * @return The node.
     */
    private Node branchAndBound(int[] fixedValues, Node parent, VariableState lastChangedVariable) {
        Node node = new Node();

        if (parent == null) node.setLevel(1);
        else node.setLevel(parent.getLevel() + 1);

        // Get the solver and solve the model
        var sol = getSolver(fixedValues);
        try {
            var result = sol.solve();

            // Set the solution status of the node
            if (result == MPSolver.ResultStatus.INFEASIBLE) {
                node.setFeasible(false);
                node.getSolutionStatus().setChanged(lastChangedVariable);
                return node;
            } else {
                node.getSolutionStatus().setValue(sol.objective().value());

                HashSet<VariableState> currentVariables = new HashSet<>();
                Arrays.stream(sol.variables()).iterator().forEachRemaining(
                    variable -> currentVariables.add(
                      new VariableState(variable.name(), variable.solutionValue()))
                    );

                node.getSolutionStatus().setVariables(currentVariables);
            }
        } catch (Exception e) {
            System.out.println("Error in solving the model with SCIP.");
        }

        // Search for the next variable to change if the solution is not integer.
        var variables = sol.variables();
        int toChange = getNonInteger(variables);

        // If there is a variable to change, branch and bound
        if (toChange >= 0) {
            int leftValue = Math.abs((int) Math.floor(variables[toChange].solutionValue()));
            int rightValue = leftValue + 1;

            if (fixedValues == null) {
                fixedValues = new int[variables.length];
                Arrays.fill(fixedValues, Integer.MIN_VALUE);
            }

            int[] leftFixedValues = Arrays.copyOf(fixedValues, fixedValues.length);
            leftFixedValues[toChange] = leftValue;

            int[] rightFixedValues = Arrays.copyOf(fixedValues, fixedValues.length);
            rightFixedValues[toChange] = rightValue;

            var variable = variables[toChange];

            node.setLeft(branchAndBound(leftFixedValues, node, new VariableState(variable.name(), leftValue, variable.solutionValue())));
            node.setRight(branchAndBound(rightFixedValues, node, new VariableState(variables[toChange].name(), rightValue, variable.solutionValue())));
        }

        // If the solution is an integer, return the node. There is no need to branch and bound.
        else {
            if (bestNode == null) {
                bestNode = node;
                node.setBest(true);
            } else if (sol.objective().value() < bestNode.getSolutionStatus().getValue()) {
                bestNode.setBest(false);
                node.setBest(true);
                bestNode = node;
            }
        }

        return node;
    }

    /**
     * Get the index of the first non-integer variable.
     *
     * @param variables The array of variables.
     * @return The index of the first non-integer variable.
     */
    private int getNonInteger(MPVariable[] variables) {
        int index = 0;
        for (var variable : variables) {
            if (variable.solutionValue() % 1 != 0) {
                return index;
            }
            index++;
        }

        return -1;
    }

    /**
     * Get the solver for the model.
     *
     * @param fixedValues The fixed values for the variables. If a value is Integer.MIN_VALUE, the variable is not fixed.
     * @return The solver.
     */
    private MPSolver getSolver(int[] fixedValues) {
        Loader.loadNativeLibraries();
        //Change to SCIP if there is a need to confirm that the solver is working.
        MPSolver solver = MPSolver.createSolver("GLOP");

        // Array to store the variables x
        MPVariable[] xArray = new MPVariable[units.size()];

        // Set the objective function
        MPObjective objective = solver.objective();
        objective.setMinimization();

        // Set the variables and constraints
        for (int i = 0; i < units.size(); i++) {
            ///Variables x and y
            var x = solver.makeNumVar(0, Double.POSITIVE_INFINITY, "X" + (i + 1));
            var y = solver.makeIntVar(0, 1, "Y" + (i + 1));

            xArray[i] = x;

            //x and y are non-negative
            var xPositive = solver.makeConstraint(0, Double.POSITIVE_INFINITY, "Non-negative for " + x.name());
            xPositive.setCoefficient(x, 1);

            var yPositive = solver.makeConstraint(0, 1, "Non-negative for " + y.name());
            yPositive.setCoefficient(y, 1);

            //Set the coefficients for the objective function
            objective.setCoefficient(x, units.get(i).getProportional_cost());
            objective.setCoefficient(y, units.get(i).getFixed_cost());

            //Set the constraints for the capacity
            //x <= y*capacity_upper_bound
            //x - y*capacity_upper_bound <= 0
            MPConstraint constraint = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0, "Upperbound for " + xArray[i].name());
            constraint.setCoefficient(x, 1);
            constraint.setCoefficient(y, -units.get(i).getCapacity_upper_bound());
        }

        // Class to store the origins and destinations of the materials. It is needed to create the constraints.
        class Pair {
            final LinkedHashSet<MPVariable> origins;
            final LinkedHashSet<MPVariable> destinations;

            public Pair() {
                origins = new LinkedHashSet<>();
                destinations = new LinkedHashSet<>();
            }
        }

        // Store the origins and destinations of the materials
        HashMap<Material, Pair> materialMap = new HashMap<>();
        var index = 0;
        for (OperatingUnit unit : units) {
            if (!materialMap.containsKey(unit.getInputMaterial())) {
                materialMap.put(unit.getInputMaterial(), new Pair());
            }
            if (!materialMap.containsKey(unit.getOutputMaterial())) {
                materialMap.put(unit.getOutputMaterial(), new Pair());
            }

            materialMap.get(unit.getInputMaterial()).destinations.add(xArray[index]);
            materialMap.get(unit.getOutputMaterial()).origins.add(xArray[index]);

            index++;
        }


        // Set the constraints for the origin-destination of the materials
        materialMap.forEach((material, pair) -> {
            if (pair.origins.isEmpty()) return;
            MPConstraint constraint = solver.makeConstraint(material.getLower_bound(), Double.POSITIVE_INFINITY, String.format("Origin-Destination for %s", material.getName()));

            pair.origins.forEach(variable -> constraint.setCoefficient(variable, 1));

            pair.destinations.forEach(variable -> constraint.setCoefficient(variable, -1));

        });

        // Set the fixed values for the variables
        if (fixedValues != null) {
            MPVariable[] variables = solver.variables();
            if (fixedValues.length == variables.length) {
                for (int i = 0; i < fixedValues.length; i++) {
                    if (fixedValues[i] != Integer.MIN_VALUE) {
                        variables[i].setBounds(fixedValues[i], fixedValues[i]);
                    }
                }
            }
        }

        return solver;
    }

    public Node getBestNode() {
        return bestNode;
    }
}
