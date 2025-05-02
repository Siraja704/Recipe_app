package com.pantrypal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

public class RecipeFetcher {
    private static final String APP_ID = "74715e3f";
    private static final String APP_KEY = "a9a851a7201296cee6d1281b2f4acf00";

    public static String fetchRecipes(String ingredients) throws Exception {
        String encodedIngredients = URLEncoder.encode(ingredients, StandardCharsets.UTF_8.toString());
        String urlString = "https://api.edamam.com/search?q=" + encodedIngredients + "&app_id=" + APP_ID + "&app_key=" + APP_KEY;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        conn.disconnect();
        return content.toString();
    }

    public static String processRecipes(String jsonResponse) {
        StringBuilder result = new StringBuilder();
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray recipes = jsonObject.getJSONArray("hits");
        for (int i = 0; i < recipes.length(); i++) {
            JSONObject recipe = recipes.getJSONObject(i).getJSONObject("recipe");
            String name = recipe.getString("label");
            JSONArray ingredients = recipe.getJSONArray("ingredientLines");
            result.append(i + 1).append(". ").append(name).append(":\n")
                    .append("\n\n")
                    .append("Ingredients:\n");
            for (int j = 0; j < ingredients.length(); j++) {
                String ingredient = ingredients.getString(j);
                result.append(" - ").append(ingredient).append(" - ").append("\n");
            }
            result.append("Final cost after subtracting specified ingredients: ").append("\n\n");
        }
        return result.toString();
    }
}