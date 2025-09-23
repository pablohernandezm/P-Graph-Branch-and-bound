package App.PGraph.Entities;

/**
 * Material class represents a material in the process graph.
 * It has a name, a type and a lower bound for the flow rate.
 * The type can be one of the following: raw material, intermediate or product.
 *
 * @author Pablo Hernández
 * @author Juan Camilo Narváez
 */
public class Material {
    protected String name;
    protected Type type;
    protected int lower_bound;

    /**
     * Material constructor.
     *
     * @param name the name of the material.
     * @param type the type of the material.
     */
    public Material(String name, Type type) {
        this.name = name;
        this.type = type;
        this.lower_bound = 0;
    }

    /**
     * Returns a string representation of the material based on the process format.
     *
     * @return A String.
     */
    @Override
    public String toString() {
        return String.format("%s: %s", name, type.toString().toLowerCase().replace(" ", "_")) + (lower_bound > 0 ? String.format(", flow_rate_lower_bound=%d", lower_bound) : "");
    }

    public String getName() {
        return name;
    }

    public int getLower_bound() {
        return lower_bound;
    }

    public void setLower_bound(int lower_bound) {
        this.lower_bound = lower_bound;
    }

    public Type getType() {
        return type;
    }

    /**
     * Type enum represents the type of material.
     * It can be one of the following: raw material, intermediate or product.
     */
    public enum Type {
        RAW_MATERIAL {
            @Override
            public boolean matches(String value) {
                return value.equalsIgnoreCase("raw_material") || value.equalsIgnoreCase("raw material");
            }

            @Override
            public String toString() {
                return "Raw Material";
            }
        }, INTERMEDIATE {
            @Override
            public boolean matches(String value) {
                return value.equalsIgnoreCase("intermediate");
            }

            @Override
            public String toString() {
                return "Intermediate";
            }
        }, PRODUCT {
            @Override
            public boolean matches(String value) {
                return value.equalsIgnoreCase("product");
            }

            @Override
            public String toString() {
                return "Product";
            }
        };

        /**
         * Returns the Type that matches the given value.
         *
         * @param value the value to match.
         * @return the Type that matches the given value.
         */
        public static Type fromString(String value) {
            for (Type type : Type.values()) {
                if (type.matches(value)) return type;
            }
            throw new IllegalArgumentException("Invalid type value");
        }

        /**
         * Returns true if the given value matches the type.
         *
         * @param value the value to match.
         * @return true if the value matches the type, false otherwise.
         */
        public abstract boolean matches(String value);
    }
}