package com.pantrypal;

import org.json.JSONArray;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class PriceCalculator {
    private static final Map<String, Double> ingredientPrices;

    static {
        ingredientPrices = new HashMap<>();
        ingredientPrices.put("chicken legs", 3.0);
        ingredientPrices.put("salt", 0.1);
        ingredientPrices.put("black pepper", 0.1);
        ingredientPrices.put("olive oil", 0.5);
        ingredientPrices.put("onion", 0.3);
        ingredientPrices.put("broccoli", 2.0);
        ingredientPrices.put("garlic", 0.2);
        ingredientPrices.put("ground cumin", 0.1);
        ingredientPrices.put("turmeric", 0.1);
        ingredientPrices.put("cayenne pepper", 0.1);
        ingredientPrices.put("chicken broth", 1.0);
        ingredientPrices.put("basmati rice", 1.5);
        ingredientPrices.put("frozen peas", 0.5);
        ingredientPrices.put("parsley", 0.2);
        ingredientPrices.put("lemon", 0.5);
        ingredientPrices.put("rice", 1.5);
        ingredientPrices.put("tomatoes", 0.5);
        ingredientPrices.put("basil", 0.2);
        ingredientPrices.put("mozzarella", 1.0);
        ingredientPrices.put("spicy chicken sausages", 4.0);
        ingredientPrices.put("wild rice", 1.5);
        ingredientPrices.put("cheddar cheese", 2.0);
        ingredientPrices.put("Gruyère cheese", 2.5);
        ingredientPrices.put("heavy cream", 1.0);
        ingredientPrices.put("panko bread crumbs", 0.5);
        ingredientPrices.put("spring onions", 0.5);
        ingredientPrices.put("sesame seeds", 0.2);
        ingredientPrices.put("miso paste", 0.3);
        ingredientPrices.put("rice vinegar", 0.2);
        ingredientPrices.put("mirin", 0.2);
        ingredientPrices.put("ginger", 0.1);
        ingredientPrices.put("half-and-half", 0.5);
        ingredientPrices.put("cornstarch", 0.1);
        ingredientPrices.put("oyster sauce", 0.3);
        ingredientPrices.put("carrots", 0.5);
        ingredientPrices.put("celery", 0.4);
        ingredientPrices.put("bell peppers", 0.6);
        ingredientPrices.put("zucchini", 0.7);
        ingredientPrices.put("spinach", 1.0);
        ingredientPrices.put("kale", 1.2);
        ingredientPrices.put("sweet potatoes", 0.8);
        ingredientPrices.put("avocado", 1.5);
        ingredientPrices.put("coconut milk", 1.0);
        ingredientPrices.put("tofu", 2.0);
        ingredientPrices.put("black beans", 0.8);
        ingredientPrices.put("chickpeas", 0.9);
        ingredientPrices.put("quinoa", 2.0);
        ingredientPrices.put("pasta", 1.0);
        ingredientPrices.put("sour cream", 0.5);
        ingredientPrices.put("cream cheese", 1.0);
        ingredientPrices.put("ranch dressing", 0.8);
        ingredientPrices.put("honey", 0.5);
        ingredientPrices.put("vinegar", 0.2);
        ingredientPrices.put("mustard", 0.3);
        ingredientPrices.put("peanuts", 1.0);
        ingredientPrices.put("walnuts", 2.0);
        ingredientPrices.put("watercress", 0.7);
        ingredientPrices.put("skinless boneless chicken breast halves", 0.5);
        ingredientPrices.put("butter", 0.6);
        ingredientPrices.put("canned low-salt chicken broth", 1.0);
        ingredientPrices.put("whipping cream", 1.0);
        ingredientPrices.put("honey dijon mustard", 0.3);
    }

    public static double calculatePrice(JSONArray ingredients) {
        double totalCost = 0.0;

        for (int i = 0; i < ingredients.length(); i++) {
            String ingredient = normalizeIngredient(ingredients.getString(i));
            Double price = ingredientPrices.get(ingredient);
            if (price != null) {
                totalCost += price;
            }
        }
        return totalCost;
    }

    public static double getPrice(String ingredientName) {
        String normalizedIngredient = normalizeIngredient(ingredientName);
        Double price = ingredientPrices.get(normalizedIngredient);
        return price != null ? price : 0.0;
    }

    private static String normalizeIngredient(String ingredient) {
        ingredient = ingredient.toLowerCase().trim();
        ingredient = ingredient.replaceAll("bone-in|skin-on|\\d+\\s*\\p{Punct}?\\s*", "").trim();
        ingredient = ingredient.replaceAll("pieces|cups|pounds|tablespoons|teaspoons|cloves|large|small|cut into florets|chopped", "").trim();
        ingredient = ingredient.replaceAll("bone-in|skin-on|\\d+\\s*\\p{Punct}?\\s*|pieces|cups|pounds|tablespoons|teaspoons|cloves|large|small|cut into florets|chopped|bunch|sprigs|slices|package|jar|can|bottle", "").trim();
        return ingredient;
    }

    public static List<String> getRecipesWithIngredientPrices(List<Recipe> recipes, String input) {
        List<String> results = new ArrayList<>();
        String[] ingredientsInput = input.toLowerCase().split(",\\s*");

        for (Recipe recipe : recipes) {
            boolean containsAllIngredients = true;
            double totalPrice = 0.0;
            StringBuilder recipeOutput = new StringBuilder();

            recipeOutput.append(recipe.getName()).append(":\n")
                    .append("This recipe for ").append(recipe.getName()).append(" includes the following ingredients:\n\n");

            recipeOutput.append("Ingredients:\n");
            for (String ingredient : recipe.getIngredients()) {
                Double price = ingredientPrices.get(normalizeIngredient(ingredient));
                if (price != null) {
                    recipeOutput.append(" - ").append(ingredient).append(" - $").append(price).append("\n");
                    totalPrice += price;
                } else {
                    recipeOutput.append(" - ").append(ingredient).append(" - $0.0\n");
                }
            }

            for (String ingredientInput : ingredientsInput) {
                if (!recipe.containsIngredient(ingredientInput.trim())) {
                    containsAllIngredients = false;
                    break;
                }
            }

            if (containsAllIngredients) {
                recipeOutput.append("Final cost after subtracting specified ingredients: $").append(totalPrice).append("\n");
                results.add(recipeOutput.toString());
            }
        }
        return results;
    }
}