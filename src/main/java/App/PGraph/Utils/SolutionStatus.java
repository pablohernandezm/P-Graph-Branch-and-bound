package App.PGraph.Utils;

import javafx.beans.property.SimpleBooleanProperty;

import java.util.HashSet;

/**
 * This class is used to store the status of a solution from the Branch and Bound algorithm.
 * It stores the value of the solution, the feasibility of the solution, the best solution found so far, the variables that are part of the solution and the variable that was changed to reach this solution.
 *
 * @author Pablo Hernández
 * @author Juan Camilo Narváez
 */
public class SolutionStatus {
    double value;
    SimpleBooleanProperty feasible;
    SimpleBooleanProperty best;
    VariableState changed;
    HashSet<VariableState> variables;

    /**
     * Constructor for the SolutionStatus class.
     *
     * @param value     The value of the solution.
     * @param variables The variables that are part of the solution.
     * @param changed   The variable that was changed to reach this solution.
     */
    public SolutionStatus(double value, HashSet<VariableState> variables, VariableState changed) {
        this.value = value;
        this.variables = variables;
        this.changed = changed;
        this.feasible = new SimpleBooleanProperty(true);
        this.best = new SimpleBooleanProperty(false);
    }

    public boolean isFeasible() {
        return feasible.get();
    }

    public void setFeasible(boolean feasible) {
        this.feasible.set(feasible);
    }

    public boolean isBest() {
        return best.get();
    }

    public void setBest(boolean best) {
        this.best.set(best);
    }

    public HashSet<VariableState> getVariables() {
        return variables;
    }

    public void setVariables(HashSet<VariableState> variables) {
        this.variables = variables;
    }

    public VariableState getChanged() {
        return changed;
    }

    public void setChanged(VariableState changed) {
        this.changed = changed;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public SimpleBooleanProperty feasibleProperty() {
        return feasible;
    }

    public SimpleBooleanProperty bestProperty() {
        return best;
    }
}
