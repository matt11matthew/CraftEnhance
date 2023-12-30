package com.dutchjelly.craftenhance.gui.guis.editors;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.files.CategoryData;
import com.dutchjelly.craftenhance.files.MenuSettingsCache;
import com.dutchjelly.craftenhance.gui.guis.RecipesViewerCategorys;
import com.dutchjelly.craftenhance.gui.templates.MenuTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.prompt.HandleChatInput;
import craftenhance.libs.menulib.MenuButton;
import craftenhance.libs.menulib.MenuHolder;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RecipesViewerCategorysSettings extends MenuHolder {
   private final MenuSettingsCache menuSettingsCache = CraftEnhance.self().getMenuSettingsCache();
   private final MenuTemplate menuTemplate;
   private final String category;

   public RecipesViewerCategorysSettings(String category) {
      this.menuTemplate = (MenuTemplate)this.menuSettingsCache.getTemplates().get("CategorysSettings");
      if (this.menuTemplate != null) {
         this.setFillSpace(this.menuTemplate.getFillSlots());
         this.setTitle(this.menuTemplate.getMenuTitel());
         this.setMenuSize(GuiUtil.invSize("CategorysSettings", this.menuTemplate.getAmountOfButtons()));
         this.setMenuOpenSound(this.menuTemplate.getSound());
      }

      this.category = category;
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
            if (RecipesViewerCategorysSettings.this.run(value, menu, player, click)) {
               RecipesViewerCategorysSettings.this.updateButtons();
            }

         }

         public ItemStack getItem() {
            return value.getItemStack();
         }
      };
   }

   public boolean run(com.dutchjelly.craftenhance.gui.templates.MenuButton value, Inventory menu, Player player, ClickType click) {
      if (value.getButtonType() == ButtonType.RemoveCategory) {
         CategoryData categoryData = CraftEnhance.self().getCategoryDataCache().get(this.category);
         if (categoryData != null) {
            List<EnhancedRecipe> enhancedRecipes = categoryData.getEnhancedRecipes();
            String defaultCategory = "default";
            if (enhancedRecipes != null && !enhancedRecipes.isEmpty()) {
               CategoryData oldCategory = CraftEnhance.self().getCategoryDataCache().get("default");
               if (oldCategory == null) {
                  oldCategory = CraftEnhance.self().getCategoryDataCache().of("default", new ItemStack(Adapter.getMaterial("CRAFTING_TABLE")), (String)null);
               }

               Iterator var9 = enhancedRecipes.iterator();

               while(var9.hasNext()) {
                  EnhancedRecipe recipe = (EnhancedRecipe)var9.next();
                  recipe.setRecipeCategory("default");
                  oldCategory.addEnhancedRecipes(recipe);
               }

               CraftEnhance.self().getCategoryDataCache().put("default", oldCategory);
            }

            CraftEnhance.self().getCategoryDataCache().remove(this.category);
            Bukkit.getScheduler().runTaskLaterAsynchronously(CraftEnhance.self(), () -> {
               CraftEnhance.self().getCategoryDataCache().save();
            }, 1L);
            (new RecipesViewerCategorys("")).menuOpen(player);
         }
      }

      if (value.getButtonType() == ButtonType.ChangeCategoryName) {
         (new HandleChatInput(this, (msg) -> {
            if (!GuiUtil.changeCategoryName(this.category, msg, player)) {
               (new RecipesViewerCategorysSettings(this.category)).menuOpen(player);
               return false;
            } else {
               return true;
            }
         })).setMessages("Please input new display name. Like this 'name' without '.Type cancel, quit, exit to close this without change.").start(player);
      }

      if (value.getButtonType() == ButtonType.ChangeCategoryItem) {
         (new HandleChatInput(this, (msg) -> {
            System.out.println("msg " + msg);
            if (!GuiUtil.changeCategoryItem(this.category, msg, player)) {
               (new RecipesViewerCategorysSettings(this.category)).menuOpen(player);
               return false;
            } else {
               return true;
            }
         })).setMessages("Change category item. Like this 'stone' without '.Type cancel, quit, exit or q to close this without change.").start(player);
      }

      if (value.getButtonType() == ButtonType.ChangeCategory) {
         (new HandleChatInput(this, (msg) -> {
            if (!GuiUtil.changeCategory(this.category, msg, player)) {
               (new RecipesViewerCategorysSettings(this.category)).menuOpen(player);
               return false;
            } else {
               return true;
            }
         })).setMessages("Change category name. Like this 'new_category_name' without '.Type cancel, quit, exit or q to close this without change.").start(player);
      }

      if (value.getButtonType() == ButtonType.Back) {
         (new RecipesViewerCategorys("")).menuOpen(player);
      }
      if (value.getButtonType()==ButtonType.Close){
         player.closeInventory();
      }

      return false;
   }
}
