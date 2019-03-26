/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import dbClasses.DBHelper;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 *
 * @author The_Humble_Fool
 */
public class FXMLDocumentController implements Initializable {

    private Label label;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private VBox vBoxMain;
    @FXML
    private HBox hBoxTop;
    @FXML
    private VBox vBoxTopLeft;
    @FXML
    private ComboBox<String> cbCatalog;
    @FXML
    private VBox vBoxTopRight;
    @FXML
    private ComboBox<String> cbTables;
    @FXML
    private VBox vBoxMiddle;
    @FXML
    private HBox vBoxMiddleHBoxTop;
    @FXML
    private FlowPane vBoxMiddleFlowPane;
    @FXML
    private HBox vBoxMiddleHBoxBottom;
    @FXML
    private HBox hBoxBottom;
    @FXML
    private TitledPane titledPaneBottom;
    @FXML
    private AnchorPane anchorPaneBottom;
    @FXML
    private TableView tableView;
    @FXML
    private TextField tfRowCount;
    @FXML
    private Button btnResults;

    DBHelper dbHelper = new DBHelper();
    List<CheckBox> checkBoxes = new ArrayList<>();
    int rowCount = 10;
    String catalogName, tableName;
    List<String> selectedParams = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        vBoxMiddleFlowPane.setVisible(false);
        tfRowCount.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.matches("\\d*")) {
                tfRowCount.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        ObservableList catalogItems = cbCatalog.getItems();
        catalogItems.clear();
        catalogItems.addAll(dbHelper.getCatalogs());
        titledPaneBottom.setExpanded(false);
        titledPaneBottom.setOnMouseClicked((event) -> {
            rootPane.getScene().getWindow().sizeToScene();
        });
    }

    @FXML
    private void populateTables(ActionEvent event) {
        catalogName = (String) ((ComboBox) event.getSource()).getValue();
        dbHelper.useCatalog(catalogName);
        ObservableList tables = cbTables.getItems();
        tables.clear();
        tables.addAll(dbHelper.getTables());
    }

    @FXML
    private void descTable(ActionEvent event) {
        tableName = (String) ((ComboBox) event.getSource()).getValue();
        ObservableList<String> tableColumns = dbHelper.getTableColumns(tableName);
        buildCheckBoxesWithColumns(tableColumns);
    }

    private void buildDataWithList(TableView tableView, ObservableList<String> tableCols, ObservableList<ObservableList<String>> tableDataAsList) {
        if (tableDataAsList == null) {
            System.out.println("Null table data received.");
            return;
        }
        tableView.getColumns().clear();
        ObservableList data = FXCollections.observableArrayList();
        for (int i = 0; i < tableCols.size(); ++i) {
            int j = i;
            TableColumn col = new TableColumn((String) tableCols.get(i));
            col.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param -> {
                if (param.getValue().get(j) != null) {
                    return new SimpleStringProperty(param.getValue().get(j).toString());
                } else {
                    return null;
                }
            });
            tableView.getColumns().add((Object) col);
        }
        tableDataAsList.forEach(t -> {
            ObservableList row1 = FXCollections.observableArrayList();
            for (int i = 0; i < t.size(); ++i) {
                row1.add(t.get(i));
            }
            data.add((Object) row1);
        });
        tableView.setItems(data);
        tableView.setFixedCellSize(25.0);
//        tableView.prefHeightProperty().bind((ObservableValue) tableView.fixedCellSizeProperty().multiply((ObservableNumberValue) Bindings.size((ObservableList) tableView.getItems()).add(1.01)));
        tableView.minHeightProperty().bind((ObservableValue) tableView.prefHeightProperty());
        tableView.maxHeightProperty().bind((ObservableValue) tableView.prefHeightProperty());
        titledPaneBottom.setExpanded(true);
        rootPane.getScene().getWindow().sizeToScene();
    }

    private void buildCheckBoxesWithColumns(ObservableList<String> tableColumns) {
        checkBoxes.clear();
        tableColumns.forEach((column) -> {
            checkBoxes.add(new CheckBox(column));
        });
        checkBoxes.add(new CheckBox("All"));
        vBoxMiddleFlowPane.getChildren().clear();
        vBoxMiddleFlowPane.getChildren().addAll(checkBoxes);
        vBoxMiddleFlowPane.setVisible(true);
        rootPane.getScene().getWindow().sizeToScene();
        setActionOnCheckboxes(checkBoxes);
    }

    private void setActionOnCheckboxes(List<CheckBox> checkBoxes) {
        checkBoxes.forEach((t) -> {
            t.setOnAction((event) -> {
                CheckBox cb = (CheckBox) event.getSource();
                if (cb.getText().equals("All")) {
                    if (cb.isSelected()) {
                        selectedParams.clear();
                        checkBoxes.forEach((p) -> {
                            if (!p.getText().equals("All")) {
                                selectedParams.add(p.getText());
                                p.setSelected(true);
                                p.setDisable(true);
                            }
                        });
                    } else {
                        selectedParams.clear();
                        checkBoxes.forEach((p) -> {
                            if (!p.getText().equals("All")) {
                                p.setSelected(false);
                                p.setDisable(false);
                            }
                        });
                    }
                } else {
                    if (cb.isSelected()) {
                        selectedParams.add(cb.getText());
                    } else {
                        selectedParams.remove(cb.getText());
                    }
                }
                System.out.println("Selected params: " + selectedParams);
                if (!selectedParams.isEmpty()) {
                    tfRowCount.setDisable(false);
                    btnResults.setDisable(false);
                } else {
                    tfRowCount.setDisable(true);
                    btnResults.setDisable(true);
                }
            });
        });
    }

//    @FXML
//    private void generateRowCount(ActionEvent event) {
//        TextField tfRowCount = (TextField) event.getSource();
//        rowCount = Integer.parseInt(tfRowCount.getText());
//    }
    @FXML
    private void buildResults(ActionEvent event) {
        ObservableList<String> tableColumns = dbHelper.getTableColumns(tableName);
//        ObservableList<ObservableList<String>> tableDataAsList = dbHelper.getTableDataAsList();
//        buildDataWithList(tableView, tableColumns, tableDataAsList);
        rowCount = Integer.parseInt(tfRowCount.getText());
        System.out.println("Limit: " + rowCount);
        if (rowCount > 50) {
            rowCount = 50;
        }
        ObservableList<ObservableList<String>> tableData = dbHelper.getTableData(tableName, selectedParams, rowCount);
        buildDataWithList(tableView, FXCollections.observableArrayList(selectedParams), tableData);
    }
}
