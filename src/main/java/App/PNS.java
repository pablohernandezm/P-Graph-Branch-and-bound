package App;

import App.Controllers.MaterialsTableController;
import App.Controllers.OUTableController;
import App.FXPGraph.FXPGraph;
import App.PGraph.Entities.Material;
import App.PGraph.Entities.OperatingUnit;
import App.PGraph.Utils.VariableState;
import App.Util.LaTeXNode;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Process Network Synthesis controller. This class is the main controller of the application.
 *
 * @author Pablo Hernández
 * @author Juan Camilo Narváez
 */
public class PNS {
    // Debounce variables for resizing
    private final int debounceTime = 350;
    @FXML
    public HBox hBox;
    @FXML
    public GridPane header;
    @FXML
    public ColumnConstraints header_col_constraint1;
    @FXML
    public VBox ouContainer;
    @FXML
    public ScrollPane canvas_scroll;
    @FXML
    public VBox maContainer;
    @FXML
    public HBox canvas_container;
    @FXML
    public Button saveBtn;
    @FXML
    public ScrollPane parentPane;
    @FXML
    public VBox best_formula;
    @FXML
    public VBox formula_section;
    // Controllers
    private OUTableController ouTableController;
    private MaterialsTableController materialsTableController;
    private Timeline debounceTimer;
    private boolean isFullScreen;

    /**
     * Load the FXML file.
     *
     * @param route Route of the FXML file.
     * @return FXMLLoader object.
     */
    private static FXMLLoader loadFXML(String route) {
        return new FXMLLoader(PNS.class.getResource(route));
    }

    /**
     * Get an OperatingUnit object from the parts and data.
     *
     * @param parts parts of the line.
     * @param data  data of the line.
     * @return OperatingUnit object.
     * @see #lineToOperatingUnit(String)
     */
    private static OperatingUnit getOperatingUnit(String[] parts, String[] data) {
        var name = parts[0].strip();
        OperatingUnit unit;
        try {
            int capacity_upper_bound = Integer.parseInt(data[0].split("=")[1].strip());
            int fixed_cost = Integer.parseInt(data[1].split("=")[1].strip());
            int proportional_cost = Integer.parseInt(data[2].split("=")[1].strip());

            unit = new OperatingUnit(name, capacity_upper_bound, fixed_cost, proportional_cost);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid operating unit data");
        }
        return unit;
    }

    private static Material getMaterial(String name, Material.Type type, String[] data) {
        Material material = new Material(name, type);
        if (data.length == 2) {
            try {
                String[] lower_bound = data[1].split("=");
                if (lower_bound.length != 2) {
                    throw new IllegalArgumentException("Invalid lower bound value [material: " + name + ", type: " + type + ", lower_bound: " + data[1].strip() + "]");
                }

                material.setLower_bound(Integer.parseInt(lower_bound[1].strip()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid lower bound value [material: " + name + ", type: " + type + ", lower_bound: " + data[1].strip() + "]");
            }
        }
        return material;
    }

    /**
     * Initialize the application and load the FXML files.
     *
     * @throws IOException If the FXML file is not found.
     */
    @FXML
    public void initialize() throws IOException {
        isFullScreen = false;

        //Tables
        FXMLLoader maLoader = loadFXML("components/MaterialsTable.fxml");
        Parent materialsTable = maLoader.load();
        maContainer.getChildren().add(materialsTable);
        materialsTableController = maLoader.getController();

        FXMLLoader ouTableLoader = loadFXML("components/OUTable.fxml");
        Parent ouTable = ouTableLoader.load();
        ouContainer.getChildren().add(ouTable);
        ouTableController = ouTableLoader.getController();

        // Add the debounce timer to the scene when it's loaded
        parentPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                debounceTimer = new Timeline(new javafx.animation.KeyFrame(Duration.millis(debounceTime), e -> checkDebounce()));

                parentPane.getScene().widthProperty().addListener((obs, oldVal, newVal) -> debounceTimer.playFromStart());
            }
        });

        // Only show the formula section when the canvas is not empty
        canvas_container.getChildren().subscribe(() -> {
            if (!canvas_container.getChildren().isEmpty() && !formula_section.isVisible()) {
                formula_section.setVisible(true);
                formula_section.setManaged(true);
            } else if (canvas_container.getChildren().isEmpty() && formula_section.isVisible()) {
                formula_section.setVisible(false);
                formula_section.setManaged(false);
            }
        });

        formula_section.setVisible(false);
        formula_section.setManaged(false);

