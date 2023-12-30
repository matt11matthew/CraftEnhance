package com.dutchjelly.craftenhance.gui.guis;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.RecipeLoader;
import com.dutchjelly.craftenhance.files.MenuSettingsCache;
import com.dutchjelly.craftenhance.gui.templates.MenuTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.util.FormatListContents;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.gui.util.InfoItemPlaceHolders;
import com.dutchjelly.craftenhance.prompt.HandleChatInput;
import craftenhance.libs.menulib.MenuButton;
import craftenhance.libs.menulib.MenuHolder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;

public class RecipeDisabler extends MenuHolder {
   private final MenuSettingsCache menuSettingsCache = CraftEnhance.self().getMenuSettingsCache();
   private final MenuTemplate menuTemplate;
   private final MenuButton fillSlots;
   boolean enableMode;

   public RecipeDisabler(List<Recipe> enabledRecipes, List<Recipe> disabledRecipes, final boolean enableMode, String recipesSeachFor) {
      super(FormatListContents.getRecipes(enabledRecipes, disabledRecipes, enableMode, recipesSeachFor));
      this.menuTemplate = (MenuTemplate)this.menuSettingsCache.getTemplates().get("RecipeDisabler");
      this.enableMode = enableMode;
      this.setFillSpace(this.menuTemplate.getFillSlots());
      this.setTitle(this.menuTemplate.getMenuTitel());
      this.setMenuSize(GuiUtil.invSize("RecipeDisabler", this.menuTemplate.getAmountOfButtons()));
      this.setMenuOpenSound(this.menuTemplate.getSound());
      this.fillSlots = new MenuButton() {
         public void onClickInsideMenu(Player player, Inventory inventory, ClickType clickType, ItemStack itemStack, Object o) {
            if (o instanceof Recipe) {
               if (enableMode) {
                  if (RecipeLoader.getInstance().enableServerRecipe((Recipe)o)) {
                     RecipeDisabler.this.updateButtons();
                  }
               } else if (RecipeLoader.getInstance().disableServerRecipe((Recipe)o)) {
                  RecipeDisabler.this.updateButtons();
               }
            }

         }

         public ItemStack getItem(Object object) {
            if (object instanceof Recipe) {
               ItemStack result = ((Recipe)object).getResult();
               ItemMeta meta;
               if (GuiUtil.isNull(result)) {
                  result = new ItemStack(Material.BARRIER);
                  meta = result.getItemMeta();
                  meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&4Complex Recipe: " + Adapter.GetRecipeIdentifier((Recipe)object)));
                  meta.setLore(Arrays.asList("&eWARN: &fThis recipe is complex, which", "&f means that the result is only known", " &f&oafter&r&f the content of the crafting table is sent", " &fto the server. Think of repairing or coloring recipes.", " &f&nSo disabling is not recommended!"));
                  meta.setLore((List)meta.getLore().stream().map((x) -> {
                     return ChatColor.translateAlternateColorCodes('&', x);
                  }).collect(Collectors.toList()));
                  result.setItemMeta(meta);
               } else {
                  meta = result.getItemMeta();
                  meta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&3key: &f" + Adapter.GetRecipeIdentifier((Recipe)object))));
                  result.setItemMeta(meta);
               }

               return result;
            } else {
               return null;
            }
         }

         public ItemStack getItem() {
            return null;
         }
      };
   }

   public MenuButton getFillButtonAt(Object object) {
      return this.fillSlots;
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
            if (RecipeDisabler.this.run(value, menu, player, click)) {
               RecipeDisabler.this.updateButtons();
            }

         }

         public ItemStack getItem() {
            Map<String, String> placeHolders = new HashMap<String, String>() {
               {
                  this.put(InfoItemPlaceHolders.DisableMode.getPlaceHolder(), RecipeDisabler.this.enableMode ? "enable recipes by clicking them" : "disable recipes by clicking them");
               }
            };
            return value.getItemStack() == null ? null : GuiUtil.ReplaceAllPlaceHolders(value.getItemStack().clone(), placeHolders);
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
      } else if (value.getButtonType() == ButtonType.SwitchDisablerMode) {
         this.enableMode = !this.enableMode;
         (new RecipeDisabler(RecipeLoader.getInstance().getServerRecipes(), RecipeLoader.getInstance().getDisabledServerRecipes(), this.enableMode, "")).menuOpen(player);
         return true;
      } else {
         if (value.getButtonType() == ButtonType.Search) {
            if (click == ClickType.RIGHT) {
               (new HandleChatInput(this, (msg) -> {
                  if (GuiUtil.seachCategory(msg)) {
                     (new RecipeDisabler(RecipeLoader.getInstance().getServerRecipes(), RecipeLoader.getInstance().getDisabledServerRecipes(), this.enableMode, msg)).menuOpen(this.getViewer());
                     return false;
                  } else {
                     return true;
                  }
               })).setMessages("Search for recipe items").start(this.getViewer());
            } else {
               (new RecipeDisabler(RecipeLoader.getInstance().getServerRecipes(), RecipeLoader.getInstance().getDisabledServerRecipes(), this.enableMode, "")).menuOpen(player);
            }
         }

         return false;
      }
   }
}
