package App.PGraph.Utils;

/**
 * This class represents the status of a variable in the optimization problem.
 * It contains the name of the variable, its value and the previous value.
 *
 * @author Pablo Hernández
 * @author Juan Camilo Narváez
 */
public class VariableState {
    private String name;
    private double value;
    private double previousValue;

    /**
     * Default constructor.
     */
    public VariableState(){
        this.name = "";
        this.value = -1;
        this.previousValue = -1;
    }

    /**
     * Constructor with name.
     * @param name The name of the variable.
     */
    public VariableState(String name){
        this();
        this.name = name;
    }

    /**
     * Constructor with name and value.
     * @param name The name of the variable.
     * @param value The current value of the variable.
     */
    public VariableState(String name, double value){
        this(name);
        this.value = value;
    }

    /**
     * Constructor with name, value and previous value.
     * @param name The name of the variable.
     * @param value The current value of the variable.
     * @param previousValue The previous value of the variable.
     */
    public VariableState(String name, double value, double previousValue){
        this(name, value);
        this.previousValue = previousValue;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public double getPreviousValue() {
        return previousValue;
    }
}