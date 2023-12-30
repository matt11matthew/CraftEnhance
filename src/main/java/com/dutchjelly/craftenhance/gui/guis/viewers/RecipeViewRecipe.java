package com.dutchjelly.craftenhance.gui.guis.viewers;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.FurnaceRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.files.CategoryData;
import com.dutchjelly.craftenhance.files.MenuSettingsCache;
import com.dutchjelly.craftenhance.gui.guis.RecipesViewer;
import com.dutchjelly.craftenhance.gui.templates.MenuTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.util.FormatListContents;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.gui.util.InfoItemPlaceHolders;
import craftenhance.libs.menulib.MenuButton;
import craftenhance.libs.menulib.MenuHolder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RecipeViewRecipe<RecipeT extends EnhancedRecipe> extends MenuHolder {
   private final MenuSettingsCache menuSettingsCache = CraftEnhance.self().getMenuSettingsCache();
   private final MenuTemplate menuTemplate;
   private final CategoryData categoryData;
   private final RecipeT recipe;

   public RecipeViewRecipe(CategoryData categoryData, RecipeT recipe, String menuType) {
      super(FormatListContents.formatRecipes(recipe));
      this.recipe = recipe;
      this.categoryData = categoryData;
      this.menuTemplate = (MenuTemplate)this.menuSettingsCache.getTemplates().get(menuType);
      this.setFillSpace(this.menuTemplate.getFillSlots());
      this.setTitle(this.menuTemplate.getMenuTitel());
      this.setMenuSize(27);
   }

   public MenuButton getFillButtonAt(Object object) {
      return new MenuButton() {
         public void onClickInsideMenu(Player player, Inventory inventory, ClickType clickType, ItemStack itemStack, Object o) {
         }

         public ItemStack getItem(Object object) {
            return object instanceof ItemStack ? (ItemStack)object : null;
         }

         public ItemStack getItem() {
            return null;
         }
      };
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
            if (RecipeViewRecipe.this.run(value, menu, player, click)) {
               RecipeViewRecipe.this.updateButtons();
            }

         }

         public ItemStack getItem() {
            Map<String, String> placeHolders = new HashMap<String, String>() {
               {
                  if (RecipeViewRecipe.this.recipe instanceof WBRecipe) {
                     this.put(InfoItemPlaceHolders.Shaped.getPlaceHolder(), ((WBRecipe)RecipeViewRecipe.this.recipe).isShapeless() ? "shapeless" : "shaped");
                  }

                  if (RecipeViewRecipe.this.recipe instanceof FurnaceRecipe) {
                     this.put(InfoItemPlaceHolders.Exp.getPlaceHolder(), String.valueOf(((FurnaceRecipe)RecipeViewRecipe.this.recipe).getExp()));
                     this.put(InfoItemPlaceHolders.Duration.getPlaceHolder(), String.valueOf(((FurnaceRecipe)RecipeViewRecipe.this.recipe).getDuration()));
                  }

                  this.put(InfoItemPlaceHolders.Key.getPlaceHolder(), RecipeViewRecipe.this.recipe.getKey() == null ? "null" : RecipeViewRecipe.this.recipe.getKey());
                  this.put(InfoItemPlaceHolders.MatchMeta.getPlaceHolder(), RecipeViewRecipe.this.recipe.getMatchType().getDescription());
                  this.put(InfoItemPlaceHolders.MatchType.getPlaceHolder(), RecipeViewRecipe.this.recipe.getMatchType().getDescription());
                  this.put(InfoItemPlaceHolders.Permission.getPlaceHolder(), RecipeViewRecipe.this.recipe.getPermissions() == null ? "null" : RecipeViewRecipe.this.recipe.getPermissions());
               }
            };
            return GuiUtil.ReplaceAllPlaceHolders(value.getItemStack().clone(), placeHolders);
         }
      };
   }

   public boolean run(com.dutchjelly.craftenhance.gui.templates.MenuButton value, Inventory menu, Player player, ClickType click) {
      if (value.getButtonType() == ButtonType.Back) {
         (new RecipesViewer(this.categoryData, "", player)).menuOpen(player);
      }

      if (value.getButtonType()==ButtonType.Close){
         player.closeInventory();
      }
      return false;
   }
}
