package com.dutchjelly.craftenhance.files;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import craftenhance.libs.menulib.utility.ServerVersion;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class FileManager {
   private final boolean useJson;
   private File dataFolder;
   private File itemsFile;
   private File recipesFile;
   private File containerOwnerFile;
   private File serverRecipeFile;
   private FileConfiguration recipesConfig;
   private FileConfiguration itemsConfig;
   private FileConfiguration serverRecipeConfig;
   private FileConfiguration containerOwnerConfig;
   private String itemsJson;
   private Logger logger;
   private Map<String, ItemStack> items;
   private List<EnhancedRecipe> recipes;

   private FileManager(boolean useJson) {
      this.useJson = useJson;
   }

   public static FileManager init(CraftEnhance main) {
      FileManager fm = new FileManager(main.getConfig().getBoolean("use-json"));
      fm.items = new HashMap();
      fm.recipes = new ArrayList();
      fm.logger = main.getLogger();
      fm.dataFolder = main.getDataFolder();
      fm.dataFolder.mkdir();
      fm.itemsFile = fm.getFile(fm.useJson ? "items.json" : "items.yml");
      fm.recipesFile = fm.getFile("recipes.yml");
      fm.serverRecipeFile = fm.getFile("server-recipes.yml");
      fm.containerOwnerFile = fm.getFile("container-owners.yml");
      return fm;
   }

   public static boolean EnsureResourceUpdate(String resourceName, File file, FileConfiguration fileConfig, JavaPlugin plugin) {
      try {
         if (!file.exists()) {
            plugin.saveResource(resourceName, false);
            return false;
         } else {
            Reader jarConfigReader = new InputStreamReader(plugin.getResource(resourceName));
            FileConfiguration jarResourceConfig = YamlConfiguration.loadConfiguration(jarConfigReader);
            jarConfigReader.close();
            boolean unsavedChanges = false;
            Iterator var7 = jarResourceConfig.getKeys(false).iterator();

            while(var7.hasNext()) {
               String key = (String)var7.next();
               if (ServerVersion.newerThan(ServerVersion.v1_8)) {
                  if (!fileConfig.contains(key, false)) {
                     fileConfig.set(key, jarResourceConfig.get(key));
                     unsavedChanges = true;
                  } else if (!fileConfig.contains(key)) {
                     fileConfig.set(key, jarResourceConfig.get(key));
                     unsavedChanges = true;
                  }
               }
            }

            if (unsavedChanges) {
               fileConfig.save(file);
            }

            return true;
         }
      } catch (Throwable var9) {
         var9.printStackTrace();
         return false;
      }
   }

   private File ensureCreated(File file) {
      if (!file.exists()) {
         this.logger.info(file.getName() + " doesn't exist... creating it.");

         try {
            file.createNewFile();
         } catch (IOException var3) {
            this.logger.warning("The file " + file.getName() + " couldn't be created!");
         }
      }

      return file;
   }

   private File getFile(String name) {
      File file = new File(this.dataFolder, name);
      this.ensureCreated(file);
      return file;
   }

   private FileConfiguration getYamlConfig(File file) {
      return YamlConfiguration.loadConfiguration(file);
   }

   public void cacheRecipes() {
      Debug.Send((Object)"The file manager is caching recipes...");
      this.recipesConfig = this.getYamlConfig(this.recipesFile);
      this.recipes.clear();
      Iterator var2 = this.recipesConfig.getKeys(false).iterator();

      while(var2.hasNext()) {
         String key = (String)var2.next();
         Debug.Send((Object)("Caching recipe with key " + key));
         EnhancedRecipe keyValue = (EnhancedRecipe)this.recipesConfig.get(key);
         String validation = keyValue.validate();
         if (validation != null) {
            Messenger.Error("Recipe with key " + key + " has issues: " + validation);
            Messenger.Error("This recipe will not be cached and loaded.");
         } else {
            keyValue.setKey(key);
            this.recipes.add(keyValue);
         }
      }

   }

   public void cacheItems() {
      try {
         if (this.useJson) {
            StringBuilder json = new StringBuilder("");
            Scanner scanner = new Scanner(this.itemsFile);

            while(scanner.hasNextLine()) {
               json.append(scanner.nextLine());
            }

            scanner.close();
            this.items.clear();
            Type typeToken = (new TypeToken<HashMap<String, Map<String, Object>>>() {
            }).getType();
            Gson gson = new Gson();
            Map<String, Map<String, Object>> serialized = (Map)gson.fromJson(json.toString(), typeToken);
            if (serialized != null) {
               serialized.keySet().forEach((x) -> {
                  ItemStack var10000 = (ItemStack)this.items.put(x, ItemStack.deserialize((Map)serialized.get(x)));
               });
            }

         } else {
            if (this.itemsConfig == null) {
               this.itemsConfig = new YamlConfiguration();
            }

            this.itemsConfig.load(this.itemsFile);
            this.items.clear();
            if (this.itemsConfig != null) {
               Iterator var1 = this.itemsConfig.getKeys(false).iterator();

               while(var1.hasNext()) {
                  String key = (String)var1.next();
                  this.items.put(key, this.itemsConfig.getItemStack(key));
               }
            }

         }
      } catch (Throwable var6) {
          var6.printStackTrace();
      }
   }

   public Map<String, ItemStack> getItems() {
      return this.items;
   }

   public ItemStack getItem(String key) {
      return (ItemStack)this.items.get(key);
   }

   public String getItemKey(ItemStack item) {
      if (item == null) {
         return null;
      } else {
         Iterator var2 = this.items.keySet().iterator();

         String key;
         do {
            if (!var2.hasNext()) {
               String uniqueKey = this.getUniqueItemKey(item);
               this.saveItem(uniqueKey, item);
               return uniqueKey;
            }

            key = (String)var2.next();
         } while(!item.equals(this.items.get(key)));

         return key;
      }
   }

   private String getUniqueItemKey(ItemStack item) {
      if (item == null) {
         return null;
      } else {
         String base = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name();
         base = base.replaceAll("\\.", "");
         String unique = base;

         for(int var4 = 1; this.items.keySet().contains(unique); unique = base + var4++) {
         }

         return unique;
      }
   }

   public List<EnhancedRecipe> getRecipes() {
      return this.recipes;
   }

   public EnhancedRecipe getRecipe(String key) {
      Iterator var2 = this.recipes.iterator();

      EnhancedRecipe recipe;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         recipe = (EnhancedRecipe)var2.next();
      } while(!recipe.getKey().equals(key));

      return recipe;
   }

   public boolean isUniqueRecipeKey(String key) {
      return this.getRecipe(key) == null;
   }

   public boolean saveItem(String key, ItemStack item) {
      try {
         if (this.useJson) {
            this.items.put(key, item);
            Gson gson = new Gson();
            Map<String, Map<String, Object>> serialized = new HashMap();
            this.items.keySet().forEach((x) -> {
               Map var10000 = (Map)serialized.put(x, ((ItemStack)this.items.get(x)).serialize());
            });
            this.itemsJson = gson.toJson(serialized, (new TypeToken<HashMap<String, Map<String, Object>>>() {
            }).getType());
            FileWriter writer = new FileWriter(this.itemsFile);
            writer.write(this.itemsJson);
            writer.close();
            return true;
         } else {
            this.itemsConfig = this.getYamlConfig(this.itemsFile);
            if (!this.itemsConfig.contains(key)) {
               this.itemsConfig.set(key, item);

               try {
                  this.itemsConfig.save(this.itemsFile);
                  this.items.put(key, item);
                  return true;
               } catch (IOException var6) {
                  this.logger.severe("Error saving an item to the items.yml file.");
               }
            }

            return false;
         }
      } catch (Throwable var7) {
         var7.printStackTrace();
         return false;
//         throw var7;
      }
   }

   public List<String> readDisabledServerRecipes() {
      if (this.serverRecipeConfig == null) {
         this.serverRecipeConfig = this.getYamlConfig(this.serverRecipeFile);
      }

      return this.serverRecipeConfig.getStringList("disabled");
   }

   public boolean saveDisabledServerRecipes(List<String> keys) {
      this.serverRecipeConfig.set("disabled", keys);

      try {
         this.serverRecipeConfig.save(this.serverRecipeFile);
         return true;
      } catch (IOException var3) {
         return false;
      }
   }

   public Map<Location, UUID> getContainerOwners() {
      this.containerOwnerConfig = this.getYamlConfig(this.containerOwnerFile);
      Map<Location, UUID> blockOwners = new HashMap();
      Iterator var2 = this.containerOwnerConfig.getKeys(false).iterator();

      while(var2.hasNext()) {
         String key = (String)var2.next();
         if (key != null) {
            String[] parsedKey = key.split(",");
            World world = Bukkit.getServer().getWorld(UUID.fromString(parsedKey[3]));
            if (world != null) {
               Location loc = new Location(world, (double)Integer.parseInt(parsedKey[0]), (double)Integer.parseInt(parsedKey[1]), (double)Integer.parseInt(parsedKey[2]));
               blockOwners.put(loc, UUID.fromString(this.containerOwnerConfig.getString(key)));
            }
         }
      }

      return blockOwners;
   }

   public boolean saveContainerOwners(Map<Location, UUID> blockOwners) {
      this.containerOwnerConfig.getKeys(false).forEach((x) -> {
         this.containerOwnerConfig.set(x, (Object)null);
      });
      Iterator var2 = blockOwners.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<Location, UUID> blockOwnerSet = (Entry)var2.next();
         Location key = (Location)blockOwnerSet.getKey();
         String keyString = key.getBlockX() + "," + key.getBlockY() + "," + key.getBlockZ() + "," + key.getWorld().getUID();
         this.containerOwnerConfig.set(keyString, ((UUID)blockOwnerSet.getValue()).toString());
      }

      try {
         this.containerOwnerConfig.save(this.containerOwnerFile);
         return true;
      } catch (IOException var6) {
         return false;
      }
   }

   public void saveRecipe(EnhancedRecipe recipe) {
      Debug.Send((Object)("Saving recipe " + recipe.toString() + " with key " + recipe.getKey()));
      String recipeKey = recipe.getKey();
      if (recipe.getKey().contains(".")) {
         recipeKey = recipeKey.replace(".", "_");
         Messenger.Message("your recipe key contains '.', it is removed now. Before " + recipe.getKey() + " after removed " + recipeKey);
         recipe.setKey(recipeKey);
      }

      this.recipesConfig = this.getYamlConfig(this.recipesFile);
      this.recipesConfig.set(recipeKey, recipe);

      try {
         this.recipesConfig.save(this.recipesFile);
         if (this.getRecipe(recipe.getKey()) == null) {
            this.recipes.add(recipe);
         }

         Debug.Send((Object)("Succesfully saved the recipe, there are now " + this.recipes.size() + " recipes cached."));
      } catch (IOException var4) {
         this.logger.severe("Error saving a recipe to the recipes.yml file.");
      }

   }

   public void removeRecipe(EnhancedRecipe recipe) {
      Debug.Send((Object)("Removing recipe " + recipe.toString() + " with key " + recipe.getKey()));
      String recipeKey = recipe.getKey();
      if (recipe.getKey().contains(".")) {
         recipeKey = recipeKey.replace(".", "_");
         Messenger.Message("your recipe key contains '.', it is removed now. Before " + recipe.getKey() + " after removed " + recipeKey);
         recipe.setKey(recipeKey);
      }

      this.recipesConfig = this.getYamlConfig(this.recipesFile);
      this.recipesConfig.set(recipeKey, (Object)null);
      this.recipes.remove(recipe);

      try {
         this.recipesConfig.save(this.recipesFile);
      } catch (IOException var4) {
         this.logger.severe("Error removing a recipe.");
      }

   }

   public void overrideSave() {
      Debug.Send((Object)"Overriding saved recipes with new list..");
      List<EnhancedRecipe> cloned = new ArrayList();
      this.recipes.forEach((x) -> {
         cloned.add(x);
      });
      this.removeAllRecipes();
      cloned.forEach((x) -> {
         this.saveRecipe(x);
      });
      this.recipes = cloned;
      this.recipesConfig = this.getYamlConfig(this.recipesFile);
   }

   private void removeAllRecipes() {
      if (!this.recipes.isEmpty()) {
         this.removeRecipe((EnhancedRecipe)this.recipes.get(0));
         this.removeAllRecipes();
      }
   }

   public void cleanItemFile() {
      Debug.Send((Object)"Cleaning up unused items.");
      Iterator var1 = this.items.keySet().iterator();

      while(var1.hasNext()) {
         String itemKey = (String)var1.next();
         if (!this.isItemInUse((ItemStack)this.items.get(itemKey))) {
            Debug.Send((Object)("Item with key " + itemKey + " is not used and will be removed."));
            this.itemsConfig.set(itemKey, (Object)null);

            try {
               this.itemsConfig.save(this.itemsFile);
            } catch (IOException var4) {
               Debug.Send((Object)"Failed saving itemsConfig");
            }
         }
      }

   }

   private boolean isItemInUse(ItemStack item) {
      Iterator var2 = this.recipes.iterator();

      while(var2.hasNext()) {
         EnhancedRecipe r = (EnhancedRecipe)var2.next();
         if (r.getResult().equals(item)) {
            return true;
         }

         ItemStack[] var4 = r.getContent();
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            ItemStack inRecipe = var4[var6];
            if (inRecipe != null && inRecipe.equals(item)) {
               return true;
            }
         }
      }

      return false;
   }
}
