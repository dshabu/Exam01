package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSnackbar;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.net.URL;
import java.sql.*;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

public class Controller implements Initializable {

    @FXML
    public JFXListView<Integer> allNumList;

    @FXML
    public JFXSnackbar snackBar;

    @FXML
    public TextField minField;

    @FXML
    public TextField maxField;

    @FXML
    public JFXButton runBtn;

    @FXML
    public JFXButton loadBtn;

    @FXML
    public Label genNumLabel;

    private static Connection dbConn;

    public static void closeDatabase() {
        if (dbConn != null) {
            try {
                System.out.println("INFO: Closing Database");
                dbConn.close();
            } catch (SQLException e) {
                System.out.println(e.getErrorCode());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            initializeDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        UnaryOperator<TextFormatter.Change> numericFilter = change -> {
            String value = change.getText();
            if (value.matches("\\d*")) {
                return change;
            }
            return null;
        };

        minField.setTextFormatter(new TextFormatter<String>(numericFilter));
        maxField.setTextFormatter(new TextFormatter<String>(numericFilter));

        loadBtn.setOnAction(actionEvent -> fromTableIntoList());
        runBtn.setOnAction(actionEvent -> runAction());
    }

    public void runAction() {
        if (!(minField.getText().isBlank() && maxField.getText().isBlank())) {
            int min = Integer.parseInt(minField.getText());
            int max = Integer.parseInt(maxField.getText());

            int number = new Random().nextInt(max + 1) + min;
            genNumLabel.setText("" + number);
            insertIntoList(number);
            insertIntoTable(number);
        }
    }

    private void insertIntoTable(int i) {
        try (Statement stmt = dbConn.createStatement()) {
            stmt.execute(String.format("INSERT INTO Numbers VALUES %d", i));
        } catch (SQLException e) {
            System.out.println(e.getSQLState());
            e.printStackTrace();
        }
    }

    private void initializeDatabase() throws SQLException {
        final String DBSTR = "jdbc:derby:NumbersDB;create=true";

        try {
            dbConn = DriverManager.getConnection(DBSTR);
            Statement stmt = dbConn.createStatement();

            stmt.execute("CREATE TABLE Numbers (num int)");
            dbConn.commit();
        } catch (SQLException e) {
            if (e.getErrorCode() == 30000) {
                System.out.println("WARN: Table already exists.");
            } else {
                throw e;
            }
        }
    }

    private void fromTableIntoList() {
        allNumList.getItems().clear();

        try (Statement stmt = dbConn.createStatement()) {
            ResultSet results = stmt.executeQuery("SELECT * FROM Numbers");

            while (results.next()) {
                insertIntoList(results.getInt("num"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertIntoList(int i) {
        ObservableList<Integer> numList = allNumList.getItems();
        numList.add(i);
    }
}
