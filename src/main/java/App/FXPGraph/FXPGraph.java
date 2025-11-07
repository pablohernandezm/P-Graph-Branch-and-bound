package App.FXPGraph;

import App.PGraph.Entities.OperatingUnit;
import App.PGraph.Node;
import App.PGraph.PGraph;
import App.PGraph.Utils.VariableState;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Popup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * PGraph adapted to JavaFX.
 * This class is responsible for drawing the graph in a JavaFX environment.
 *
 * @author Pablo Hernández
 * @author Juan Camilo Narváez
 * @see PGraph
 */
public class FXPGraph extends PGraph {
    private final SimpleDoubleProperty nodeRadius;
    private final Group treePane;
    private final Popup popup;
    protected SimpleDoubleProperty width;

    /**
     * Constructor.
     *
     * @param units List of OperatingUnits to be represented in the graph.
     * @see App.PGraph.Entities.OperatingUnit
     */
    public FXPGraph(ArrayList<OperatingUnit> units) {
        super(units);

        nodeRadius = new SimpleDoubleProperty();
        nodeRadius.set(Node.DEFAULT_RADIUS);

        width = new SimpleDoubleProperty();
        treePane = new Group();
        popup = new Popup();
    }

    /**
     * Vertical spacing between nodes.
     *
     * @param level Level of the node in the tree.
     * @return Vertical spacing between nodes.
     */
    private double getVsSpacing(int level) {
        return this.radiusProperty().get() * 2 * ((double) this.depth / (level + 1));
    }

    /**
     * Method to get visuals of the graph nodes and edges.
     *
     * @return Group with the visuals of the graph.
     */
    public Group draw() {
        Logger logger = Logger.getLogger("FXPGraph.draw");
        logger.setLevel(Level.INFO);

        if (root == null) return null;


        logger.info("FXPGraph");

        // Clear the pane and set the root node in the middle of the pane.
        treePane.getChildren().clear();
        root.xProperty().set(this.width.get() / 2);
        root.yProperty().set(nodeRadius.get());

        drawTree(root);
        return treePane;
    }

    /**
     * Method to draw the tree recursively.
     *
     * @param node Node to be drawn.
     */
    private void drawTree(Node node) {
        if (node == null) return;

        node.radiusProperty().bind(nodeRadius);

        // Horizontal and vertical spacing between nodes.
        var h_spacing = Math.pow(2, depth - node.getLevel()) * (node.getRadius() / 1.5);
        var v_spacing = getVsSpacing(node.getLevel());

        if (node.getLeft() != null) {
            node.getLeft().xProperty().set(node.xProperty().get() - h_spacing);
            node.getLeft().yProperty().set(node.yProperty().get() + node.getRadius() * 2 + v_spacing);

            treePane.getChildren().add(getEdge(node, node.getLeft()));
        }

        if (node.getRight() != null) {
            node.getRight().xProperty().set(node.xProperty().get() + h_spacing);
            node.getRight().yProperty().set(node.yProperty().get() + node.getRadius() * 2 + v_spacing);

            treePane.getChildren().add(getEdge(node, node.getRight()));
        }

        // Set the popup for the node.
        var nodeDraw = node.getDraw();
        setPopup(node, nodeDraw);

        treePane.getChildren().add(nodeDraw);
        drawTree(node.getLeft());
        drawTree(node.getRight());
    }

    /**
     * Method to set a popup for a node. It shows the variables of the node.
     *
     * @param node     Node to set the popup.
     * @param nodeDraw Group element of the node.
     */
    public void setPopup(Node node, Group nodeDraw) {
        nodeDraw.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
            popup.getContent().clear();
            if (popup.isShowing()) popup.hide();
            else {
                popUpTable(node);
            }

            event.consume();
        });
    }

    /**
     * Method to set the table of variables in the popup.
     *
     * @param node Node to show the variables.
     */
    private void popUpTable(Node node) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPadding(new Insets(16));

        HashSet<VariableState> variables = node.getSolutionStatus().getVariables();

        // If the solution is feasible there are variables to show
        if (variables != null) {
            // Sort variables by name
            ArrayList<VariableState> variablesList = new ArrayList<>(variables);
            variablesList.sort(Comparator.comparing(VariableState::getName));

            //TableView setup
            TableView<VariableState> table = new TableView<>();
            table.setPadding(new Insets(10));
            TableColumn<VariableState, String> column1 = new TableColumn<>("Name");
            TableColumn<VariableState, String> column2 = new TableColumn<>("Value");

            column1.setSortable(false);
            column2.setSortable(false);

            column1.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
            column2.setCellValueFactory(cellData -> {
                String value;
                if (cellData.getValue().getValue() % 1 == 0) {
                    value = String.valueOf((int) cellData.getValue().getValue());
                } else {
                    value = String.valueOf(cellData.getValue().getValue());
                }
                return new SimpleStringProperty(value);
            });

            table.getColumns().add(column1);
            table.getColumns().add(column2);

            table.getItems().addAll(variablesList);
            scrollPane.setContent(table);
        }

        // If the solution is infeasible show a message
        else {
            String message = String.format("Infeasible solution when solving the model with changed variable [%s=%.4f] to [%s=%.0f]", node.getSolutionStatus().getChanged().getName(), node.getSolutionStatus().getChanged().getPreviousValue(), node.getSolutionStatus().getChanged().getName(), node.getSolutionStatus().getChanged().getValue());

            Text text = new Text(message);
            text.getStyleClass().add("popup-text");

            scrollPane.setContent(text);
        }

        popup.getContent().add(scrollPane);
        popup.setAutoHide(true);
        popup.show(treePane.getScene().getWindow());
    }

    /**
     * Method to get the edge line between two nodes.
     *
     * @param node1 Node 1.
     * @param node2 Node 2.
     * @return Group with the edge line.
     */
    private Group getEdge(Node node1, Node node2) {
        // Calculate the angle between the two nodes.
        var angle = Math.atan2(node2.yProperty().get() - node1.yProperty().get(), node2.xProperty().get() - node1.xProperty().get());

        // Create the line.
        Line line = new Line();
        line.startXProperty().bind(node1.xProperty().add(node1.getRadius() * Math.cos(angle)));
        line.startYProperty().bind(node1.yProperty().add(node1.getRadius() * Math.sin(angle)));
        line.endXProperty().bind(node2.xProperty().subtract(node2.getRadius() * Math.cos(angle)));
        line.endYProperty().bind(node2.yProperty().subtract(node2.getRadius() * Math.sin(angle)));

        line.getStyleClass().add("edge");
        node2.isSelectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                line.getStyleClass().add("selected-edge");
            } else {
                line.getStyleClass().remove("selected-edge");
            }
        });

        return new Group(line);
    }

    /**
     * Method to set the width of the graph.
     *
     * @return Width of the graph.
     */
    public DoubleProperty radiusProperty() {
        return this.nodeRadius;
    }

    /**
     * Method to set the width of the graph.
     *
     * @return Width of the graph.
     */
    public Popup getPopup() {
        return popup;
    }
}
