package com.dutchjelly.craftenhance.gui.guis;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.FurnaceRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.files.CategoryData;
import com.dutchjelly.craftenhance.files.MenuSettingsCache;
import com.dutchjelly.craftenhance.gui.guis.editors.RecipeEditor;
import com.dutchjelly.craftenhance.gui.guis.viewers.RecipeViewRecipe;
import com.dutchjelly.craftenhance.gui.templates.MenuTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.util.FormatListContents;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.prompt.HandleChatInput;
import com.dutchjelly.craftenhance.util.PermissionTypes;
import craftenhance.libs.menulib.MenuButton;
import craftenhance.libs.menulib.MenuHolder;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RecipesViewer extends MenuHolder {
   private final MenuSettingsCache menuSettingsCache = CraftEnhance.self().getMenuSettingsCache();
   private final MenuTemplate menuTemplate;
   private final CategoryData categoryData;

   public RecipesViewer(CategoryData categoryData, String recipeSeachFor, Player player) {
      super(FormatListContents.canSeeRecipes(categoryData.getEnhancedRecipes(recipeSeachFor), player));
      this.menuTemplate = (MenuTemplate)this.menuSettingsCache.getTemplates().get("RecipesViewer");
      this.categoryData = categoryData;
      this.setFillSpace(this.menuTemplate.getFillSlots());
      this.setTitle(() -> {
         return this.menuTemplate.getMenuTitel() + (categoryData.getDisplayName() != null && !categoryData.getDisplayName().isEmpty() ? categoryData.getDisplayName() : categoryData.getRecipeCategory());
      });
      this.setMenuSize(GuiUtil.invSize("RecipesViewer", this.menuTemplate.getAmountOfButtons()));
      this.setMenuOpenSound(this.menuTemplate.getSound());
   }

   public MenuButton getFillButtonAt(Object object) {
      return new MenuButton() {
         public void onClickInsideMenu(Player player, Inventory inventory, ClickType clickType, ItemStack itemStack, Object o) {
            if (o instanceof WBRecipe) {
               if ((clickType == ClickType.MIDDLE || clickType == ClickType.RIGHT) && RecipesViewer.this.getViewer().hasPermission(PermissionTypes.Edit.getPerm())) {
                  (new RecipeEditor((WBRecipe)o, RecipesViewer.this.categoryData, (String)null, ButtonType.ChooseWorkbenchType)).menuOpen(player);
               } else {
                  (new RecipeViewRecipe(RecipesViewer.this.categoryData, (WBRecipe)o, "WBRecipeViewer")).menuOpen(player);
               }
            }

            if (o instanceof FurnaceRecipe) {
               if ((clickType == ClickType.MIDDLE || clickType == ClickType.RIGHT) && RecipesViewer.this.getViewer().hasPermission(PermissionTypes.Edit.getPerm())) {
                  (new RecipeEditor((FurnaceRecipe)o, RecipesViewer.this.categoryData, (String)null, ButtonType.ChooseFurnaceType)).menuOpen(player);
               } else {
                  (new RecipeViewRecipe(RecipesViewer.this.categoryData, (FurnaceRecipe)o, "FurnaceRecipeViewer")).menuOpen(player);
               }
            }

         }

         public ItemStack getItem(Object object) {
            return object instanceof EnhancedRecipe ? ((EnhancedRecipe)object).getDisplayItem() : null;
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
            if (RecipesViewer.this.run(value, menu, player, click)) {
               RecipesViewer.this.updateButtons();
            }

         }

         public ItemStack getItem() {
            return value.getItemStack();
         }
      };
   }

   public boolean run(com.dutchjelly.craftenhance.gui.templates.MenuButton value, Inventory menu, Player player, ClickType click) {
      if (value.getButtonType()==ButtonType.Close){
         player.closeInventory();
         return true;
      }
      if (value.getButtonType() == ButtonType.PrvPage) {
         this.previousPage();
         return true;
      } else if (value.getButtonType() == ButtonType.NxtPage) {
         this.nextPage();
         return true;
      } else {
         if (value.getButtonType() == ButtonType.Search) {
            if (click == ClickType.RIGHT) {
               new HandleChatInput(this, (msg) -> {
                  if (GuiUtil.seachCategory(msg)) {
                     (new RecipesViewer(this.categoryData, msg, player)).menuOpen(this.getViewer());
                     return false;
                  } else {
                     return true;
                  }
               });
            } else {
               (new RecipesViewer(this.categoryData, "", player)).menuOpen(player);
            }
         }

         if (value.getButtonType() == ButtonType.Back) {
            (new RecipesViewerCategorys("")).menuOpen(player);
         } else   if (value.getButtonType() == ButtonType.Close) {
            player.closeInventory();
         }

         return false;
      }
   }
}
