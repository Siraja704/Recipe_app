package com.pantrypal;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class HomePage {
    @FXML
    private TextField ingredientsField;
    @FXML
    private TextFlow outputArea;
    @FXML
    private AnchorPane thirdAnchorPane;
    @FXML
    private TextArea outputArea1;
    @FXML
    private TextArea outputArea11;
    String [] Recipe_Name = new String[10];
    String [] Ingredients_Name = new String[10];
    String [] Total_Price = new String[10];
    String Ingredients_Lines = "";
    String Description = "";
    String Total_Ingredients = "";
    String Total_Ingredients_1 = "";
    int index1 = 0;
    int index2 = 0;
    int index3 = 0;

    @FXML
    public void initialize() {
        fetchRecipeNames();
        thirdAnchorPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            double newHeight1 = (newValue.doubleValue() / 2) - 55;
            outputArea1.setPrefHeight(newHeight1);
            outputArea.requestLayout();
            double newHeight11 = (newValue.doubleValue() / 2) - 70;
            outputArea11.setPrefHeight(newHeight11);
            outputArea.requestLayout();
        });
    }

    @FXML
    private void fetchRecipes() {
        String ingredients = ingredientsField.getText();
        if (ingredients.isEmpty()) {
            outputArea.getChildren().clear();
            outputArea.getChildren().add(new Text("Please Enter Some Ingredients."));
            return;
        }
        try {
            String jsonResponse = RecipeFetcher.fetchRecipes(ingredients);
            String processedRecipes = RecipeFetcher.processRecipes(jsonResponse);
            String[] lines = processedRecipes.split("\n");
            String[] userIngredients = ingredients.split(",\\s*");
            double totalRemainingCost = 0.0;
            Text heartText;
            outputArea.getChildren().clear();
            for (String line : lines) {
                heartText = new Text("🖤 ");
                heartText.setStyle("-fx-font-size: 12; -fx-font-family: 'Segoe UI Emoji'; -fx-cursor: hand;");
                String recipeName = removeNumbering(line);
                boolean recipeExists = recipeExistsInDatabase(recipeName);
                if (recipeExists) {
                    heartText.setStyle("-fx-fill: red; -fx-font-size: 12; -fx-font-family: 'Segoe UI Emoji'; -fx-cursor: hand;");
                } else {
                    heartText.setStyle("-fx-fill: black; -fx-font-size: 12; -fx-font-family: 'Segoe UI Emoji'; -fx-cursor: hand;");
                }
                boolean excludeIngredient = false;
                if (line.matches("^\\d+\\..*"))
                {
                    Description = "";
                    outputArea1.appendText(line + "\n");
                    Description = line + "\n     This recipe for " + recipeName + " includes the following ingredients:\n\n";;
                    Recipe_Name [index1] = removeNumbering (line);
                    index1++;
                    outputArea.getChildren().add(heartText);
                }
                else if (line.startsWith("This Recipe"))
                {
                    outputArea1.appendText("\n");
                }
                else if (line.startsWith("Ingredients:"))
                {
                    outputArea1.appendText(line + "\n");
                    Description += "     " + line + "\n";
                }
                else if (line.startsWith(" - "))
                {
                    String cleanedIng = line.replace(" - ", "").replaceAll("\\b(?:tablespoon|cup|teaspoon|pound|ounce|gram|ml|liter)\\b", "").trim();
                    String nutrientInfo = getIngredientNutrients(cleanedIng.split("\\$")[0].trim());
                    Description += "     " + line + " (" + nutrientInfo + ")\n";
                    Ingredients_Lines += line + " (" + nutrientInfo + ")\n";
                    for (String userIngredient : userIngredients) {
                        if (line.toLowerCase().contains(userIngredient.toLowerCase())) {
                            excludeIngredient = true;
                            break;
                        }
                    }
                    if (!excludeIngredient) {
                        outputArea1.appendText(line + nutrientInfo + "\n");
                        Total_Ingredients += line + nutrientInfo + "\n";
                    }
                    Total_Ingredients_1 += line + nutrientInfo + "\n";
                }
                else if (line.startsWith("Final cost"))
                {
                    outputArea1.appendText(calculateNutrition(Total_Ingredients) + "\n\n");
                    String Ingredients_Line = removeLastNewline(Ingredients_Lines);
                    String ing = calculateNutrition(Total_Ingredients_1);
                    outputArea.getChildren().add(new Text(Description + "      " + ing + "\n\n"));
                    Ingredients_Name [index2] = Ingredients_Line;
                    index2++;
                    Total_Price [index3] = ing;
                    index3++;
                    Description = "";
                    Total_Ingredients = "";
                    Ingredients_Lines = "";
                    Total_Ingredients_1 = "";
                }
                heartText.setOnMouseClicked((MouseEvent event) -> handleHeartClick(line, Recipe_Name, Ingredients_Name, Total_Price));
            }
            String output = outputArea1.getText();
            outputArea1.setText("");
            outputArea1.setText(output);
        } catch (Exception e) {
            outputArea.getChildren().clear();
            outputArea.getChildren().add(new Text("Please Enter correct Ingredients: " + e.getMessage()));
            System.out.println("Please Enter correct Ingredients: " + e.getMessage());
        }
    }

    public static String calculateNutrition(String ingredients) {
        double totalProtein = 0.0;
        double totalFat = 0.0;
        double totalCarbs = 0.0;
        String[] ingredientLines = ingredients.split("\n");
        for (String line : ingredientLines) {
            double protein = extractValue(line, "Protein:");
            double fat = extractValue(line, "Fat:");
            double carbs = extractValue(line, "Carbs:");
            totalProtein += protein;
            totalFat += fat;
            totalCarbs += carbs;
        }
        return String.format("Total: Protein: %.2f g, Fat: %.2f g, Carbs: %.2f g",
                totalProtein, totalFat, totalCarbs);
    }

    public static double extractValue(String line, String nutrient) {
        int startIndex = line.indexOf(nutrient) + nutrient.length();
        int endIndex = line.indexOf("g", startIndex);
        String value = line.substring(startIndex, endIndex).trim();
        return Double.parseDouble(value);
    }

    private boolean recipeExistsInDatabase(String recipeName) {
        boolean exists = false;
        try {
            Connection conn = DatabaseConnection.connect();
            String query = "SELECT COUNT(*) FROM recipe_table WHERE recipe_name = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, recipeName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                exists = rs.getInt(1) > 0;
            }
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error checking recipe existence: " + e.getMessage());
        }
        return exists;
    }

    public String removeLastNewline(String input) {
        if (input != null && input.endsWith("\n")) {
            return input.substring(0, input.length() - 1);
        }
        return input;
    }

    private String removeNumbering(String recipe) {
        return recipe.replaceAll("^\\d+\\.\\s*", "")
                .replaceAll(":\\s*$", "");
    }

    private void handleHeartClick(String recipeLine, String [] Recipe_na, String [] Ingredients_na, String [] Total_pri) {
        String recipeName = removeNumbering(recipeLine);
        int id = Integer.parseInt(remove(recipeLine));
        if (recipeExistsInDatabase(recipeName)) {
            removeRecipeFromDatabase(recipeName);
        } else {
            id = id-1;
            saveRecipeToDatabase(id, Recipe_na, Ingredients_na, Total_pri);
        }
        index1 = 0;
        index2 = 0;
        index3 = 0;
        Ingredients_Lines = "";
        Description = "";
        fetchRecipeNames();
        fetchRecipes();
    }

    public void showDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public String remove(String recipeLine) {
        String numberOnly = recipeLine.replaceAll("^\\D*(\\d+).*", "$1");
        return numberOnly;
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

    private void saveRecipeToDatabase(int id, String [] Recipe_na, String [] Ingredients_na, String [] Total_pri ) {
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
        try {
            Connection conn = DatabaseConnection.connect();
            String query = "INSERT INTO recipe_table (id, recipe_name, ingredients_name, total_price) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, newId);
            stmt.setString(2, Recipe_na [id]);
            stmt.setString(3, Ingredients_na [id]);
            stmt.setString(4, Total_pri [id]);
            stmt.executeUpdate();
            showDialog("Success", "Recipe Inserted Successfully.");
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error saving recipe: " + e.getMessage());
        }
    }

    private void removeRecipeFromDatabase(String recipeName) {
        try {
            Connection conn = DatabaseConnection.connect();
            String query = "DELETE FROM recipe_table WHERE recipe_name = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, recipeName);
            stmt.executeUpdate();
            updateRecipeIds();
            showDialog("Success", "Recipe Deleted Successfully.");
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error removing recipe: " + e.getMessage());
        }
    }

    public String getIngredientNutrients(String ingredient) {
        int retries = 0;
        while (retries < 5) {
            try {
                String appId = "db1861e1";
                String appKey = "559d2eae3a32847a019d8a57241dcf68";
                String urlString = "https://api.edamam.com/api/nutrition-data?app_id=" + appId
                        + "&app_key=" + appKey
                        + "&ingr=" + URLEncoder.encode(ingredient, StandardCharsets.UTF_8.toString());
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                JSONObject jsonResponse = new JSONObject(response.toString());
                if (!jsonResponse.has("totalNutrients")) {
                    return "Nutritional data not available";
                }
                JSONObject totalNutrients = jsonResponse.getJSONObject("totalNutrients");
                double protein = totalNutrients.has("PROCNT") ? totalNutrients.getJSONObject("PROCNT").getDouble("quantity") : 0.0;
                double fat = totalNutrients.has("FAT") ? totalNutrients.getJSONObject("FAT").getDouble("quantity") : 0.0;
                double carbs = totalNutrients.has("CHOCDF") ? totalNutrients.getJSONObject("CHOCDF").getDouble("quantity") : 0.0;
                return String.format("Protein: %.2f g, Fat: %.2f g, Carbs: %.2f g", protein, fat, carbs);
            } catch (Exception e) {
                System.out.println("Error fetching ingredient nutrients: " + e.getMessage());
                retries++;
                if (retries == 5) {
                    return "Nutritional data not available after retries";
                }
            }
        }
        return "Nutritional data not available after retries";
    }


    @FXML
    public void fetchRecipeNames() {
        StringBuilder recipeNames = new StringBuilder();
        try {
            Connection conn = DatabaseConnection.connect();
            String query = "SELECT recipe_name FROM recipe_table";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            int index = 1;
            while (rs.next()) {
                String recipeName = rs.getString("recipe_name");
                recipeNames.append(index).append(". ").append(recipeName).append("\n");
                index++;
            }
            outputArea11.setText(recipeNames.toString());
            conn.close();
        } catch (SQLException e) {
            outputArea11.setText("Error fetching recipe names: " + e.getMessage());
        }
    }

    @FXML
    public void goNext() {
        try {
            Parent favoriteList = FXMLLoader.load(getClass().getResource("FavoriteList.fxml"));
            Stage currentStage = (Stage) thirdAnchorPane.getScene().getWindow();
            boolean isMaximized = currentStage.isMaximized();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            Scene favoriteListScene = new Scene(favoriteList);
            currentStage.setScene(favoriteListScene);
            currentStage.setWidth(width);
            currentStage.setHeight(height);
            if (isMaximized) {
                currentStage.setMaximized(true);
                outputArea.requestLayout();
            } else {
                currentStage.centerOnScreen();
            }
            currentStage.show();
        } catch (Exception e) {
            outputArea.getChildren().add(new Text("Error loading FavoriteList: " + e.getMessage()));
        }
    }
}