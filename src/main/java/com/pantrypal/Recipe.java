package com.pantrypal;

import java.util.List;

public class Recipe {
    private int id;
    private String recipeName;
    private String ingredientsName;
    private String totalPrice;
    private List<String> ingredients;

    public Recipe(int id, String recipeName, String ingredientsName, String totalPrice) {
        this.id = id;
        this.recipeName = recipeName;
        this.ingredientsName = ingredientsName;
        this.totalPrice = totalPrice;
    }

    public Recipe(String name, List<String> ingredients) {
        this.recipeName = name;
        this.ingredients = ingredients;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public boolean containsIngredient(String ingredient) {
        return ingredients.stream().anyMatch(i -> i.toLowerCase().contains(ingredient.toLowerCase()));
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return recipeName;
    }
    public void setName(String name)
    {
        this.recipeName = name;
    }

    public String getMass()
    {
        return ingredientsName;
    }
    public void setMass(String mass)
    {
        this.ingredientsName = mass;
    }

    public String getPrice()
    {
        return totalPrice;
    }

    public void setPrice(String price)
    {
        this.totalPrice = price;
    }
}