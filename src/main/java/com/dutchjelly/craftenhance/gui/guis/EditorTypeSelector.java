package com.dutchjelly.craftenhance.gui.guis;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.FurnaceRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.files.CategoryData;
import com.dutchjelly.craftenhance.files.MenuSettingsCache;
import com.dutchjelly.craftenhance.gui.guis.editors.RecipeEditor;
import com.dutchjelly.craftenhance.gui.templates.MenuTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import craftenhance.libs.menulib.MenuButton;
import craftenhance.libs.menulib.MenuHolder;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EditorTypeSelector extends MenuHolder {
   private final MenuSettingsCache menuSettingsCache = CraftEnhance.self().getMenuSettingsCache();
   private final MenuTemplate menuTemplate;
   private final String permission;
   private final String recipeKey;
   private int slots;

   public EditorTypeSelector(String recipeKey, String permission) {
      this.permission = permission;
      this.recipeKey = recipeKey;
      this.menuTemplate = (MenuTemplate)this.menuSettingsCache.getTemplates().get("EditorTypeSelector");
      this.setMenuSize(GuiUtil.invSize("EditorTypeSelector", this.menuTemplate.getAmountOfButtons()));
      this.setTitle(this.menuTemplate.getMenuTitel());
      this.setMenuOpenSound(this.menuTemplate.getSound());
   }

   private String getFreshKey(String keySeed) {
      if (keySeed == null || !CraftEnhance.self().getFm().isUniqueRecipeKey(keySeed)) {
         int uniqueKeyIndex = 1;

         for(keySeed = "recipe"; !CraftEnhance.self().getFm().isUniqueRecipeKey(keySeed + uniqueKeyIndex); ++uniqueKeyIndex) {
         }

         keySeed = keySeed + uniqueKeyIndex;
      }

      return keySeed;
   }

   public MenuButton getButtonAt(int slot) {
      if (this.menuTemplate == null) {
         return null;
      } else {
         Iterator var2 = this.menuTemplate.getMenuButtons().entrySet().iterator();

         Entry menuTemplate;
         do {
            if (!var2.hasNext()) {
               return null;
            }

            menuTemplate = (Entry)var2.next();
         } while(!((List)menuTemplate.getKey()).contains(slot));

         return this.registerButtons((com.dutchjelly.craftenhance.gui.templates.MenuButton)menuTemplate.getValue());
      }
   }

   private MenuButton registerButtons(final com.dutchjelly.craftenhance.gui.templates.MenuButton value) {
      return new MenuButton() {
         public void onClickInsideMenu(Player player, Inventory menu, ClickType click, ItemStack clickedItem, Object object) {
            EditorTypeSelector.this.run(value, player);
         }

         public ItemStack getItem() {
            return value.getItemStack();
         }
      };
   }

   public void run(com.dutchjelly.craftenhance.gui.templates.MenuButton value, Player player) {
      EnhancedRecipe newRecipe = null;
      if (value.getButtonType() == ButtonType.ChooseWorkbenchType) {
         newRecipe = new WBRecipe(this.permission, (ItemStack)null, new ItemStack[9]);
      }

      if (value.getButtonType() == ButtonType.ChooseFurnaceType) {
         newRecipe = new FurnaceRecipe(this.permission, (ItemStack)null, new ItemStack[1]);
      }

      if (newRecipe != null) {
         ((EnhancedRecipe)newRecipe).setKey(this.getFreshKey(this.recipeKey));
         RecipeEditor<EnhancedRecipe> recipeEditor = new RecipeEditor((EnhancedRecipe)newRecipe, (CategoryData)null, this.permission, value.getButtonType());
         recipeEditor.menuOpen(player);
      }

   }
}
