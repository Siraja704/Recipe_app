package com.pantrypal;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FavoriteListController {

    @FXML
    private AnchorPane FirstAnchorPan;
    @FXML
    private AnchorPane SecondAnchorPan;
    @FXML
    private AnchorPane ThirdAnchorPan;
    @FXML
    private AnchorPane ForthAnchorPan;
    @FXML
    private AnchorPane FifthAnchorPan;
    @FXML
    private Label lab;
    @FXML
    private TextField nameField;
    @FXML
    private TextField massField;
    @FXML
    private TextField priceField;
    @FXML
    private TableView<Recipe> recipeTable;
    @FXML
    private TableColumn<Recipe, Integer> idColumn;
    @FXML
    private TableColumn<Recipe, String> nameColumn;
    @FXML
    private TableColumn<Recipe, String> ingredientsColumn;
    @FXML
    private TableColumn<Recipe, String> priceColumn;
    private ObservableList<Recipe> recipeList;
    private int id = -1;

    @FXML
    public void initialize() {
        FirstAnchorPan.widthProperty().addListener((observable, oldValue, newValue) -> {
            double newWidth = (newValue.doubleValue() / 2);
            SecondAnchorPan.setPrefWidth(newWidth);
            ThirdAnchorPan.setPrefWidth(newWidth);
            updateRecipeIds();
            Platform.runLater(() -> {
                centerForthAnchorPan();
                centerFifthAnchorPanWidth();
                centerFifthAnchorPanHeight();
                centerLab();
            });
        });
        SecondAnchorPan.widthProperty().addListener((observable, oldValue, newValue) -> {
            centerForthAnchorPan();
        });
        SecondAnchorPan.widthProperty().addListener((observable, oldValue, newValue) -> {
            centerFifthAnchorPanWidth();
        });
        SecondAnchorPan.heightProperty().addListener((observable, oldValue, newValue) -> {
            centerFifthAnchorPanHeight();
        });
        ThirdAnchorPan.widthProperty().addListener((observable, oldValue, newValue) -> {
            centerLab();
        });
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        ingredientsColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMass()));
        priceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrice()));
        loadRecipeData();
        recipeTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                nameField.setText(newValue.getName());
                massField.setText(newValue.getMass());
                priceField.setText(newValue.getPrice());
                id = newValue.getId();
            }
        });
    }

    private void loadRecipeData() {
        recipeList = FXCollections.observableArrayList();
        String sql = "SELECT * FROM recipe_table";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                recipeList.add(new Recipe(
                        resultSet.getInt("id"),
                        resultSet.getString("recipe_name"),
                        resultSet.getString("ingredients_name"),
                        resultSet.getString("total_price")
                ));
            }
            recipeTable.setItems(recipeList);
        } catch (SQLException e) {
            System.out.println("Error loading recipes: " + e.getMessage());
        }
    }

    private void updateRecipeIds() {
        try {
            Connection conn = DatabaseConnection.connect();
            String selectQuery = "SELECT id FROM recipe_table ORDER BY id ASC";
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            ResultSet resultSet = selectStmt.executeQuery();
            int newId = 1;
            String updateQuery = "UPDATE recipe_table SET id = ? WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            while (resultSet.next()) {
                int currentId = resultSet.getInt("id");
                updateStmt.setInt(1, newId);
                updateStmt.setInt(2, currentId);
                updateStmt.executeUpdate();
                newId++;
            }
            resultSet.close();
            selectStmt.close();
            updateStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error updating IDs: " + e.getMessage());
        }
    }

    private void centerFifthAnchorPanHeight() {
        double secondHeight = SecondAnchorPan.getHeight();
        double fifthHeight = FifthAnchorPan.getHeight();
        double newYPosition = (secondHeight - fifthHeight) / 2;
        FifthAnchorPan.setLayoutY(newYPosition);
    }

    private void centerFifthAnchorPanWidth() {
        double secondWidth = SecondAnchorPan.getWidth();
        double fifthWidth = FifthAnchorPan.getWidth();
        double newXPosition = (secondWidth - fifthWidth) / 2;
        FifthAnchorPan.setLayoutX(newXPosition);
    }

    private void centerForthAnchorPan() {
        double secondWidth = SecondAnchorPan.getWidth();
        double forthWidth = ForthAnchorPan.getWidth();
        double newXPosition = (secondWidth - forthWidth) / 2;
        ForthAnchorPan.setLayoutX(newXPosition);
    }

    private void centerLab() {
        double thirdWidth = ThirdAnchorPan.getWidth();
        double labelWidth = lab.getWidth();
        double newXPosition = (thirdWidth - labelWidth) / 2;
        lab.setLayoutX(newXPosition);
    }

    @FXML
    public void insertRecipe() {
        if (!recipeTable.getSelectionModel().isEmpty()) {
            showDialog("Warning", "Please Clear The Selection First.");
            return;
        }
        if (nameField.getText().isEmpty() || massField.getText().isEmpty() || priceField.getText().isEmpty()) {
            showDialog("Input Error", "All fields must be filled out.");
            return;
        }
        String name = nameField.getText();
        String mass = massField.getText();
        String price = priceField.getText();
        if (recipeExists(name, mass, price)) {
            showDialog("Duplicate Error", "A recipe with the same name, mass, and price already exists.");
            return;
        }
        String getLastIdSql = "SELECT MAX(id) FROM recipe_table";
        int newId = 1;
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement getIdStatement = connection.prepareStatement(getLastIdSql);
             ResultSet resultSet = getIdStatement.executeQuery()) {
            if (resultSet.next()) {
                newId = resultSet.getInt(1) + 1;
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving last ID: " + e.getMessage());
            return;
        }
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setGraphic(null);
        confirmationAlert.setContentText("Are you sure you want to Insert this recipe?");
        ButtonType result = confirmationAlert.showAndWait().orElse(ButtonType.CANCEL);
        if (result != ButtonType.OK) {
            return;
        }
        String sql = "INSERT INTO recipe_table (id, recipe_name, ingredients_name, total_price) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, newId);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, mass);
            preparedStatement.setString(4, price);
            preparedStatement.executeUpdate();
            loadRecipeData();
            showDialog("Success", "Recipe Inserted Successfully.");
        } catch (SQLException e) {
            System.out.println("Error inserting recipe: " + e.getMessage());
        }
    }

    public void updateRecipe() {
        if (id == -1) {
            showDialog("Input Error", "Select a row to update.");
            return;
        }
        if (nameField.getText().isEmpty() || massField.getText().isEmpty() || priceField.getText().isEmpty()) {
            showDialog("Input Error", "All fields must be filled out.");
            return;
        }
        String name = nameField.getText();
        String mass = massField.getText();
        String price = priceField.getText();
        if (recipeExists(name, mass, price)) {
            showDialog("Duplicate Error", "A recipe with the same name, mass, and price already exists.");
            return;
        }
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setGraphic(null);
        confirmationAlert.setContentText("Are you sure you want to Update this recipe?");
        ButtonType result = confirmationAlert.showAndWait().orElse(ButtonType.CANCEL);
        if (result != ButtonType.OK) {
            return;
        }
        String sql = "UPDATE recipe_table SET recipe_name = ?, ingredients_name = ?, total_price = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, mass);
            preparedStatement.setString(3, price);
            preparedStatement.setInt(4, id);
            preparedStatement.executeUpdate();
            loadRecipeData();
            showDialog("Success", "Recipe Updated Successfully.");
        } catch (SQLException e) {
            System.out.println("Error updating recipe: " + e.getMessage());
        }
    }

    public void deleteRecipe() {
        if (id == -1) {
            showDialog("Input Error", "Select a row to delete.");
            return;
        }
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setGraphic(null);
        confirmationAlert.setContentText("Are you sure you want to Delete this recipe?");
        ButtonType result = confirmationAlert.showAndWait().orElse(ButtonType.CANCEL);
        if (result != ButtonType.OK) {
            return;
        }
        String sql = "DELETE FROM recipe_table WHERE id = ?";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            loadRecipeData();
            showDialog("Success", "Recipe Deleted Successfully.");
        } catch (SQLException e) {
            System.out.println("Error deleting recipe: " + e.getMessage());
        }
    }

    public boolean recipeExists(String name, String mass, String price) {
        String checkSql = "SELECT * FROM recipe_table WHERE recipe_name = ? AND ingredients_name = ? AND total_price = ?";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(checkSql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, mass);
            preparedStatement.setString(3, price);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("Error checking if recipe exists: " + e.getMessage());
            return false;
        }
    }

    public void clearFields() {
        nameField.setText("");
        massField.setText("");
        priceField.setText("");
        id = -1;
        loadRecipeData();
    }

    @FXML
    public void goBack() {
        try {
            Parent favoriteList = FXMLLoader.load(getClass().getResource("HomePage.fxml"));
            Stage currentStage = (Stage) FirstAnchorPan.getScene().getWindow();
            boolean isMaximized = currentStage.isMaximized();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            Scene favoriteListScene = new Scene(favoriteList);
            currentStage.setScene(favoriteListScene);
            currentStage.setWidth(width);
            currentStage.setHeight(height);
            if (isMaximized) {
                currentStage.setMaximized(true);
            } else {
                currentStage.centerOnScreen();
            }
            currentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}