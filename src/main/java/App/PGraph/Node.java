package App.PGraph;


import App.PGraph.Utils.SolutionStatus;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

/**
 * This class represents a node in the graph.
 * It also contains its properties and methods to draw it in a JavaFX scene.
 *
 * @author Pablo Hernández
 * @author Juan Camilo Narváez
 * @see PGraph
 */
public class Node {
    public static final Double DEFAULT_RADIUS = 16.0;
    // Properties
    protected int level;
    protected Node left;
    protected Node right;
    protected SolutionStatus solutionStatus;
    // JavaFX properties
    protected SimpleStringProperty text;
    protected SimpleDoubleProperty x;
    protected SimpleDoubleProperty y;
    protected SimpleBooleanProperty isLeaf;
    protected DoubleProperty r;
    protected Paint textColor;
    protected Group draw;
    protected SimpleBooleanProperty isSelected;

    /**
     * Default constructor.
     */
    public Node() {
        left = null;
        right = null;

        text = new SimpleStringProperty("");
        x = new SimpleDoubleProperty(0f);
        y = new SimpleDoubleProperty(0f);
        r = new Circle().radiusProperty();
        isLeaf = new SimpleBooleanProperty(false);
        isSelected = new SimpleBooleanProperty(false);
        this.solutionStatus = new SolutionStatus(Double.POSITIVE_INFINITY, null, null);

        r.set(DEFAULT_RADIUS);
        textColor = Color.BLACK;
    }

    /**
     * Get the JavaFX group that represents the node.
     * CSS classes for styling:
     * - node: The circle.
     * - node-text: The text.
     * - leaf: If the node is a leaf.
     * - feasible: If the solution is feasible.
     * - infeasible: If the solution is infeasible.
     * - best: If the solution is the best.
     * - selected-node: If the node is selected.
     *
     * @return Group with a circle and a text.
     */
    public Group getDraw() {
        Circle circle = new Circle();
        circle.centerXProperty().bind(x);
        circle.centerYProperty().bind(y);
        circle.radiusProperty().bind(r);
        circle.getStyleClass().add("node");

        Text text = new Text();
        text.textProperty().bind(this.text);
        text.setBoundsType(TextBoundsType.VISUAL);
        text.getStyleClass().add("node-text");

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(circle, text);
        stackPane.layoutXProperty().bind(x.subtract(r));
        stackPane.layoutYProperty().bind(y.subtract(r));
        stackPane.setMinSize(2 * r.get(), 2 * r.get());
        stackPane.setMaxSize(2 * r.get(), 2 * r.get());

        draw = new Group(stackPane);

        this.isLeaf.addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                draw.getStyleClass().add("leaf");
            } else {
                draw.getStyleClass().remove("leaf");
            }
        });

        this.isSelected.addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                circle.getStyleClass().add("selected-node");
            } else {
                circle.getStyleClass().remove("selected-node");
            }
        });

        // Update styles when the node's solution status changes
        Runnable updateStyles = () -> {
            if (solutionStatus.isFeasible()) {
                draw.getStyleClass().add("feasible");
                draw.getStyleClass().remove("infeasible");

                if (solutionStatus.getValue() % 1 == 0) {
                    this.text.set("" + (int) solutionStatus.getValue());
                } else {
                    this.text.set("" + solutionStatus.getValue());
                }
            } else {
                draw.getStyleClass().add("infeasible");
                draw.getStyleClass().remove("feasible");

                this.text.set("X");
            }

            if (solutionStatus.isBest()) {
                draw.getStyleClass().remove("infeasible");
                draw.getStyleClass().add("best");
            } else {
                draw.getStyleClass().remove("best");
            }
        };

        solutionStatus.feasibleProperty().addListener((observableValue, aBoolean, t1) -> updateStyles.run());
        solutionStatus.bestProperty().addListener((observableValue, aBoolean, t1) -> updateStyles.run());

        // Update styles when the node is created
        updateStyles.run();

        return draw;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public SolutionStatus getSolutionStatus() {
        return solutionStatus;
    }

    public void setFeasible(boolean feasible) {
        solutionStatus.setFeasible(feasible);
    }

    public void setBest(boolean best) {
        solutionStatus.setBest(best);
    }

    public double getRadius() {
        return r.get();
    }

    public DoubleProperty radiusProperty() {
        return r;
    }

    public SimpleBooleanProperty isSelectedProperty() {
        return isSelected;
    }

    public SimpleDoubleProperty xProperty() {
        return x;
    }

    public SimpleDoubleProperty yProperty() {
        return y;
    }
}