package com.dutchjelly.craftenhance.crafthandling;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.FurnaceRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.RecipeType;
import com.dutchjelly.craftenhance.files.CategoryData;
import com.dutchjelly.craftenhance.files.CategoryDataCache;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import com.dutchjelly.craftenhance.updatechecking.VersionChecker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class RecipeLoader {
   private static RecipeLoader instance = null;
   private List<Recipe> serverRecipes = new ArrayList();
   private List<Recipe> disabledServerRecipes = new ArrayList();
   private List<EnhancedRecipe> loadedRecipes = new ArrayList();
   private Map<ItemStack, ItemStack> similarVanillaRecipe = new HashMap();
   private Map<String, Recipe> loaded = new HashMap();
   private Map<RecipeType, List<RecipeGroup>> mappedGroupedRecipes = new HashMap();
   private final CategoryDataCache categoryDataCache;
   private final Server server;

   public static RecipeLoader getInstance() {
      return instance == null ? (instance = new RecipeLoader(Bukkit.getServer(), CraftEnhance.self().getCategoryDataCache())) : instance;
   }

   public static void clearInstance() {
      Iterator var0 = getInstance().getLoadedRecipes().iterator();

      while(var0.hasNext()) {
         EnhancedRecipe loaded = (EnhancedRecipe)var0.next();
         instance.unloadRecipe(loaded.getServerRecipe());
      }

      instance = null;
   }

   private RecipeLoader(Server server, CategoryDataCache categoryDataCache) {
      this.server = server;
      Iterator var10000 = server.recipeIterator();
      List var10001 = this.serverRecipes;
      var10000.forEachRemaining(var10001::add);
      RecipeType[] var3 = RecipeType.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         RecipeType type = var3[var5];
         this.mappedGroupedRecipes.put(type, new ArrayList());
      }

      this.categoryDataCache = categoryDataCache;
   }

   private RecipeGroup addGroup(List<Recipe> serverRecipes, EnhancedRecipe enhancedRecipe) {
      Debug.Send((Object)"[AddGroup] is now add recipe group.");
      List<RecipeGroup> groupedRecipes = (List)this.mappedGroupedRecipes.get(enhancedRecipe.getType());
      if (groupedRecipes == null) {
         groupedRecipes = new ArrayList();
      }

      Iterator var4 = ((List)groupedRecipes).iterator();
      if (var4.hasNext()) {
         RecipeGroup group = (RecipeGroup)var4.next();
         group.addAllNotExist(serverRecipes);
         return group.addIfNotExist(enhancedRecipe);
      } else {
         RecipeGroup newGroup = new RecipeGroup();
         if (((List)groupedRecipes).isEmpty()) {
            newGroup.addIfNotExist(enhancedRecipe);
            newGroup.setServerRecipes(serverRecipes);
            ((List)groupedRecipes).add(newGroup);
         }

         return newGroup;
      }
   }

   public RecipeGroup findGroup(EnhancedRecipe recipe) {
      return this.mappedGroupedRecipes.get(recipe.getType()).stream().filter((x) -> {
         return x.getEnhancedRecipes().contains(recipe);
      }).findFirst().orElse(null);
   }

   public List<RecipeGroup> findGroupsByResult(ItemStack result, RecipeType type) {
      List<RecipeGroup> originGroups = new ArrayList();
      Iterator var4 = ((List)this.mappedGroupedRecipes.get(type)).iterator();

      while(var4.hasNext()) {
         RecipeGroup group = (RecipeGroup)var4.next();
         if (group.getEnhancedRecipes().stream().anyMatch((x) -> {
            return result.equals(x.getResult());
         })) {
            originGroups.add(group);
         } else if (group.getServerRecipes().stream().anyMatch((x) -> {
            return result.equals(x.getResult());
         })) {
            originGroups.add(group);
         }
      }

      return originGroups;
   }

   public RecipeGroup findMatchingGroup(ItemStack[] matrix, RecipeType type) {
      Iterator var3 = ((List)this.mappedGroupedRecipes.get(type)).iterator();

      RecipeGroup group;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         group = (RecipeGroup)var3.next();
      } while(!group.getEnhancedRecipes().stream().anyMatch((x) -> {
         return x.matches(matrix);
      }));

      return group;
   }

   public RecipeGroup findSimilarGroup(EnhancedRecipe recipe) {
//      for (RecipeGroup x : this.mappedGroupedRecipes.get(recipe.getType())) {
//          return x.getEnhancedRecipes().stream().anyMatch((y) -> {
//            return y.isSimilar(recipe);
//
//      }
      return this.mappedGroupedRecipes.get(recipe.getType()).stream().filter(o-> {
         return o.getEnhancedRecipes().stream().anyMatch((y) -> {
            return y.isSimilar(recipe);
         }) || o.getServerRecipes().stream().anyMatch((y) -> {
            return recipe.isSimilar(y);
         });
      }).findFirst().orElse((null));
   }

   public boolean isLoadedAsServerRecipe(EnhancedRecipe recipe) {
      return this.loaded.containsKey(recipe.getKey());
   }

   private void unloadCehRecipes() {
      Iterator it = this.server.recipeIterator();

      while(it.hasNext()) {
         Recipe r = (Recipe)it.next();
         if (Adapter.ContainsSubKey(r, "cehrecipe")) {
            it.remove();
         }
      }

   }

   private void unloadRecipe(Recipe r) {
      Iterator it = this.server.recipeIterator();

      Recipe currentRecipe;
      do {
         if (!it.hasNext()) {
            return;
         }

         currentRecipe = (Recipe)it.next();
      } while(!currentRecipe.equals(r));

      it.remove();
   }

   public void unloadAll() {
      this.disabledServerRecipes.forEach((x) -> {
         this.enableServerRecipe(x);
      });
      this.mappedGroupedRecipes.clear();
      RecipeType[] var1 = RecipeType.values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         RecipeType type = var1[var3];
         this.mappedGroupedRecipes.put(type, new ArrayList());
      }

      this.serverRecipes.clear();
      this.loaded.clear();
      this.unloadCehRecipes();
   }

   public void unloadRecipe(EnhancedRecipe recipe) {
      RecipeGroup group = this.findGroup(recipe);
      this.loadedRecipes.remove(recipe);
      CategoryData categoryData = this.categoryDataCache.get(recipe.getRecipeCategory());
      if (categoryData != null && categoryData.getEnhancedRecipes() != null) {
         categoryData.getEnhancedRecipes().remove(recipe);
      }

      if (group == null) {
         this.printGroupsDebugInfo();
         Bukkit.getLogger().log(Level.SEVERE, "Could not unload recipe from groups because it doesn't exist.");
      } else {
         Recipe serverRecipe = (Recipe)this.loaded.get(recipe.getKey());
         if (serverRecipe != null) {
            this.loaded.remove(recipe.getKey());
            this.unloadRecipe(serverRecipe);
         }

         if (group.getEnhancedRecipes().size() == 1) {
            ((List)this.mappedGroupedRecipes.get(recipe.getType())).remove(group);
         } else {
            group.getEnhancedRecipes().remove(recipe);
         }

         Debug.Send((Object)"Unloaded a recipe");
         this.printGroupsDebugInfo();
      }
   }

   public void loadRecipe(@NonNull EnhancedRecipe recipe) {
      if (recipe == null) {
         throw new NullPointerException("recipe is marked non-null but is null");
      } else if (recipe.validate() != null) {
         Messenger.Error("There's an issue with recipe " + recipe.getKey() + ": " + recipe.validate());
      } else {
         boolean containsRecipe = this.loaded.containsKey(recipe.getKey());
         if (containsRecipe) {
            this.unloadRecipe(recipe);
         }

         List<Recipe> similarServerRecipes = new ArrayList();
         Iterator var4 = this.serverRecipes.iterator();

         Recipe serverRecipe;
         while(var4.hasNext()) {
            serverRecipe = (Recipe)var4.next();
            if (recipe.isSimilar(serverRecipe)) {
               similarServerRecipes.add(serverRecipe);
            }
         }

         Recipe alwaysSimilar = null;
         Iterator var8 = similarServerRecipes.iterator();

         while(var8.hasNext()) {
            Recipe r = (Recipe)var8.next();
            if (recipe.isAlwaysSimilar(r) && (!CraftEnhance.self().getVersionChecker().newerThan(VersionChecker.ServerVersion.v1_12) || (!(r instanceof ShapedRecipe) || !((ShapedRecipe)r).getKey().getNamespace().contains("craftenhance")) && (!(r instanceof ShapelessRecipe) || !((ShapelessRecipe)r).getKey().getNamespace().contains("craftenhance")))) {
               alwaysSimilar = r;
               break;
            }
         }

         this.cacheSimilarVanilliaRecipe(recipe);
         if (alwaysSimilar == null) {
            serverRecipe = recipe.getServerRecipe();
            if (serverRecipe == null) {
               Debug.Send((Object)("Added server recipe is null for " + recipe.getKey()));
               CraftEnhance.self().getLogger().log(Level.WARNING, "Recipe will not be cached becuse the result is null or invalid material type.");
               return;
            }

            if (!containsRecipe) {
               this.server.addRecipe(serverRecipe);
            }

            Debug.Send((Object)("Added server recipe for " + serverRecipe.getResult()));
            this.loaded.put(recipe.getKey(), serverRecipe);
         } else {
            Debug.Send((Object)("Didn't add server recipe for " + recipe.getKey() + " because a similar one was already loaded: " + alwaysSimilar.toString() + " with the result " + alwaysSimilar.getResult().toString()));
         }

         this.loadToCache(recipe);
         this.addGroup(similarServerRecipes, recipe);
         Debug.Send((Object)"AddGroupe done.");
         this.loadedRecipes.add(recipe);
      }
   }

   private void loadToCache(@NonNull EnhancedRecipe recipe) {
      if (recipe == null) {
         throw new NullPointerException("recipe is marked non-null but is null");
      } else {
         String category = recipe.getRecipeCategory();
         if (recipe instanceof FurnaceRecipe) {
            category = category != null && !category.equals("") ? category : "furnace";
         } else {
            category = category != null && !category.equals("") ? category : "default";
         }

         if (recipe.getRecipeCategory() == null) {
            recipe.setRecipeCategory(category);
         }

         CategoryData recipeCategory = this.categoryDataCache.get(category);
         if (recipeCategory != null) {
            List<EnhancedRecipe> enhancedRecipeList = recipeCategory.getEnhancedRecipes();
            if (enhancedRecipeList.stream().noneMatch((cachedRecipe) -> {
               return cachedRecipe.getKey().equals(recipe.getKey());
            })) {
               enhancedRecipeList.add(recipe);
            }
         } else {
            ItemStack itemStack;
            if (recipe instanceof FurnaceRecipe) {
               itemStack = new ItemStack(Adapter.getMaterial("FURNACE"));
            } else {
               itemStack = new ItemStack(Adapter.getMaterial("CRAFTING_TABLE"));
            }

            recipeCategory = this.categoryDataCache.of(category, itemStack, (String)null);
            recipeCategory.addEnhancedRecipes(recipe);
         }

         this.categoryDataCache.put(category, recipeCategory);
      }
   }

   public void cacheSimilarVanilliaRecipe(EnhancedRecipe recipe) {
      if (recipe instanceof FurnaceRecipe) {
         Debug.Send((Object)"Start to add Furnace recipe");
         Iterator var2 = this.serverRecipes.iterator();

         while(var2.hasNext()) {
            Recipe r = (Recipe)var2.next();
            if (r instanceof org.bukkit.inventory.FurnaceRecipe && recipe.getContent().length > 0 && recipe.getContent()[0] != null) {
               org.bukkit.inventory.FurnaceRecipe serverRecipe = (org.bukkit.inventory.FurnaceRecipe)r;
               ItemStack itemStack = serverRecipe.getInput();
               Debug.Send((Object)("Added Furnace recipe for " + serverRecipe.getResult()));
               if (recipe.getContent()[0].getType() == itemStack.getType()) {
                  this.similarVanillaRecipe.put(serverRecipe.getInput(), serverRecipe.getResult());
               }
            }
         }

      }
   }

   public List<Recipe> getLoadedServerRecipes() {
      return new ArrayList(this.loaded.values());
   }

   public void printGroupsDebugInfo() {
      Iterator var1 = this.mappedGroupedRecipes.entrySet().iterator();

      while(var1.hasNext()) {
         Entry<RecipeType, List<RecipeGroup>> recipeGrouping = (Entry)var1.next();
         Debug.Send((Object)("Groups for recipes of type: " + ((RecipeType)recipeGrouping.getKey()).toString()));
         Iterator var3 = ((List)recipeGrouping.getValue()).iterator();

         while(var3.hasNext()) {
            RecipeGroup group = (RecipeGroup)var3.next();
            Debug.Send((Object)"<group>");
            Debug.Send((Object)("Enhanced recipes: " + (String)group.getEnhancedRecipes().stream().filter(Objects::nonNull).map((x) -> {
               return x.getResult().toString();
            }).collect(Collectors.joining("\nEnhanced recipes: "))));
            Debug.Send((Object)("Server recipes: " + (String)group.getServerRecipes().stream().filter(Objects::nonNull).map((x) -> {
               return x.getResult().toString();
            }).collect(Collectors.joining("\nEnhanced recipes: "))));
         }
      }

   }

   public boolean disableServerRecipe(Recipe r) {
      if (!this.serverRecipes.contains(r)) {
         return false;
      } else {
         Debug.Send((Object)("[Recipe Loader] disabling server recipe for " + r.getResult().getType().name()));
         this.serverRecipes.remove(r);
         this.disabledServerRecipes.add(r);
         this.unloadRecipe(r);
         RecipeType type = RecipeType.getType(r);
         if (type != null) {
            Iterator var3 = ((List)this.mappedGroupedRecipes.get(type)).iterator();

            while(var3.hasNext()) {
               RecipeGroup recipeGroup = (RecipeGroup)var3.next();
               if (recipeGroup.getServerRecipes().contains(r)) {
                  recipeGroup.getServerRecipes().remove(r);
                  EnhancedRecipe alwaysSimilar = (EnhancedRecipe)recipeGroup.getEnhancedRecipes().stream().filter((x) -> {
                     return x.isAlwaysSimilar(r);
                  }).findFirst().orElse(null);
                  if (alwaysSimilar != null) {
                     this.loaded.put(alwaysSimilar.getKey(), alwaysSimilar.getServerRecipe());
                     this.server.addRecipe(alwaysSimilar.getServerRecipe());
                  }
               }
            }
         }

         return true;
      }
   }

   public boolean enableServerRecipe(Recipe r) {
      if (!this.serverRecipes.contains(r)) {
         Debug.Send((Object)("[Recipe Loader] enabling server recipe for " + r.getResult().getType().name()));
         this.serverRecipes.add(r);
         this.disabledServerRecipes.remove(r);
         if (this.server.getRecipe(r.getResult().getType().getKey()) == null) {
            this.server.addRecipe(r);
         }

         RecipeType type = RecipeType.getType(r);
         if (type != null) {
            Iterator var3 = ((List)this.mappedGroupedRecipes.get(type)).iterator();

            while(var3.hasNext()) {
               RecipeGroup recipeGroup = (RecipeGroup)var3.next();
               if (recipeGroup.getEnhancedRecipes().stream().anyMatch((x) -> {
                  return x.isSimilar(r);
               })) {
                  recipeGroup.getServerRecipes().add(r);
                  EnhancedRecipe alwaysSimilar = (EnhancedRecipe)recipeGroup.getEnhancedRecipes().stream().filter((x) -> {
                     return x.isAlwaysSimilar(r);
                  }).findFirst().orElse(null);
                  if (alwaysSimilar != null) {
                     this.loaded.remove(alwaysSimilar.getKey());
                  }
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public EnhancedRecipe getLoadedRecipes(Predicate<? super EnhancedRecipe> predicate) {
      return (EnhancedRecipe)this.getLoadedRecipes().stream().filter(predicate).findFirst().orElse(null);
   }

   public void disableServerRecipes(List<Recipe> disabledServerRecipes) {
      disabledServerRecipes.forEach((x) -> {
         this.disableServerRecipe(x);
      });
   }

   public void clearCache() {
      this.serverRecipes = new ArrayList();
      this.disabledServerRecipes = new ArrayList();
      this.similarVanillaRecipe = new HashMap();
      this.loaded = new HashMap();
      this.loadedRecipes = new ArrayList();
      this.mappedGroupedRecipes = new HashMap();
   }

   public List<Recipe> getServerRecipes() {
      return this.serverRecipes;
   }

   public List<Recipe> getDisabledServerRecipes() {
      return this.disabledServerRecipes;
   }

   public List<EnhancedRecipe> getLoadedRecipes() {
      return this.loadedRecipes;
   }

   public Map<ItemStack, ItemStack> getSimilarVanillaRecipe() {
      return this.similarVanillaRecipe;
   }

   public Map<RecipeType, List<RecipeGroup>> getMappedGroupedRecipes() {
      return this.mappedGroupedRecipes;
   }
}
