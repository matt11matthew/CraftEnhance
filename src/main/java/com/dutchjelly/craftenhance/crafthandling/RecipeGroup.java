package com.dutchjelly.craftenhance.crafthandling;

import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.bukkit.inventory.Recipe;

public class RecipeGroup {
   private List<Recipe> serverRecipes = new ArrayList();
   private List<EnhancedRecipe> enhancedRecipes = new ArrayList();

   public RecipeGroup() {
   }

   public RecipeGroup(List<EnhancedRecipe> enhanced, List<Recipe> server) {
      this.enhancedRecipes.addAll(enhanced);
      this.serverRecipes.addAll(server);
   }

   public RecipeGroup addIfNotExist(EnhancedRecipe enhancedRecipe) {
      if (!this.enhancedRecipes.contains(enhancedRecipe)) {
         this.enhancedRecipes.add(enhancedRecipe);
      }

      return this;
   }

   public RecipeGroup addIfNotExist(Recipe recipe) {
      if (!this.serverRecipes.contains(recipe)) {
         this.serverRecipes.add(recipe);
      }

      return this;
   }

   public RecipeGroup addAllNotExist(List<Recipe> recipes) {
      Iterator var2 = recipes.iterator();

      while(var2.hasNext()) {
         Recipe recipe = (Recipe)var2.next();
         if (!this.serverRecipes.contains(recipe)) {
            this.serverRecipes.add(recipe);
         }
      }

      return this;
   }

   public RecipeGroup mergeWith(@NonNull RecipeGroup othergroup) {
      if (othergroup == null) {
         throw new NullPointerException("othergroup is marked non-null but is null");
      } else {
         List<Recipe> mergedServerRecipes = new ArrayList();
         mergedServerRecipes.addAll(this.serverRecipes);
         mergedServerRecipes.addAll(othergroup.serverRecipes);
         this.serverRecipes = (List)mergedServerRecipes.stream().distinct().collect(Collectors.toList());
         List<EnhancedRecipe> mergedEnhancedRecipes = new ArrayList();
         mergedEnhancedRecipes.addAll(this.enhancedRecipes);
         mergedEnhancedRecipes.addAll(othergroup.enhancedRecipes);
         this.enhancedRecipes = (List)mergedEnhancedRecipes.stream().distinct().collect(Collectors.toList());
         return this;
      }
   }

   public List<Recipe> getServerRecipes() {
      return this.serverRecipes;
   }

   public void setServerRecipes(List<Recipe> serverRecipes) {
      this.serverRecipes = serverRecipes;
   }

   public List<EnhancedRecipe> getEnhancedRecipes() {
      return this.enhancedRecipes;
   }

   public void setEnhancedRecipes(List<EnhancedRecipe> enhancedRecipes) {
      this.enhancedRecipes = enhancedRecipes;
   }
}
