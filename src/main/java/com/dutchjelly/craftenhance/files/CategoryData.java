package com.dutchjelly.craftenhance.files;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.files.util.ConfigurationSerializeUtility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CategoryData implements ConfigurationSerializeUtility {
   private String recipeCategory;
   private String displayName;
   private final ItemStack recipeCategoryItem;
   private List<EnhancedRecipe> enhancedRecipes = new ArrayList();

   private CategoryData(ItemStack recipeCategoryItem, String recipeCategory, String displayName) {
      this.recipeCategoryItem = recipeCategoryItem;
      this.recipeCategory = recipeCategory;
      this.displayName = displayName;
   }

   public static CategoryData of(ItemStack recipeCategoryItem, String recipeCategory, String displayName) {
      return new CategoryData(recipeCategoryItem, recipeCategory, displayName);
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }

   public String getRecipeCategory() {
      return this.recipeCategory;
   }

   public void setRecipeCategory(String recipeCategory) {
      this.recipeCategory = recipeCategory;
   }

   public ItemStack getRecipeCategoryItem() {
      return this.recipeCategoryItem;
   }

   public List<EnhancedRecipe> getEnhancedRecipes() {
      return this.enhancedRecipes;
   }

   public List<EnhancedRecipe> getEnhancedRecipes(String recipeSeachFor) {
      return recipeSeachFor != null && !recipeSeachFor.equals("") ? (List)this.enhancedRecipes.stream().filter((x) -> {
         return x.getKey().contains(recipeSeachFor);
      }).collect(Collectors.toList()) : this.enhancedRecipes;
   }

   public void addEnhancedRecipes(EnhancedRecipe enhancedRecipes) {
      this.enhancedRecipes.add(enhancedRecipes);
   }

   public void setEnhancedRecipes(List<EnhancedRecipe> enhancedRecipes) {
      this.enhancedRecipes = enhancedRecipes;
   }

   public Map<String, Object> serialize() {
      return new HashMap<String, Object>() {
         {
            this.put("category.name", CategoryData.this.recipeCategory);
            this.put("category.category_item", CategoryData.this.recipeCategoryItem);
            this.put("category.display_name", CategoryData.this.displayName);
         }
      };
   }

   public static CategoryData deserialize(Map<String, Object> map) {
      String recipeCategory = (String)map.getOrDefault("category.name", (Object)null);
      ItemStack itemStack = (ItemStack)map.getOrDefault("category.category_item", (Object)null);
      String displayName = (String)map.getOrDefault("category.display_name", (Object)null);
      if (itemStack == null || itemStack.getType() == Material.AIR || itemStack.getType() == Material.getMaterial("END_PORTAL")) {
         Material material = Adapter.getMaterial("CRAFTING_TABLE");
         if (material == null) {
            material = Material.CRAFTING_TABLE;
         }

         itemStack = new ItemStack(material);
      }

      CategoryData categoryData = new CategoryData(itemStack, recipeCategory, displayName);
      categoryData.setDisplayName(displayName);
      return categoryData;
   }

   public String toString() {
      return "CategoryData{recipeCategory='" + this.recipeCategory + '\'' + ", displayName='" + this.displayName + '\'' + ", recipeCategoryItem=" + this.recipeCategoryItem + ", enhancedRecipes=" + this.enhancedRecipes + '}';
   }
}
