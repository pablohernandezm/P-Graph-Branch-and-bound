package App.Controllers;

import App.PGraph.Entities.OperatingUnit;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

/**
 * Controller for the Operating Unit Table
 * This class is responsible for the Operating Unit Table in the GUI
 *
 * @author Pablo Hernández
 * @author Juan Camilo Narváez
 * @see OperatingUnit
 */
public class OUTableController {
    private final ObservableList<OperatingUnit> operatingUnitList = FXCollections.observableArrayList();
    @FXML
    public TableView<OperatingUnit> table;
    @FXML
    public TableColumn<OperatingUnit, String> nameColumn;
    @FXML
    public TableColumn<OperatingUnit, Integer> upperBoundColumn;
    @FXML
    public TableColumn<OperatingUnit, Integer> fCostColumn;
    @FXML
    public TableColumn<OperatingUnit, Integer> pCostColumn;
    @FXML
    public TableColumn<OperatingUnit, String> inputColumn;
    @FXML
    public TableColumn<OperatingUnit, String> outputColumn;

    /**
     * Adds an operating unit to the list of operating units.
     *
     * @param operatingUnit The operating unit to be added.
     */
    public void addOperatingUnit(OperatingUnit operatingUnit) {
        operatingUnitList.add(operatingUnit);
    }

    /**
     * Removes all operating units from the list.
     */
    public void clear() {
        operatingUnitList.clear();
    }

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        upperBoundColumn.setCellValueFactory(new PropertyValueFactory<>("capacity_upper_bound"));
        fCostColumn.setCellValueFactory(new PropertyValueFactory<>("fixed_cost"));
        pCostColumn.setCellValueFactory(new PropertyValueFactory<>("proportional_cost"));
        inputColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInputMaterial().getName()));
        outputColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOutputMaterial().getName()));

        table.getColumns().forEach(column -> {
            column.setResizable(false);
            column.setReorderable(false);
            column.setMinWidth(200);

        });


        var placeholder = new Text("");
        placeholder.getStyleClass().add("h4");
        table.setPlaceholder(placeholder);
        table.setItems(operatingUnitList);
    }
}
