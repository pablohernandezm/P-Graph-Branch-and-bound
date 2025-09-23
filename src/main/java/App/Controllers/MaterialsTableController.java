package App.Controllers;

import App.PGraph.Entities.Material;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

/**
 * Controller for the table of materials in the main window.
 * This class is responsible for displaying the list of materials in the main window.
 *
 * @author Pablo Hernández
 * @author Juan Camilo Narváez
 * @see App.PGraph.Entities.Material
 */
public class MaterialsTableController {
    private final ObservableList<Material> materialList = FXCollections.observableArrayList();
    @FXML
    public TableView<Material> table;
    @FXML
    public TableColumn<Material, String> nameColumn;
    @FXML
    public TableColumn<Material, String> typeColumn;
    @FXML
    public TableColumn<Material, String> flowColumn;

    /**
     * Adds a material to the list of materials.
     *
     * @param material The material to be added.
     */
    public void addMaterial(Material material) {
        materialList.add(material);
    }

    /**
     * Removes all materials from the list.
     */
    public void clear() {
        materialList.clear();
    }

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().toString()));
        flowColumn.setCellValueFactory(new PropertyValueFactory<>("lower_bound"));

        var placeholder = new Text("");

        table.getColumns().forEach(column -> {
            column.setResizable(false);
            column.setReorderable(false);
            column.setMinWidth(200);
        });

        placeholder.getStyleClass().add("h4");
        table.setPlaceholder(placeholder);
        table.setItems(materialList);
    }
}