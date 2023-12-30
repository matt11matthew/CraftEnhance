package com.dutchjelly.craftenhance.gui.guis;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.files.CategoryData;
import com.dutchjelly.craftenhance.files.MenuSettingsCache;
import com.dutchjelly.craftenhance.gui.guis.editors.RecipesViewerCategorysSettings;
import com.dutchjelly.craftenhance.gui.templates.MenuTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.util.FormatListContents;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.gui.util.InfoItemPlaceHolders;
import com.dutchjelly.craftenhance.messaging.Messenger;
import com.dutchjelly.craftenhance.prompt.HandleChatInput;
import com.dutchjelly.craftenhance.util.PermissionTypes;
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

public class RecipesViewerCategorys extends MenuHolder {
   private final MenuSettingsCache menuSettingsCache = CraftEnhance.self().getMenuSettingsCache();
   private final MenuTemplate menuTemplate;

   public RecipesViewerCategorys(String grupSeachFor) {
      super(FormatListContents.getCategorys(CraftEnhance.self().getCategoryDataCache().values(), grupSeachFor));
      this.menuTemplate = (MenuTemplate)this.menuSettingsCache.getTemplates().get("RecipesCategorys");
      this.setFillSpace(this.menuTemplate.getFillSlots());
      this.setTitle(this.menuTemplate.getMenuTitel());
      this.setMenuSize(GuiUtil.invSize("RecipesCategorys", this.menuTemplate.getAmountOfButtons()));
      this.setMenuOpenSound(this.menuTemplate.getSound());
   }

   public MenuButton getFillButtonAt(Object object) {
      return new MenuButton() {
         public void onClickInsideMenu(Player player, Inventory inventory, ClickType clickType, ItemStack itemStack, Object o) {
            if (o instanceof CategoryData) {
               if (clickType == ClickType.LEFT) {
                  (new RecipesViewer((CategoryData)o, "", player)).menuOpen(player);
               } else if (player.hasPermission(PermissionTypes.Categorys_editor.getPerm())) {
                  (new RecipesViewerCategorysSettings(((CategoryData)o).getRecipeCategory())).menuOpen(player);
               }
            }

         }

         public ItemStack getItem(Object object) {
            if (object instanceof CategoryData) {
               String displayName = " ";
               List<String> lore = new ArrayList();
               Map<String, String> placeHolders = new HashMap();
               if (RecipesViewerCategorys.this.menuTemplate != null) {
                  com.dutchjelly.craftenhance.gui.templates.MenuButton menuButton = RecipesViewerCategorys.this.menuTemplate.getMenuButton(-1);
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
            if (RecipesViewerCategorys.this.run(value, menu, player, click)) {
               RecipesViewerCategorys.this.updateButtons();
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
      } else if (value.getButtonType()==ButtonType.Close){

         player.closeInventory();
         return true;
      } else if (value.getButtonType() == ButtonType.NxtPage) {
         this.nextPage();
         return true;
      } else {
         if (value.getButtonType() == ButtonType.Search) {
            if (click == ClickType.RIGHT) {
               Messenger.Message("Search for categorys.", this.getViewer());
               (new HandleChatInput(this, (msg) -> {
                  if (GuiUtil.seachCategory(msg)) {
                     (new RecipesViewerCategorys(msg)).menuOpen(this.getViewer());
                     return false;
                  } else {
                     return true;
                  }
               })).setMessages("Search for categorys.").start(this.getViewer());
            } else {
               (new RecipesViewerCategorys("")).menuOpen(player);
            }
         }

         if (value.getButtonType() == ButtonType.NewCategory && player.hasPermission(PermissionTypes.Categorys_editor.getPerm())) {
            (new HandleChatInput(this, (msg) -> {
               if (!GuiUtil.newCategory(msg, player)) {
                  (new RecipesViewerCategorys("")).menuOpen(player);
                  return false;
               } else {
                  return true;
               }
            })).setMessages("Please input your category name and item type you want. Like this 'category' without '.Type cancel, quit, exit or q to close this without change.").start(this.getViewer());
         }

         return false;
      }
   }
}