        Consumer<ScrollPane> scrollEvents = (scroll) -> {
            //Zoom on ctrl + scroll
            scroll.addEventFilter(ScrollEvent.SCROLL, event -> {
                if (event.isControlDown() && (isFullScreen || !canvas_container.getChildren().isEmpty())) {
                    double zoomFactor = 1.05;
                    double deltaY = event.getDeltaY();

                    if (deltaY < 0) {
                        zoomFactor = 2.0 - zoomFactor;
                    }

                    canvas_container.setScaleX(canvas_container.getScaleX() * zoomFactor);
                    canvas_container.setScaleY(canvas_container.getScaleY() * zoomFactor);
                    event.consume();
                }
            });

            // Zoom in and out on ctrl + +/-
            scroll.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.isControlDown() && (isFullScreen || !canvas_container.getChildren().isEmpty())) {
                    if (event.getCode().equals(KeyCode.PLUS) || event.getCode().equals(KeyCode.ADD)) {
                        canvas_container.setScaleX(canvas_container.getScaleX() * 1.1);
                        canvas_container.setScaleY(canvas_container.getScaleY() * 1.1);
                    } else if (event.getCode().equals(KeyCode.MINUS) || event.getCode().equals(KeyCode.SUBTRACT)) {
                        canvas_container.setScaleX(canvas_container.getScaleX() * 0.9);
                        canvas_container.setScaleY(canvas_container.getScaleY() * 0.9);
                    }
                }
            });
        };

        // Full screen graph
        Runnable fullScreenCanvas = () -> {
            Stage fullScreenStage = new Stage();
            fullScreenStage.setMaximized(true);
            fullScreenStage.setTitle("Graph");
            fullScreenStage.initStyle(StageStyle.UNDECORATED);
            fullScreenStage.setFullScreen(true);

            ScrollPane fullScreenPane = new ScrollPane();
            fullScreenPane.setContent(canvas_container);
            fullScreenPane.setPannable(true);
            fullScreenPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/app.css")).toExternalForm());

            Scene scene = new Scene(fullScreenPane);
            fullScreenStage.setScene(scene);
            fullScreenStage.setAlwaysOnTop(true);
            fullScreenStage.show();
            isFullScreen = true;

            canvas_scroll.hvalueProperty().bindBidirectional(fullScreenPane.hvalueProperty());
            canvas_scroll.vvalueProperty().bindBidirectional(fullScreenPane.vvalueProperty());
            scrollEvents.accept(fullScreenPane);

            fullScreenStage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    canvas_scroll.setContent(canvas_container);
                    //unbind
                    canvas_scroll.hvalueProperty().unbindBidirectional(fullScreenPane.hvalueProperty());
                    canvas_scroll.vvalueProperty().unbindBidirectional(fullScreenPane.vvalueProperty());
                    isFullScreen = false;
                    fullScreenStage.close();
                }
            });

            fullScreenStage.setOnCloseRequest(event -> {
                canvas_scroll.setContent(canvas_container);
                //unbind
                canvas_scroll.hvalueProperty().unbindBidirectional(fullScreenPane.hvalueProperty());
                canvas_scroll.vvalueProperty().unbindBidirectional(fullScreenPane.vvalueProperty());
                isFullScreen = false;
                fullScreenStage.close();
            });

            //close on F11
            scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode().equals(KeyCode.F11) || event.getCode().equals(KeyCode.ALT) || event.getCode().equals(KeyCode.WINDOWS)) {
                    fullScreenStage.setFullScreen(false);
                }
            });

            // Remove content from the original ScrollPane
            canvas_scroll.setContent(null);
        };

        // Full screen canvas on right click
        canvas_container.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (!isFullScreen && !canvas_container.getChildren().isEmpty() && event.getButton() == MouseButton.SECONDARY) {
                fullScreenCanvas.run();
            }
        });

        // Full screen canvas on F11
        parentPane.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (!isFullScreen && !canvas_container.getChildren().isEmpty() && event.getCode().equals(KeyCode.F11)) {
                fullScreenCanvas.run();
            }
        });

        // Zoom events
        scrollEvents.accept(canvas_scroll);

    }

    /**
     * Create a graph with the given units and materials.
     *
     * @param units     Operating units.
     * @param materials Materials.
     * @see FXPGraph
     * @see OperatingUnit
     * @see Material
     */
    private void loadGraph(ArrayList<OperatingUnit> units, ArrayList<Material> materials) {
        FXPGraph graph = new FXPGraph(units);

        // Add the materials and operating units to their tables
        materialsTableController.clear();
        for (Material material : materials) {
            materialsTableController.addMaterial(material);
        }

        ouTableController.clear();
        for (OperatingUnit unit : units) {
            ouTableController.addOperatingUnit(unit);
        }

        // Clear the canvas to avoid overlapping and add the new graph
        canvas_container.getChildren().clear();
        var draw = graph.draw();

        canvas_container.getChildren().add(new StackPane(draw));
        canvas_container.setAlignment(Pos.CENTER);
        canvas_container.setPadding(new Insets(24));

        canvas_scroll.setPannable(true);

        //Show best solution
        var best = graph.getBestNode();
        if (best != null) {
            // Show the formula
            best_formula.getChildren().clear();
            ArrayList<VariableState> variables = new ArrayList<>(best.getSolutionStatus().getVariables());
            variables.sort(Comparator.comparing(VariableState::getName));


            VariableState[] xVariables = new VariableState[units.size()];
            VariableState[] yVariables = new VariableState[units.size()];

            int indexX = 0;
            int indexY = 0;
            for (VariableState variable : variables) {
                String name = variable.getName();

                if ((name.toCharArray()[0] + "").equalsIgnoreCase("x")) {
                    xVariables[indexX++] = variable;
                } else if ((name.toCharArray()[0] + "").equalsIgnoreCase("y")) {
                    yVariables[indexY++] = variable;
                }
            }

            Function<VariableState, String> variableValueText = (variable) -> variable.getValue() % 1 == 0 ? String.valueOf((int) variable.getValue()) : String.valueOf(variable.getValue());
            StringBuilder latex = new StringBuilder("Minimize: ");
            for (int i = 0; i < units.size(); i++) {
                var unit = units.get(i);
                boolean add = false;
                if (xVariables[i] != null) {
                    // \left( pc * x \right)
                    latex.append("\\left(").append(unit.getProportional_cost()).append("\\times").append(variableValueText.apply(xVariables[i])).append("\\right)");
                    add = true;
                }

                if (yVariables[i] != null) {
                    if (add) {
                        latex.append(" + ");
                    }

                    // \left( fc * y \right)
                    latex.append("\\left(").append(unit.getFixed_cost()).append("\\times").append(variableValueText.apply(yVariables[i])).append("\\right)");
                }

                if (i < units.size() - 1) {
                    latex.append(" + ");
                }
            }
            latex.append("=").append(best.getSolutionStatus().getValue()).append("$");

            String latexHeader = "Minimize: $\\sum_{i=1}^{n} \\left(\\left(Cf_i\\times Y_i:\\{0, 1\\}\\right) + \\left(Cp_i \\times X_i\\right)\\right)= $";

            HBox formula = new HBox(LaTeXNode.createLaTeXImage(latexHeader, 16, java.awt.Color.WHITE));
            HBox solution = new HBox(LaTeXNode.createLaTeXImage(latex.toString(), 16, java.awt.Color.WHITE));

            var bestDraw = graph.getBestNode().getDraw();
            graph.setPopup(best, bestDraw);

            formula.getChildren().add(bestDraw);
            formula.setAlignment(Pos.CENTER_LEFT);

            best_formula.getChildren().add(formula);
            best_formula.getChildren().add(solution);
        }

        // Show popup on click
        parentPane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            var popup = graph.getPopup();

            if (popup.isShowing()) {
                popup.hide();
            }
        });

        // When the graph is loaded, the user can save the file.
        saveBtn.setOnAction(event -> saveFile(units, materials));
    }

    /**
     * Load a file with the materials and operating units.
     */
    public void loadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        // Set the initial directory to the current directory
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        File file = fileChooser.showOpenDialog(parentPane.getScene().getWindow());
        if (file == null) {
            return;
        }

        String[] headers = {"materials:", "operating_units:", "material_to_operating_unit_flow_rates:"};

        ArrayList<Material> materials = new ArrayList<>();
        ArrayList<OperatingUnit> operatingUnits = new ArrayList<>();

        int current_header = -1;

        try {
            java.util.Scanner scanner = new java.util.Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().strip();

                if (Arrays.asList(headers).contains(line)) {
                    current_header++;
                    continue;
                }

                if (current_header < 0) continue;
                if (line.isBlank()) continue;

                if (current_header == 0) { //Materials
                    Material material = lineToMaterial(line);
                    materials.add(material);
                } else if (current_header == 1) { // Operating Units
                    operatingUnits.add(lineToOperatingUnit(line));
                } else if (current_header == 2) { // Flow rates
                    var parts = line.split(":");
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("Invalid flow rate line. [" + line + "]");
                    }

                    var data = parts[1].strip().split("=>");
                    if (data.length != 2) {
                        throw new IllegalArgumentException("Invalid flow rate data.[" + parts[1] + "]");
                    }

                    var ou_name = parts[0].strip();
                    var input = data[0].strip();
                    var output = data[1].strip();

                    OperatingUnit ou = null;
                    Material inputMaterial = null;
                    Material outputMaterial = null;

                    for (OperatingUnit unit : operatingUnits) {
                        if (unit.getName().equals(ou_name)) {
                            ou = unit;
                            break;
                        }
                    }

                    if (ou == null) {
                        throw new IllegalArgumentException("The required operating unit was not found. [" + ou_name + "]");
                    }

                    for (Material material : materials) {
                        if (material.getName().equals(input)) {
                            inputMaterial = material;
                        }

                        if (material.getName().equals(output)) {
                            outputMaterial = material;
                        }
                    }

                    if (inputMaterial == null || outputMaterial == null) {
                        throw new IllegalArgumentException("Invalid material name");
                    }

                    ou.setInputMaterial(inputMaterial);
                    ou.setOutputMaterial(outputMaterial);
                }
            }

            loadGraph(operatingUnits, materials);
        } catch (java.io.FileNotFoundException e) {
            System.out.println("An error occurred.");
        }
    }

    /**
     * Convert a string line to a Material object.
     *
     * @param line Line with the material data.
     * @return Material object.
     */
    private Material lineToMaterial(String line) {
        var parts = line.split(":");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid material line");
        }

        var data = parts[1].strip().split(",");

        if (data.length < 1 || data.length > 2) {
            throw new IllegalArgumentException("Invalid material data");
        }

        var name = parts[0].strip();
        var type = Material.Type.fromString(data[0].strip());

        return getMaterial(name, type, data);
    }

    /**
     * Convert a string line to an OperatingUnit object.
     *
     * @param line Line with the operating unit data.
     * @return OperatingUnit object.
     * @see #getOperatingUnit(String[], String[])
     */
    private OperatingUnit lineToOperatingUnit(String line) {
        var parts = line.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid operating unit line");
        }

        var data = parts[1].strip().split(",");
        if (data.length != 3) {
            throw new IllegalArgumentException("Invalid operating unit data line [operating unit: " + Arrays.toString(data) + "]\n line: " + line);
        }

        return getOperatingUnit(parts, data);
    }

    /**
     * Save the file with the materials and operating units.
     *
     * @param units     Operating units.
     * @param materials Materials.
     */
    public void saveFile(ArrayList<OperatingUnit> units, ArrayList<Material> materials) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Resource File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("output.txt");

        File file = fileChooser.showSaveDialog(parentPane.getScene().getWindow());
        if (file != null) {
            try {
                java.io.FileWriter myWriter = new java.io.FileWriter(file);

                myWriter.write("materials:\n");
                for (Material material : materials) {
                    myWriter.write(material.toString() + "\n");
                }

                ArrayList<String> flowRates = new ArrayList<>();

                myWriter.write("\n\noperating_units:\n");
                for (OperatingUnit unit : units) {
                    var info = unit.toString().split(";");
                    myWriter.write(info[0] + "\n");

                    flowRates.add(info[1]);
                }

                myWriter.write("\n\nmaterial_to_operating_unit_flow_rates:\n");
                for (var flow : flowRates) {
                    myWriter.write(flow + "\n");
                }

                myWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred.");
            }
        }
    }

    /**
     * Check if the debounce time has passed
     */
    private void checkDebounce() {
        if (debounceTimer.getCurrentTime().greaterThanOrEqualTo(Duration.millis(debounceTime))) {
            debounceTimer.stop();

            checkHBoxBounds();
        }
    }

    /**
     * Check if the HBox is overflowing and adjust the layout accordingly
     */
    public void checkHBoxBounds() {
        double elementsWidth = 0;

        for (javafx.scene.Node node : header.getChildren()) {
            elementsWidth += node.getBoundsInParent().getWidth();
        }

        if (elementsWidth >= parentPane.getScene().getWidth() - 100) {
            header_col_constraint1.setHgrow(Priority.NEVER);
            GridPane.setRowIndex(hBox, 1);
            GridPane.setColumnIndex(hBox, 0);
        } else {
            header_col_constraint1.setHgrow(Priority.ALWAYS);
            GridPane.setRowIndex(hBox, 0);
            GridPane.setColumnIndex(hBox, 1);
        }
    }
}
