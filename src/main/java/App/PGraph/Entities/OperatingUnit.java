package App.PGraph.Entities;

import javafx.beans.property.SimpleStringProperty;

/**
 * Operating unit class.
 *
 * @author Pablo Hernández
 * @author Juan Camilo Narváez
 */
public class OperatingUnit {
    protected SimpleStringProperty name;
    protected int capacity_upper_bound;
    protected int fixed_cost;
    protected int proportional_cost;
    protected Material inputMaterial;
    protected Material outputMaterial;
    protected String inputMaterialName;
    protected String outputMaterialName;

    /**
     * Constructor.
     *
     * @param name                 Name.
     * @param capacity_upper_bound capacity upper bound.
     * @param fixed_cost           fixed cost.
     * @param proportional_cost    proportional cost.
     */
    public OperatingUnit(String name, int capacity_upper_bound, int fixed_cost, int proportional_cost) {
        this.name = new SimpleStringProperty(name);
        this.capacity_upper_bound = capacity_upper_bound;
        this.fixed_cost = fixed_cost;
        this.proportional_cost = proportional_cost;
        this.inputMaterialName = "";
        this.outputMaterialName = "";
    }

    @Override
    public String toString() {
        return String.format("%s: capacity_upper_bound=%d, fix_cost=%d, proportional_cost=%d", name.get(), capacity_upper_bound, fixed_cost, proportional_cost) + ";" + String.format("%s: %s => %s", name.get(), inputMaterialName, outputMaterialName);
    }

    public String getName() {
        return name.get();
    }
    
    public int getCapacity_upper_bound() {
        return capacity_upper_bound;
    }

    public int getFixed_cost() {
        return fixed_cost;
    }

    public int getProportional_cost() {
        return proportional_cost;
    }

    public Material getInputMaterial() {
        return inputMaterial;
    }

    public void setInputMaterial(Material inputMaterial) {
        this.inputMaterial = inputMaterial;
        this.inputMaterialName = inputMaterial.getName();
    }

    public Material getOutputMaterial() {
        return outputMaterial;
    }

    public void setOutputMaterial(Material outputMaterial) {
        this.outputMaterial = outputMaterial;
        this.outputMaterialName = outputMaterial.getName();
    }
}