package com.dutchjelly.craftenhance.files;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.ServerLoadable;
import com.dutchjelly.craftenhance.files.util.ConfigurationSerializeUtility;
import com.dutchjelly.craftenhance.files.util.SimpleYamlHelper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class CategoryDataCache extends SimpleYamlHelper {
   private final Map<String, CategoryData> recipeCategorys = new HashMap();

   public CategoryDataCache() {
      super("categorys.yml", true, true);
   }

   public CategoryData of(String category, ItemStack itemStack, String displayName) {
      return CategoryData.of(itemStack, category, displayName);
   }

   public Collection<CategoryData> values() {
      return this.recipeCategorys.values();
   }

   public List<Recipe> getServerRecipes() {
      return (List)this.recipeCategorys.values().stream().flatMap((categoryData) -> {
         return categoryData.getEnhancedRecipes().stream().map(ServerLoadable::getServerRecipe);
      }).collect(Collectors.toList());
   }

   @Nullable
   public CategoryData get(String category) {
      return (CategoryData)this.recipeCategorys.get(category);
   }

   public void put(String category, ItemStack itemStack, String displayName) {
      this.recipeCategorys.put(category, this.of(category, itemStack, displayName));
   }

   public void put(String category, CategoryData categoryData) {
      this.recipeCategorys.put(category, categoryData);
   }

   public void remove(String category) {
      this.recipeCategorys.remove(category);
   }

   public CategoryData move(String oldCategory, String category, EnhancedRecipe... recipes) {
      CategoryData categoryDataOld = (CategoryData)this.getRecipeCategorys().get(oldCategory);
      CategoryData existingCategory = (CategoryData)this.getRecipeCategorys().get(category);
      CategoryData categoryData = this.createCategoryData(categoryDataOld, category, existingCategory != null);
      if (categoryDataOld != null && recipes != null && recipes.length > 0) {
         EnhancedRecipe[] var7 = recipes;
         int var8 = recipes.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            EnhancedRecipe recipe = var7[var9];
            categoryDataOld.getEnhancedRecipes().remove(recipe);
            if (existingCategory != null) {
               recipe.setRecipeCategory(category);
               if (!this.containsRecipe(existingCategory.getEnhancedRecipes(), recipe.getKey())) {
                  existingCategory.getEnhancedRecipes().add(recipe);
               }
            } else if (categoryData != null) {
               recipe.setRecipeCategory(category);
               if (!this.containsRecipe(categoryData.getEnhancedRecipes(), recipe.getKey())) {
                  categoryData.getEnhancedRecipes().add(recipe);
               }
            }
         }
      } else if (categoryDataOld != null) {
         this.collectToNewList(category, categoryDataOld.getEnhancedRecipes(), (List)(existingCategory != null ? existingCategory.getEnhancedRecipes() : (categoryData != null ? categoryData.getEnhancedRecipes() : new ArrayList())));
         this.remove(oldCategory);
      }

      if (existingCategory != null) {
         this.getRecipeCategorys().put(category, existingCategory);
         return existingCategory;
      } else if (categoryData != null) {
         this.getRecipeCategorys().put(category, categoryData);
         return categoryData;
      } else {
         return null;
      }
   }

   public boolean addCategory(String category, ItemStack itemStack, String displayname) {
      CategoryData categoryData = (CategoryData)this.getRecipeCategorys().get(category);
      if (categoryData != null) {
         return true;
      } else {
         this.recipeCategorys.put(category, this.of(category, itemStack, displayname));
         return false;
      }
   }

   public Set<String> getCategoryNames() {
      return this.recipeCategorys.keySet();
   }

   private Map<String, CategoryData> getRecipeCategorys() {
      return this.recipeCategorys;
   }

   protected void saveDataToFile(File file) {
      if (!file.exists()) {
         try {
            file.createNewFile();
         } catch (IOException var5) {
            var5.printStackTrace();
         }
      }

      FileConfiguration fileConfiguration = this.getCustomConfig();
      if (fileConfiguration == null) {
         fileConfiguration = YamlConfiguration.loadConfiguration(file);
      }

      ((FileConfiguration)fileConfiguration).set("Categorys", (Object)null);
      Iterator var3 = this.recipeCategorys.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<String, CategoryData> entry = (Entry)var3.next();
         this.setData(file, "Categorys." + (String)entry.getKey(), (ConfigurationSerializeUtility)entry.getValue());
      }

   }

   protected void loadSettingsFromYaml(File file) {
      ConfigurationSection templateConfig = this.getCustomConfig().getConfigurationSection("Categorys");
      if (templateConfig != null) {
         Iterator var3 = templateConfig.getKeys(false).iterator();

         while(var3.hasNext()) {
            String category = (String)var3.next();
            CategoryData categoryData = (CategoryData)this.getData("Categorys." + category, CategoryData.class);
            if (categoryData != null) {
               this.recipeCategorys.put(category, categoryData);
            }
         }

      }
   }

   public void collectToNewList(String category, List<EnhancedRecipe> fromList, List<EnhancedRecipe> toList) {
      this.moveRecipesCategory(fromList, category).forEach((recipe) -> {
         if (!this.containsRecipe(toList, recipe.getKey())) {
            toList.add(recipe);
         }

      });
   }

   public boolean containsRecipe(List<EnhancedRecipe> enhancedRecipes, String recipeKey) {
      Iterator var3 = enhancedRecipes.iterator();

      EnhancedRecipe recipe;
      do {
         if (!var3.hasNext()) {
            return false;
         }

         recipe = (EnhancedRecipe)var3.next();
      } while(!recipe.getKey().equals(recipeKey));

      return true;
   }

   public List<EnhancedRecipe> moveRecipesCategory(List<EnhancedRecipe> enhancedRecipes, String newCategory) {
      if (enhancedRecipes != null) {
         enhancedRecipes.forEach((recipe) -> {
            recipe.setRecipeCategory(newCategory);
         });
      }

      return enhancedRecipes;
   }

   public CategoryData createCategoryData(CategoryData categoryDataOld, String category, boolean doCategoryAllredyExist) {
      CategoryData categoryData = null;
      if (!doCategoryAllredyExist) {
         if (categoryDataOld != null) {
            categoryData = this.of(category, categoryDataOld.getRecipeCategoryItem(), categoryDataOld.getDisplayName());
         } else {
            categoryData = this.of(category, new ItemStack(Adapter.getMaterial("CRAFTING_TABLE")), (String)null);
         }
      }

      return categoryData;
   }
}
