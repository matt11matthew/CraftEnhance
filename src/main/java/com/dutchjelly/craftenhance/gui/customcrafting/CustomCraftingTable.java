package com.dutchjelly.craftenhance.gui.customcrafting;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.util.CustomPrepareCraftEvent;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.guis.GUIElement;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.messaging.Debug;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CustomCraftingTable extends GUIElement {
   private Inventory inventory = GuiUtil.CopyInventory(this.getTemplate().getTemplate(), this.getTemplate().getInvTitle(), this);
   private ItemStack[] recipeContent;
   private ItemStack result;

   public CustomCraftingTable(GuiManager manager, GuiTemplate template, GUIElement previous, Player p) {
      super(manager, template, previous, p);
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public void handleEventRest(InventoryClickEvent e) {
      if (!this.getTemplate().getFillSpace().contains(e.getSlot())) {
         Debug.Send((Object)"player clicked outside recipe");
         e.setCancelled(true);
      } else if (!((Integer)this.getTemplate().getFillSpace().get(9)).equals(e.getSlot())) {
         if (e.getClick().equals(ClickType.DOUBLE_CLICK)) {
            ItemStack collector = e.getView().getCursor();
            if (!GuiUtil.isNull(e.getView().getCursor()) && collector.isSimilar(this.result)) {
               e.setCancelled(true);
               return;
            }
         }

         Debug.Send((Object)"player clicked inside recipe content");
         Bukkit.getScheduler().runTask(CraftEnhance.self(), () -> {
            Bukkit.getServer().getPluginManager().callEvent(new CustomPrepareCraftEvent(this));
         });
      } else {
         Debug.Send((Object)"player clicked result slot");
         if (this.result == null && this.getResult() != null) {
            throw new IllegalStateException("custom table has result set but no recipe in memory");
         } else if (this.result == null) {
            Debug.Send((Object)"recipe is null");
         } else {
            e.setCancelled(true);
            int canCraft = 1;
            if (!e.getClick().equals(ClickType.SHIFT_LEFT) && !e.getClick().equals(ClickType.SHIFT_RIGHT)) {
               ItemStack onCursor = this.getPlayer().getOpenInventory().getCursor();
               if (!GuiUtil.isNull(onCursor)) {
                  Debug.Send((Object)("result has to add to nonempty cursor: " + onCursor.getType()));
                  if (!onCursor.isSimilar(this.result)) {
                     return;
                  }

                  if (onCursor.getAmount() + this.result.getAmount() > onCursor.getMaxStackSize()) {
                     return;
                  }

                  onCursor.setAmount(onCursor.getAmount() + this.result.getAmount());
               } else {
                  Debug.Send((Object)"setting empty cursor to result item");
                  this.getPlayer().getOpenInventory().setCursor(this.result.clone());
               }
            } else {
               Map<Integer, Integer> destination = GuiUtil.findDestination(this.result, this.getPlayer().getInventory(), this.getPlayer().getInventory().getContents().length * this.getResult().getMaxStackSize(), false, (List)null);
               int space = destination.keySet().stream().filter((x) -> {
                  return x != null && x != -1;
               }).mapToInt((x) -> {
                  return (Integer)destination.get(x);
               }).sum();
               canCraft = Math.min(this.findFittingRecipesAmount(), space);
               if (canCraft == 0) {
                  return;
               }

               int resultCloneAmount;
               for(int remainingRewardResults = canCraft; remainingRewardResults > 0; remainingRewardResults -= resultCloneAmount) {
                  ItemStack resultClone = this.result.clone();
                  resultCloneAmount = Math.min(remainingRewardResults, resultClone.getMaxStackSize());
                  resultClone.setAmount(resultCloneAmount);
                  this.getPlayer().getInventory().addItem(new ItemStack[]{resultClone});
               }
            }

            Debug.Send((Object)("Player is crafting " + canCraft + " recipes"));
            this.setMatrix(this.subtractRecipeFromMatrix(canCraft));
            Bukkit.getServer().getPluginManager().callEvent(new CustomPrepareCraftEvent(this));
         }
      }
   }

   public void handleOutsideClick(InventoryClickEvent e) {
      if (e.getClick().equals(ClickType.DOUBLE_CLICK)) {
         ItemStack collector = e.getView().getCursor();
         if (!GuiUtil.isNull(e.getView().getCursor()) && collector.isSimilar(this.result)) {
            e.setCancelled(true);
            return;
         }
      }

      if (e.getCurrentItem() != null) {
         if (e.getClick().equals(ClickType.SHIFT_LEFT) || e.getClick().equals(ClickType.SHIFT_RIGHT)) {
            Debug.Send((Object)"Player shift clicked item in own inventory");
            e.setCancelled(true);
            Map<Integer, Integer> destination = GuiUtil.findDestination(e.getCurrentItem(), this.getInventory(), -1, false, this.getTemplate().getFillSpace().subList(0, 9));
            int leftovers = (Integer)destination.getOrDefault(-1, 0);
            destination.remove(-1);
            if (!destination.isEmpty()) {
               destination.keySet().forEach((x) -> {
                  if (this.getInventory().getItem(x) == null) {
                     ItemStack fillItem = e.getCurrentItem().clone();
                     fillItem.setAmount((Integer)destination.get(x));
                     this.getInventory().setItem(x, fillItem);
                  } else {
                     this.getInventory().getItem(x).setAmount((Integer)destination.get(x) + this.getInventory().getItem(x).getAmount());
                  }

               });
               if (leftovers != 0) {
                  e.getCurrentItem().setAmount(leftovers);
               } else {
                  e.setCurrentItem((ItemStack)null);
               }

               Bukkit.getServer().getPluginManager().callEvent(new CustomPrepareCraftEvent(this));
            }
         }
      }
   }

   public void handleDragging(InventoryDragEvent e) {
      List<Integer> validDrags = (List)e.getRawSlots().stream().filter((x) -> {
         return this.getTemplate().getFillSpace().subList(0, 9).contains(x) || x >= this.inventory.getSize();
      }).collect(Collectors.toList());
      boolean updatedMatrix = validDrags.stream().anyMatch((x) -> {
         return this.getTemplate().getFillSpace().subList(0, 9).contains(x);
      });
      if (e.getRawSlots().size() == validDrags.size()) {
         if (updatedMatrix) {
            Bukkit.getScheduler().runTask(CraftEnhance.self(), () -> {
               Bukkit.getServer().getPluginManager().callEvent(new CustomPrepareCraftEvent(this));
            });
         }

      } else {
         e.setCancelled(true);
         int dropped = 0;
         int maxDrop = e.getOldCursor().getAmount();
         Iterator var6 = validDrags.iterator();

         while(var6.hasNext()) {
            int valid = (Integer)var6.next();
            if (dropped >= maxDrop) {
               break;
            }

            ItemStack validItem = e.getView().getItem(valid);
            if (GuiUtil.isNull(validItem)) {
               ItemStack clone = e.getOldCursor().clone();
               clone.setAmount(1);
               e.getView().setItem(valid, clone);
            } else {
               if (validItem.getAmount() == validItem.getMaxStackSize()) {
                  continue;
               }

               validItem.setAmount(validItem.getAmount() + 1);
            }

            ++dropped;
         }

         Debug.Send((Object)("player dropped " + dropped + " item"));
         ItemStack newCursor = e.getOldCursor().clone();
         if (dropped == newCursor.getAmount()) {
            Bukkit.getScheduler().runTask(CraftEnhance.self(), () -> {
               e.getView().setCursor((ItemStack)null);
            });
         } else {
            newCursor.setAmount(newCursor.getAmount() - dropped);
            Bukkit.getScheduler().runTask(CraftEnhance.self(), () -> {
               e.getView().setCursor(newCursor);
            });
         }

         if (updatedMatrix) {
            Bukkit.getServer().getPluginManager().callEvent(new CustomPrepareCraftEvent(this));
         }

      }
   }

   private int findFittingRecipesAmount() {
      int i = -1;
      int j = -1;
      ItemStack[] a = this.getMatrix();
      ItemStack[] b = this.recipeContent;
      int maxAmount = 64;

      while(true) {
         do {
            ++i;
         } while(i < a.length && a[i] == null);

         do {
            ++j;
         } while(j < b.length && b[j] == null);

         if (i == a.length || j == b.length) {
            if (i == a.length && j == b.length) {
               return maxAmount;
            } else {
               throw new IllegalStateException("recipe does not match, they don't have an equal amount of items");
            }
         }

         if (a[i].getAmount() < b[j].getAmount()) {
            throw new IllegalStateException("recipe doesn't match the amount");
         }

         maxAmount = Math.min(maxAmount, a[i].getAmount() / b[j].getAmount());
      }
   }

   private ItemStack[] subtractRecipeFromMatrix(int amount) {
      ItemStack[] a = (ItemStack[])Arrays.copyOf(this.getMatrix(), this.getMatrix().length);
      ItemStack[] b = this.recipeContent;
      int i = -1;
      int j = -1;

      while(true) {
         do {
            ++i;
         } while(i < a.length && a[i] == null);

         do {
            ++j;
         } while(j < b.length && b[j] == null);

         if (i == a.length || j == b.length) {
            if (i == a.length && j == b.length) {
               return a;
            } else {
               throw new IllegalStateException("recipe does not match, they don't have an equal amount of items");
            }
         }

         int newAmount = a[i].getAmount() - amount * b[j].getAmount();
         if (newAmount < 0) {
            throw new IllegalStateException("cannot craft " + amount + " items");
         }

         if (newAmount == 0) {
            a[i] = null;
         } else {
            a[i].setAmount(newAmount);
         }
      }
   }

   private ItemStack[] orderShapelessRecipeContent(ItemStack[] content) {
      int[] sortedIndexes = new int[content.length];
      ItemStack[] matrix = this.getMatrix();

      for(int i = 0; i < content.length; ++i) {
         sortedIndexes[i] = -1;
         if (content[i] != null) {
            for(int j = 0; j < matrix.length; ++j) {
               int finalJ = j;
               if (!Arrays.stream(sortedIndexes).anyMatch((x) -> {
                  return x == finalJ;
               }) && content[i].isSimilar(matrix[j])) {
                  sortedIndexes[i] = j;
               }
            }

            if (sortedIndexes[i] == -1) {
               throw new IllegalStateException("could not convert shapeless recipe to shaped recipe");
            }
         }
      }

      ItemStack[] shaped = new ItemStack[matrix.length];
      Arrays.fill(shaped, (Object)null);

      for(int j = 0; j < sortedIndexes.length; ++j) {
         if (sortedIndexes[j] != -1) {
            shaped[sortedIndexes[j]] = content[j];
         }
      }

      boolean swabbed = true;

      while(swabbed) {
         swabbed = false;

         for(int i = 0; i < shaped.length - 1; ++i) {
            if (shaped[i] != null) {
               for(int j = i + 1; j < shaped.length; ++j) {
                  if (shaped[i].isSimilar(shaped[j])) {
                     boolean isLarger = shaped[i].getAmount() > shaped[j].getAmount();
                     boolean needsLarger = matrix[i].getAmount() > matrix[j].getAmount();
                     if (!isLarger && needsLarger || isLarger && !needsLarger) {
                        GuiUtil.swap(shaped, i, j);
                        swabbed = true;
                     }
                  }
               }
            }
         }
      }

      return shaped;
   }

   public boolean isCancelResponsible() {
      return true;
   }

   public ItemStack getResult() {
      return this.inventory.getItem((Integer)this.getTemplate().getFillSpace().get(9));
   }

   public void setRecipe(ItemStack[] content, ItemStack result, boolean shapeless) {
      this.result = result;
      this.recipeContent = shapeless ? this.orderShapelessRecipeContent(content) : content;
      this.inventory.setItem((Integer)this.getTemplate().getFillSpace().get(9), result);
   }

   public ItemStack[] getMatrix() {
      return (ItemStack[])this.getTemplate().getFillSpace().subList(0, 9).stream().map((x) -> {
         return this.inventory.getItem(x);
      }).toArray((x$0) -> {
         return new ItemStack[x$0];
      });
   }

   public void setMatrix(ItemStack[] matrix) {
      for(int i = 0; i < matrix.length; ++i) {
         Debug.Send((Object)("setting item " + i + " to " + matrix[i]));
         this.getInventory().setItem((Integer)this.getTemplate().getFillSpace().get(i), matrix[i]);
      }

   }
}
