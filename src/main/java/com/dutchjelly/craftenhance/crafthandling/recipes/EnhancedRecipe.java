package com.dutchjelly.craftenhance.crafthandling.recipes;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.RecipeLoader;
import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.files.FileManager;
import com.dutchjelly.craftenhance.gui.interfaces.GuiPlacable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public abstract class EnhancedRecipe extends GuiPlacable implements ConfigurationSerializable, ServerLoadable {
   private int id;
   private String key;
   private ItemStack result;
   private ItemStack[] content;
   private ItemMatchers.MatchType matchType;
   private String permissions;
   private boolean hidden;
   private String onCraftCommand;
   private RecipeType type;
   private Map<String, Object> deserialize;
   private Map<String, Object> serialize;

   public EnhancedRecipe() {
      this.matchType = ItemMatchers.MatchType.MATCH_META;
   }

   public EnhancedRecipe(String perm, ItemStack result, ItemStack[] content) {
      this.matchType = ItemMatchers.MatchType.MATCH_META;
      this.permissions = perm;
      this.result = result;
      this.content = content;
   }

   protected EnhancedRecipe(Map<String, Object> args) {
      super(args);
      this.matchType = ItemMatchers.MatchType.MATCH_META;
      FileManager fm = CraftEnhance.self().getFm();
      this.result = fm.getItem((String)args.get("result"));
      this.permissions = (String)args.get("permission");
      if (args.containsKey("matchtype")) {
         this.matchType = ItemMatchers.MatchType.valueOf((String)args.get("matchtype"));
      } else if (args.containsKey("matchmeta")) {
         this.matchType = (Boolean)args.get("matchmeta") ? ItemMatchers.MatchType.MATCH_META : ItemMatchers.MatchType.MATCH_TYPE;
      }

      if (args.containsKey("oncraftcommand")) {
         this.onCraftCommand = (String)args.get("oncraftcommand");
      }

      if (args.containsKey("hidden")) {
         this.hidden = (Boolean)args.get("hidden");
      }

      List<String> recipeKeys = (List)args.get("recipe");
      this.setContent(new ItemStack[recipeKeys.size()]);

      for(int i = 0; i < this.content.length; ++i) {
         this.content[i] = fm.getItem((String)recipeKeys.get(i));
      }

      this.deserialize = args;
   }

   public Map<String, Object> serialize() {
      final FileManager fm = ((CraftEnhance)CraftEnhance.getPlugin(CraftEnhance.class)).getFm();
      return new HashMap<String, Object>() {
         {
            this.putAll(EnhancedRecipe.super.serialize());
            this.put("permission", EnhancedRecipe.this.permissions);
            this.put("matchtype", EnhancedRecipe.this.matchType.name());
            this.put("hidden", EnhancedRecipe.this.hidden);
            this.put("oncraftcommand", EnhancedRecipe.this.onCraftCommand);
            this.put("result", fm.getItemKey(EnhancedRecipe.this.result));
            this.put("recipe", Arrays.stream(EnhancedRecipe.this.content).map((x) -> {
               return fm.getItemKey(x);
            }).toArray((x$0) -> {
               return new String[x$0];
            }));
            if (EnhancedRecipe.this.serialize != null && !EnhancedRecipe.this.serialize.isEmpty()) {
               this.putAll(EnhancedRecipe.this.serialize);
            }

         }
      };
   }

   public String validate() {
      if (this.result == null) {
         return "recipe cannot have null result";
      } else if (!Adapter.canUseModeldata() && this.matchType == ItemMatchers.MatchType.MATCH_MODELDATA_AND_TYPE) {
         return "recipe is using modeldata match while the server doesn't support it";
      } else {
         return this.content.length != 0 && Arrays.stream(this.content).anyMatch((x) -> {
            return x != null;
         }) ? null : "recipe content cannot be empty";
      }
   }

   public String toString() {
      return "EnhancedRecipe{key='" + this.key + '\'' + ", result=" + (this.result == null ? "null" : this.result) + '}';
   }

   public ItemStack getDisplayItem() {
      return this.getResult();
   }

   public void save() {
      if (this.validate() == null) {
         CraftEnhance.self().getFm().saveRecipe(this);
      }

   }

   public void load() {
      RecipeLoader.getInstance().loadRecipe(this);
   }

   public abstract boolean matches(ItemStack[] var1);

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getKey() {
      return this.key;
   }

   public void setKey(String key) {
      this.key = key;
   }

   public ItemStack getResult() {
      return this.result;
   }

   public void setResult(ItemStack result) {
      this.result = result;
   }

   public ItemStack[] getContent() {
      return this.content;
   }

   public void setContent(ItemStack[] content) {
      this.content = content;
   }

   public ItemMatchers.MatchType getMatchType() {
      return this.matchType;
   }

   public void setMatchType(ItemMatchers.MatchType matchType) {
      this.matchType = matchType;
   }

   public String getPermissions() {
      return this.permissions;
   }

   public void setPermissions(String permissions) {
      this.permissions = permissions;
   }

   public boolean isHidden() {
      return this.hidden;
   }

   public void setHidden(boolean hidden) {
      this.hidden = hidden;
   }

   public String getOnCraftCommand() {
      return this.onCraftCommand;
   }

   public void setOnCraftCommand(String onCraftCommand) {
      this.onCraftCommand = onCraftCommand;
   }

   public RecipeType getType() {
      return this.type;
   }

   public Map<String, Object> getDeserialize() {
      return this.deserialize;
   }

   public Map<String, Object> getSerialize() {
      return this.serialize;
   }
}
