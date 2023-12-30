package com.dutchjelly.craftenhance.gui.guis;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.files.CategoryData;
import com.dutchjelly.craftenhance.files.MenuSettingsCache;
import com.dutchjelly.craftenhance.gui.guis.editors.RecipeEditor;
import com.dutchjelly.craftenhance.gui.templates.MenuTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.util.FormatListContents;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.gui.util.InfoItemPlaceHolders;
import com.dutchjelly.craftenhance.messaging.Messenger;
import com.dutchjelly.craftenhance.prompt.HandleChatInput;
import craftenhance.libs.menulib.MenuButton;
import craftenhance.libs.menulib.MenuHolder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CategoryList<RecipeT extends EnhancedRecipe> extends MenuHolder {
   private final MenuSettingsCache menuSettingsCache = CraftEnhance.self().getMenuSettingsCache();
   private final MenuTemplate menuTemplate;
   private final RecipeT recipe;
   private final CategoryData categoryData;
   private final String permission;
   private final ButtonType editorType;

   public CategoryList(RecipeT recipe, CategoryData categoryData, String permission, ButtonType editorType, String grupSeachFor) {
      super(FormatListContents.getCategorys(CraftEnhance.self().getCategoryDataCache().values(), grupSeachFor));
      this.menuTemplate = (MenuTemplate)this.menuSettingsCache.getTemplates().get("CategoryList");
      this.recipe = recipe;
      this.categoryData = categoryData;
      this.permission = permission;
      this.editorType = editorType;
      if (this.menuTemplate != null) {
         this.setFillSpace(this.menuTemplate.getFillSlots());
         this.setTitle(this.menuTemplate.getMenuTitel());
         this.setMenuSize(GuiUtil.invSize("CategoryList", this.menuTemplate.getAmountOfButtons()));
         this.setMenuOpenSound(this.menuTemplate.getSound());
      }

   }

   public MenuButton getFillButtonAt(Object object) {
      return new MenuButton() {
         public void onClickInsideMenu(Player player, Inventory inventory, ClickType clickType, ItemStack itemStack, Object o) {
            if (o instanceof CategoryData) {
               String category = ((CategoryData)o).getRecipeCategory();
               CategoryData changedCategory = (CategoryData)o;
               CategoryList.this.recipe.setRecipeCategory(category);
               CategoryList.this.recipe.save();
               CategoryData moveCategoryData = null;
               if (CategoryList.this.categoryData != null) {
                  moveCategoryData = CraftEnhance.self().getCategoryDataCache().move(CategoryList.this.categoryData.getRecipeCategory(), category, CategoryList.this.recipe);
                  if (moveCategoryData == null) {
                     Messenger.Message("Could not add recipe to this " + o + " category.");
                     return;
                  }
               }

               (new RecipeEditor(CategoryList.this.recipe, moveCategoryData, (String)null, CategoryList.this.editorType)).menuOpen(player);
            }

         }

         public ItemStack getItem(Object object) {
            if (object instanceof CategoryData) {
               String displayName = " ";
               List<String> lore = new ArrayList();
               Map<String, String> placeHolders = new HashMap();
               if (CategoryList.this.menuTemplate != null) {
                  com.dutchjelly.craftenhance.gui.templates.MenuButton menuButton = CategoryList.this.menuTemplate.getMenuButton(-1);
                  if (menuButton != null) {
                     displayName = menuButton.getDisplayName();
                     lore = menuButton.getLore();
                  }
               }

               ItemStack itemStack = ((CategoryData)object).getRecipeCategoryItem();
               GuiUtil.setTextItem(itemStack, displayName, (List)lore);
               String categoryName = ((CategoryData)object).getDisplayName();
               if (categoryName == null || categoryName.equals("")) {
                  categoryName = ((CategoryData)object).getRecipeCategory();
               }

               placeHolders.put(InfoItemPlaceHolders.DisplayName.getPlaceHolder(), categoryName);
               return GuiUtil.ReplaceAllPlaceHolders(itemStack.clone(), placeHolders);
            } else {
               return null;
            }
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
            if (CategoryList.this.run(value, menu, player, click)) {
               CategoryList.this.updateButtons();
            }

         }

         public ItemStack getItem() {
            return value.getItemStack();
         }
      };
   }

   public boolean run(com.dutchjelly.craftenhance.gui.templates.MenuButton value, Inventory menu, Player player, ClickType click) {
      if (value.getButtonType() == ButtonType.PrvPage) {
         this.previousPage();
         return true;
      } else if (value.getButtonType() == ButtonType.NxtPage) {
         this.nextPage();
         return true;
      } else {
         if (value.getButtonType() == ButtonType.Back) {
            (new RecipeEditor(this.recipe, this.categoryData, (String)null, this.editorType)).menuOpen(player);
         }

         if (value.getButtonType() == ButtonType.Search) {
            if (click == ClickType.RIGHT) {
               (new HandleChatInput(this, (msg) -> {
                  if (GuiUtil.seachCategory(msg)) {
                     (new CategoryList(this.recipe, this.categoryData, this.permission, this.editorType, msg)).menuOpen(this.getViewer());
                     return false;
                  } else {
                     return true;
                  }
               })).setMessages("Search for categorys.").start(this.getViewer());
            } else {
               (new CategoryList(this.recipe, this.categoryData, this.permission, this.editorType, "")).menuOpen(player);
            }
         }

         return false;
      }
   }
}
