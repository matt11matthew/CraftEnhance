package com.dutchjelly.craftenhance.gui.customcrafting;

import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.guis.GUIElement;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class OrderEditor extends GUIElement {
   private List<EnhancedRecipe> recipes;
   private Inventory[] inventories;
   private int currentPage;

   public OrderEditor(GuiManager manager, GuiTemplate template, GUIElement previous, Player p) {
      super(manager, template, previous, p);
      Debug.Send((Object)"An instance is being made for an order editor");
      this.recipes = new ArrayList(this.getManager().getMain().getFm().getRecipes());
      this.addBtnListener(ButtonType.NxtPage, this::handlePageChangingClicked);
      this.addBtnListener(ButtonType.PrvPage, this::handlePageChangingClicked);
      this.addBtnListener(ButtonType.SaveRecipe, this::handleSave);
      this.currentPage = 0;
      this.generateInventories();
   }

   private void handleSave(ClickType click, ItemStack itemStack, ButtonType buttonType) {
   }

   private void handlePageChangingClicked(ClickType click, ItemStack itemStack, ButtonType buttonType) {
   }

   protected void generateInventories() {
      int itemsPerPage = this.getTemplate().getFillSpace().size();
      int requiredPages = Math.max((int)Math.ceil((double)this.recipes.size() / (double)itemsPerPage), 1);
      List<ItemStack> recipeItems = (List)this.recipes.stream().map((x) -> {
         return x.getResult();
      }).collect(Collectors.toList());
      this.inventories = new Inventory[requiredPages];

      for(int i = 0; i < requiredPages; ++i) {
         this.inventories[i] = GuiUtil.FillInventory(GuiUtil.CopyInventory(this.getTemplate().getTemplate(), this.getTemplate().getInvTitle(), this), this.getTemplate().getFillSpace(), recipeItems.subList(itemsPerPage * i, Math.min(recipeItems.size(), itemsPerPage * (i + 1))));
      }

      if (this.currentPage >= this.inventories.length) {
         this.currentPage = this.inventories.length - 1;
      }

   }

   public Inventory getInventory() {
      if (this.currentPage >= this.inventories.length) {
         this.currentPage = 0;
      }

      return this.inventories[this.currentPage];
   }

   public void handleEventRest(InventoryClickEvent e) {
      Messenger.Message("The order editor is not yet implemented.", e.getWhoClicked());
   }

   public boolean isCancelResponsible() {
      return false;
   }

   private void move(EnhancedRecipe recipe, int translation) {
      int startIndex = translation > 0 ? 0 : -translation;
      int endIndex = translation > 0 ? this.recipes.size() - translation : this.recipes.size();

      for(int i = startIndex; i < endIndex; ++i) {
         if (((EnhancedRecipe)this.recipes.get(i)).equals(recipe)) {
            this.switchRecipes(i, i + translation);
            return;
         }
      }

   }

   private void switchRecipes(int a, int b) {
      EnhancedRecipe temp = (EnhancedRecipe)this.recipes.get(a);
      this.recipes.set(a, this.recipes.get(b));
      this.recipes.set(b, temp);
   }

   private EnhancedRecipe findResultingRecipe(ItemStack result, int clickPos) {
      if (clickPos > this.getInventory().getSize() - 9) {
         return null;
      } else {
         int translatedClickPos = this.currentPage * (this.getInventory().getSize() - 9) + clickPos;
         return translatedClickPos >= this.recipes.size() ? null : (EnhancedRecipe)this.recipes.get(translatedClickPos);
      }
   }

   private void scroll(int amount) {
      this.currentPage = Math.abs((this.currentPage + amount) % this.inventories.length);
   }
}
