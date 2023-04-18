package ca.centennialcollege.lab05ex01;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // class variables ...
    private Connection dbConnection;
    private Statement sql_stmt;
    private PreparedStatement prep_stmt;
    private final String strInsertPlayerSQL = "INSERT INTO player (first_name,last_name,address,postal_code," +
            "province,phone_number) VALUES (?,?,?,?,?,?)";
    private final String strInsertGameSQL = "INSERT INTO game (game_title) VALUES (?)";
    private final String strInsertPlayerGameSQL = "INSERT INTO playerandgame ( player_id, game_id, playing_date, score ) " +
            "VALUES (?,?,?,?)";
    private final String selectPlayDetailsSQL = "SELECT p.player_id, p.last_name || ', ' || p.first_name as fullname, p.address, " +
            "p.postal_code, p.province, p.phone_number, g.game_title, pg.playing_date, pg.score " +
            "FROM player p, game g, playerandgame pg " +
            "WHERE p.player_id = pg.player_id AND g.game_id = pg.game_id";

    // used controls ...
    @FXML
    private Label lblStatusBar;
    @FXML
    private TextField txtFirstName,txtLastName, txtProvince, txtPostalCode, txtPhoneNumber;
    @FXML
    private TextField txtGameTitle, txtGameScore, txtUpdatePlayerID;
    @FXML
    private TextArea txtAreaAddress;
    @FXML
    private DatePicker txtDatePlayed;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // attach new text formatter to our game score text field
        txtGameScore.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            } else {
                return null;
            }
        }));
        // attach new text formatter to our update player id text field
        txtUpdatePlayerID.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            } else {
                return null;
            }
        }));
        // Get the DB connection
        dbConnection = getDBConnection();
        if(dbConnection != null){
            lblStatusBar.setText("Database connected successfully.");
        } else {
            lblStatusBar.setText("Sorry: Cannot connect to the DB. Please close this window and try again.");
        }
    }

    /**
     * Event handler when clicking the Create Player button
     * @throws SQLException
     */
    @FXML
    protected void onBtnCreatePlayerClick() throws SQLException {
        if(dbConnection != null){
            if(validateInput() == true){
                try {
                    // Set auto commit to false, for transaction management
                    dbConnection.setAutoCommit(false);
                    // save player details ...
                    // 1a. create the prepared statement for player table ...
                    prep_stmt = dbConnection.prepareStatement(strInsertPlayerSQL, new String[]{"PLAYER_ID"});
                    // 2a. assigned the required parameters to our statement ...
                    prep_stmt.setString(1,txtFirstName.getText().trim());
                    prep_stmt.setString(2,txtLastName.getText().trim());
                    prep_stmt.setString(3,txtAreaAddress.getText().trim());
                    prep_stmt.setString(4,txtPostalCode.getText().trim());
                    prep_stmt.setString(5,txtProvince.getText().trim());
                    prep_stmt.setString(6,txtPhoneNumber.getText().trim());
                    // 3a. execute the SQL statement ...
                    prep_stmt.executeUpdate();
                    ResultSet insertPlayerRS = prep_stmt.getGeneratedKeys();
                    // if save player details is successful, save game details...
                    if(insertPlayerRS.next()){
                        // get the new player ID
                        long newPlayerID = insertPlayerRS.getLong(1);
                        // 1b. create the prepared statement for game table ...
                        prep_stmt = dbConnection.prepareStatement(strInsertGameSQL, new String[]{"GAME_ID"});
                        // 2b. assigned the required parameters to our statement ...
                        prep_stmt.setString(1,txtGameTitle.getText());
                        // 3b. execute the SQL statement ...
                        prep_stmt.executeUpdate();
                        ResultSet insertGameRS = prep_stmt.getGeneratedKeys();
                        // if save game details is successful, save player_game details...
                        if(insertGameRS.next()){
                            // get the new game ID
                            long newGameID = insertGameRS.getLong(1);
                            // 1c. create the prepared statement for player_game table ...
                            prep_stmt = dbConnection.prepareStatement(strInsertPlayerGameSQL, new String[]{"PLAYER_GAME_ID"});
                            // 2c. assigned the required parameters to our statement ...
                            prep_stmt.setLong(1,newPlayerID);
                            prep_stmt.setLong(2,newGameID);
                            prep_stmt.setDate(3,java.sql.Date.valueOf(txtDatePlayed.getValue()));
                            prep_stmt.setInt(4,Integer.parseInt(txtGameScore.getText()));
                            // 3c. execute the SQL statement ...
                            prep_stmt.executeUpdate();
                            ResultSet insertPlayerGameRS = prep_stmt.getGeneratedKeys();
                            if(insertPlayerGameRS.next()){
                                // if save player_game record is successful, commit the transaction
                                dbConnection.commit();
                                // clear the form contents
                                clearForm();
                                // notify the app user...
                                lblStatusBar.setText("Player and Game Records created successfully.");
                            } else {
                                if(dbConnection != null){
                                    dbConnection.rollback();
                                }
                                lblStatusBar.setText("Player and Game Record not created successfully. Rolling Back...");
                            }
                        } else {
                            lblStatusBar.setText("Game Record not created successfully. Rolling Back...");
                        }
                    } else {
                        if(dbConnection != null){
                            dbConnection.rollback();
                        }
                        lblStatusBar.setText("Player Record not created successfully. Rolling Back...");
                    }
                } catch (SQLException e) {
                    if(dbConnection != null){
                        dbConnection.rollback();
                    }
                    lblStatusBar.setText(e.getMessage());
                }
            } else {
                lblStatusBar.setText("One or more fields on this form is blank or contains invalid data.");
            }
        } else {
            lblStatusBar.setText("Sorry there is no database connection at the moment. Task cannot be executed.");
        }
    }

    private void clearForm() {
        txtFirstName.setText("");
        txtLastName.setText("");
        txtProvince.setText("");
        txtPostalCode.setText("");
        txtPhoneNumber.setText("");
        txtGameTitle.setText("");
        txtGameScore.setText("");
        txtUpdatePlayerID.setText("");
        txtAreaAddress.setText("");
        txtDatePlayed.setValue(null);
    }

    /**
     * Event handler to display second window showing all captured records
     * @throws IOException
     */
    @FXML
    protected void onBtnDisplayAllPlayersClick() throws IOException {
        // Create a new Stage object
        Stage secondaryStage = new Stage();
        // Load the FXML file for the new window's layout
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("data-display.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        DataDisplayController ddController = fxmlLoader.getController();
        ArrayList<DisplayData> tempDispData = getDisplayResultSet();
        ddController.setDisplayData(tempDispData);
        // Set the modality to APPLICATION_MODAL
        secondaryStage.initModality(Modality.APPLICATION_MODAL);
        // Set the scene for the new window
        secondaryStage.setScene(scene);
        // Show the new window in modal
        secondaryStage.showAndWait();
    }

    /**
     * Event handler for when the update button is clicked.
     */
    @FXML
    protected void onBtnUpdateClick(){
        if(!txtUpdatePlayerID.getText().isEmpty()){
            Long currPlayerID = Long.parseLong(txtUpdatePlayerID.getText());
            Long currGameID = getGameIDFromPlayerID(currPlayerID);
            // get values from updated fields on the form
            ArrayList<FormFieldEntry> playerUpdateEntries = getPlayerUpdateEntries();
            ArrayList<FormFieldEntry> gameUpdateEntries = getGameUpdateEntries();
            ArrayList<FormFieldEntry> playerGameUpdateEntries = getPlayerGameUpdateEntries();
            // build the set statement based on the updated fields in the form
            String playerSetClause = getSQLSetClause(playerUpdateEntries);
            String gameSetClause = getSQLSetClause(gameUpdateEntries);
            String playerGameSetClause = getSQLSetClause(playerGameUpdateEntries);
            try{
                // try to update the player table ...
                if(playerSetClause != null && !playerSetClause.isEmpty()){
                    String sqlStmt = "UPDATE player " + playerSetClause + " WHERE PLAYER_ID = " + currPlayerID;
                    sql_stmt = dbConnection.createStatement();
                    sql_stmt.executeUpdate(sqlStmt);
                    sql_stmt.close();
                }
                // try to update the game table ...
                if(gameSetClause != null && !gameSetClause.isEmpty()){
                    String sqlStmt = "UPDATE game " + gameSetClause + " WHERE GAME_ID = " + currGameID;
                    sql_stmt = dbConnection.createStatement();
                    sql_stmt.executeUpdate(sqlStmt);
                    sql_stmt.close();
                }
                // try to update the PlayerAndGame table
                if(playerGameSetClause != null && !playerGameSetClause.isEmpty()){
                    String sqlStmt = "UPDATE playerandgame " + playerGameSetClause + " WHERE PLAYER_ID = " + currPlayerID;
                    sql_stmt = dbConnection.createStatement();
                    sql_stmt.executeUpdate(sqlStmt);
                    sql_stmt.close();
                }
            } catch (Exception e){
                lblStatusBar.setText(e.getMessage());
            }
            clearForm();
            lblStatusBar.setText("Form data updated successfully.");
        }
    }

    /**
     * Get the ID of the current game linked to the current player
     * @param currPlayerID
     * @return
     */
    private Long getGameIDFromPlayerID(Long currPlayerID) {
        Long currGameID = 0L;
        try {
            String tmpSQL = "SELECT game_id FROM playerandgame WHERE player_id = " + currPlayerID;
            sql_stmt = dbConnection.createStatement();
            ResultSet rset = sql_stmt.executeQuery(tmpSQL);
            if(rset.next()){
                currGameID = rset.getLong(1);
            }
        } catch (SQLException e) {
            lblStatusBar.setText(e.getMessage());
        }
        return currGameID;
    }

    /**
     * get the SQL Select clause from the form entries
     * @param tempUpdateEntries
     * @return String
     */
    private String getSQLSetClause(ArrayList<FormFieldEntry> tempUpdateEntries) {
        StringBuilder tempSetClause = new StringBuilder();
        int numEntries = tempUpdateEntries.size();
        int count = 1;
        if(numEntries > 0){
            tempSetClause.append("SET " );
            for (FormFieldEntry ffEntry:tempUpdateEntries) {
                if(ffEntry.dataType.equals("String")){
                    tempSetClause.append(ffEntry.fieldName + " = \'" + ffEntry.fieldValue + "\'");
                } else {
                    tempSetClause.append(ffEntry.fieldName + " = " + ffEntry.fieldValue);
                }
                if(count < numEntries){
                    tempSetClause.append(", ");
                }
            }
        }
        return tempSetClause.toString();
    }

    /**
     * Get the data put in the fields linked to the Player table
     * @return String
     */
    private ArrayList<FormFieldEntry> getPlayerUpdateEntries() {
        ArrayList<FormFieldEntry> tempFieldEntries = new ArrayList<>();
        if(!txtFirstName.getText().isEmpty()){
            tempFieldEntries.add(
                new FormFieldEntry("FIRST_NAME",txtFirstName.getText(),"String")
            );
        }
        if(!txtLastName.getText().isEmpty()){
            tempFieldEntries.add(
                    new FormFieldEntry("LAST_NAME",txtLastName.getText(),"String")
            );
        }
        if(!txtAreaAddress.getText().isEmpty()){
            tempFieldEntries.add(
                    new FormFieldEntry("ADDRESS",txtAreaAddress.getText(),"String")
            );
        }
        if(!txtProvince.getText().isEmpty()){
            tempFieldEntries.add(
                    new FormFieldEntry("PROVINCE",txtProvince.getText(),"String")
            );
        }
        if(!txtPostalCode.getText().isEmpty()){
            tempFieldEntries.add(
                    new FormFieldEntry("POSTAL_CODE",txtPostalCode.getText(),"String")
            );
        }
        if(!txtPhoneNumber.getText().isEmpty()){
            tempFieldEntries.add(
                    new FormFieldEntry("PHONE_NUMBER",txtPhoneNumber.getText(),"String")
            );
        }
        return tempFieldEntries;
    }

    /**
     * Get the data put in the fields linked to the Game table
     * @return
     */
    private ArrayList<FormFieldEntry> getGameUpdateEntries(){
        ArrayList<FormFieldEntry> tempFieldEntries = new ArrayList<>();
        if(!txtGameTitle.getText().isEmpty()){
            tempFieldEntries.add(
                    new FormFieldEntry("GAME_TITLE",txtGameTitle.getText(),"String")
            );
        }
        return tempFieldEntries;
    }

    /**
     * Get the data put in the fields linked to the Player and Game table
     * @return String
     */
    private ArrayList<FormFieldEntry> getPlayerGameUpdateEntries(){
        ArrayList<FormFieldEntry> tempFieldEntries = new ArrayList<>();
        if(!txtGameScore.getText().isEmpty()){
            tempFieldEntries.add(
                    new FormFieldEntry("SCORE",txtGameScore.getText(),"Long")
            );
        }
        if(txtDatePlayed.getValue() != null){
            tempFieldEntries.add(
                    new FormFieldEntry("PLAYING_DATE",txtDatePlayed.getValue().toString(),"String")
            );
        }
        return tempFieldEntries;
    }

    /**
     * get the list of data to display in the display data page
     * @return ArrayList<DisplayData>
     */
    private ArrayList<DisplayData> getDisplayResultSet(){
        ResultSet tempRS = null;
        ArrayList<DisplayData> displayDataList = new ArrayList<>();
        // Create an SQL statement using the DB connection
        try {
            sql_stmt = dbConnection.createStatement();
            tempRS = sql_stmt.executeQuery(selectPlayDetailsSQL);
            if(tempRS != null){
                try {
                    while (tempRS.next()){
                        DisplayData tempDispData = new DisplayData(
                                tempRS.getInt(1),tempRS.getString(2),tempRS.getString(3),
                                tempRS.getString(4),tempRS.getString(5),tempRS.getString(6),
                                tempRS.getString(7),tempRS.getDate(8),tempRS.getLong(9)
                        );
                        displayDataList.add(tempDispData);
                    }
                } catch (SQLException e) {
                    lblStatusBar.setText(e.getMessage());
                }
            }
        } catch (SQLException e) {
            lblStatusBar.setText(e.getMessage());
        }
        return displayDataList;
    }

    /**
     * validate the form data needed to create a new player record in the DB
     * @return boolean
     */
    private boolean validateInput(){
        boolean isValid = false;
        // Validate for empty field content
        if(
           !txtFirstName.getText().isEmpty() && !txtLastName.getText().isEmpty()
           && !txtAreaAddress.getText().isEmpty() && !txtProvince.getText().isEmpty()
           && !txtPostalCode.getText().isEmpty() && !txtPhoneNumber.getText().isEmpty()
        ) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * get database connection needed to save to database
     * @return Connection
     */
    private Connection getDBConnection(){
        Connection tempConn = null;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            tempConn = DriverManager.getConnection("jdbc:oracle:thin:@199.212.26.208:1521:SQLD"," COMP228_W23_sy_113", "password");
        } catch(Exception e) {
            // e.printStackTrace();
            lblStatusBar.setText(e.getMessage());
            // if we cannot connect remotely, we try to connect via the schools network
            try{
                Class.forName("oracle.jdbc.driver.OracleDriver");
                tempConn = DriverManager.getConnection("jdbc:oracle:thin:@oracle1.centennialcollege.ca:1521:SQLD","COMP228_W23_sy_113", "password");
            } catch (ClassNotFoundException | SQLException ex) {
                lblStatusBar.setText(e.getMessage());
            }
        }
        return tempConn;
    }
}