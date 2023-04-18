package ca.centennialcollege.lab05ex01;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DataDisplayController implements Initializable {
    private ArrayList<DisplayData> dataArrayList;
    @FXML
    private TableView<DisplayData> tbvPlayerGaming;
    @FXML
    private TableColumn<DisplayData,Integer> tbColID;
    @FXML
    private TableColumn<DisplayData,String> tbColName;
    @FXML
    private TableColumn<DisplayData,String> tbColAddress;
    @FXML
    private TableColumn<DisplayData,String> tbColPostalCode;
    @FXML
    private TableColumn<DisplayData,String> tbColProvince;
    @FXML
    private TableColumn<DisplayData,String> tbColPhone;
    @FXML
    private TableColumn<DisplayData,String> tbColGameTitle;
    @FXML
    private TableColumn<DisplayData,Long> tbColScore;
    @FXML
    private TableColumn<DisplayData, Date> tbColDatePlayed;

    @Override
    public void initialize(URL location, ResourceBundle resources) {  }
    public void setDisplayData(ArrayList<DisplayData> inputDataList){
        dataArrayList = inputDataList;
        if((dataArrayList != null) && (dataArrayList.size() > 0)) {
            ObservableList<DisplayData> data = FXCollections.observableArrayList(dataArrayList);
            tbColID.setCellValueFactory(new PropertyValueFactory<>("playerID"));
            tbColName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
            tbColAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
            tbColPostalCode.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
            tbColProvince.setCellValueFactory(new PropertyValueFactory<>("province"));
            tbColPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
            tbColGameTitle.setCellValueFactory(new PropertyValueFactory<>("gameTitle"));
            tbColDatePlayed.setCellValueFactory(new PropertyValueFactory<>("playingDate"));
            tbColScore.setCellValueFactory(new PropertyValueFactory<>("score"));
            tbvPlayerGaming.setItems(data);
        } else {
            tbvPlayerGaming.setPlaceholder( new Label("No rows to display"));
        }
    }
}
