package com.dutchjelly.craftenhance.gui.util;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.exceptions.ConfigError;
import com.dutchjelly.craftenhance.files.CategoryData;
import com.dutchjelly.craftenhance.messaging.Messenger;
import craftenhance.libs.menulib.dependencies.rbglib.TextTranslator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiUtil {
   public static Inventory CopyInventory(ItemStack[] invContent, String title, InventoryHolder holder) {
      if (invContent == null) {
         return null;
      } else {
         List<ItemStack> copiedItems = (List)Arrays.stream(invContent).map((x) -> {
            return x == null ? null : x.clone();
         }).collect(Collectors.toList());
         if (copiedItems.size() != invContent.length) {
            throw new IllegalStateException("Failed to copy inventory items.");
         } else {
            Inventory copy = Bukkit.createInventory(holder, invContent.length, title);

            for(int i = 0; i < copiedItems.size(); ++i) {
               copy.setItem(i, (ItemStack)copiedItems.get(i));
            }

            return copy;
         }
      }
   }

   public static Inventory FillInventory(Inventory inv, List<Integer> fillSpots, List<ItemStack> items) {
      if (inv == null) {
         throw new ConfigError("Cannot fill null inventory");
      } else if (items.size() > fillSpots.size()) {
         throw new ConfigError("Too few slots to fill.");
      } else {
         for(int i = 0; i < items.size(); ++i) {
            if ((Integer)fillSpots.get(i) >= inv.getSize()) {
               throw new ConfigError("Fill spot is outside inventory.");
            }

            inv.setItem((Integer)fillSpots.get(i), (ItemStack)items.get(i));
         }

         return inv;
      }
   }

   public static ItemStack setTextItem( ItemStack itemStack, String displayName, List<String> lore) {
      ItemMeta meta = itemStack.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(setcolorName(displayName));
         meta.setLore(setcolorLore(lore));
      }

      itemStack.setItemMeta(meta);
      return itemStack;
   }

   public static String setcolorName(String name) {
      return name == null ? null : TextTranslator.toSpigotFormat(name);
   }

   public static List<String> setcolorLore(List<String> lore) {
      List<String> lores = new ArrayList();
      Iterator var2 = lore.iterator();

      while(var2.hasNext()) {
         String text = (String)var2.next();
         if (text != null) {
            lores.add(TextTranslator.toSpigotFormat(text));
//            lores.add((Object)null);
         } else {
         }
      }

      return lores;
   }

   public static ItemStack ReplaceAllPlaceHolders(ItemStack item, Map<String, String> placeholders) {
      if (item == null) {
         return null;
      } else {
         placeholders.forEach((key, value) -> {
            ReplacePlaceHolder(item, key, value);
         });
         return item;
      }
   }

   public static ItemStack ReplacePlaceHolder(ItemStack item, String placeHolder, String value) {
      if (item == null) {
         return null;
      } else if (value == null) {
         return null;
      } else {
         ItemMeta meta = item.getItemMeta();
         if (meta.getDisplayName().contains(placeHolder)) {
            meta.setDisplayName(meta.getDisplayName().replace(placeHolder, value));
            item.setItemMeta(meta);
         }

         List<String> lore = meta.getLore();
         if (lore == null) {
            return item;
         } else {
            lore = (List)lore.stream().map((x) -> {
               return x == null ? null : x.replace(placeHolder, value);
            }).collect(Collectors.toList());
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
         }
      }
   }

   public static Map<Integer, Integer> findDestination(ItemStack item, Inventory inv, int amount, boolean preferEmpty, List<Integer> whitelist) {
      if (item == null) {
         return new HashMap();
      } else if (inv == null) {
         throw new RuntimeException("cannot try to fit item into null inventory");
      } else {
         if (amount == -1) {
            amount = item.getAmount();
         }

         ItemStack[] storage = inv.getStorageContents();
         Map<Integer, Integer> destination = new HashMap();
         int remainingItemQuantity = amount;
         int i;
         if (preferEmpty) {
            for(i = 0; i < storage.length; ++i) {
               if (whitelist == null || whitelist.contains(i)) {
                  if (storage[i] == null) {
                     destination.put(i, Math.min(remainingItemQuantity, item.getMaxStackSize()));
                     remainingItemQuantity -= (Integer)destination.get(i);
                  }

                  if (remainingItemQuantity == 0) {
                     return destination;
                  }
               }
            }
         }

         for(i = 0; i < storage.length; ++i) {
            if (whitelist == null || whitelist.contains(i)) {
               if (storage[i] == null && !destination.containsKey(i)) {
                  destination.put(i, Math.min(remainingItemQuantity, item.getMaxStackSize()));
                  remainingItemQuantity -= (Integer)destination.get(i);
               } else if (storage[i].getAmount() < storage[i].getMaxStackSize() && storage[i].isSimilar(item)) {
                  int room = Math.min(remainingItemQuantity, storage[i].getMaxStackSize() - storage[i].getAmount());
                  destination.put(i, room);
                  remainingItemQuantity -= room;
               }

               if (remainingItemQuantity == 0) {
                  return destination;
               }
            }
         }

         if (remainingItemQuantity > 0) {
            destination.put(-1, remainingItemQuantity);
         }

         return destination;
      }
   }

   public static <T> void swap(T[] list, int a, int b) {
      T t = list[a];
      list[a] = list[b];
      list[b] = t;
   }

   /** @deprecated */
   @Deprecated
   public static boolean isNull(ItemStack item) {
      return item == null || item.getType().equals(Material.AIR);
   }

   public static int invSize(String menu, int size) {
      if (size < 9) {
         return 9;
      } else if (size % 9 == 0) {
         return size;
      } else if (size <= 18) {
         return 18;
      } else if (size <= 27) {
         return 27;
      } else if (size <= 36) {
         return 36;
      } else if (size <= 45) {
         return 45;
      } else {
         if (size > 54) {
            Messenger.Error("This menu " + menu + " has set bigger inventory size an it can handle, your set size " + size + ". will defult to 54.");
         }

         return 54;
      }
   }

   public static boolean changeCategoryName(String currentCatogory, String msg, Player player) {
      if (!msg.equals("") && !msg.equals("cancel") && !msg.equals("quit") && !msg.equals("exit")) {
         if (!msg.isEmpty()) {
            CategoryData categoryData = CraftEnhance.self().getCategoryDataCache().get(currentCatogory);
            if (categoryData == null) {
               Messenger.Message("Your category name not exist", player);
               return true;
            } else {
               CategoryData newCategoryData = CraftEnhance.self().getCategoryDataCache().of(currentCatogory, categoryData.getRecipeCategoryItem(), msg);
               newCategoryData.setEnhancedRecipes(categoryData.getEnhancedRecipes());
               CraftEnhance.self().getCategoryDataCache().put(currentCatogory, newCategoryData);
               Bukkit.getScheduler().runTaskLaterAsynchronously(CraftEnhance.self(), () -> {
                  CraftEnhance.self().getCategoryDataCache().save();
               }, 1L);
               return false;
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean changeCategoryItem(String currentCatogory, String msg, Player player) {
      System.out.println("msg.equals(\"quit\") " + msg.equals("quit"));
      if (!msg.equals("") && !msg.equals("cancel") && !msg.equals("quit") && !msg.equals("exit") && !msg.equals("q")) {
         if (!msg.isEmpty()) {
            CategoryData categoryData = CraftEnhance.self().getCategoryDataCache().get(currentCatogory);
            if (categoryData == null) {
               Messenger.Message("Your category name not exist", player);
               return true;
            } else {
               Material material = Material.getMaterial(msg.toUpperCase());
               if (material == null) {
                  Messenger.Message("Your material name not exist " + msg, player);
                  return true;
               } else {
                  CategoryData newCategoryData = CraftEnhance.self().getCategoryDataCache().of(currentCatogory, new ItemStack(material), categoryData.getDisplayName());
                  newCategoryData.setEnhancedRecipes(categoryData.getEnhancedRecipes());
                  CraftEnhance.self().getCategoryDataCache().put(currentCatogory, newCategoryData);
                  Bukkit.getScheduler().runTaskLaterAsynchronously(CraftEnhance.self(), () -> {
                     CraftEnhance.self().getCategoryDataCache().save();
                  }, 1L);
                  return false;
               }
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean changeCategory(String currentCatogory, String msg, Player player) {
      if (!msg.equals("") && !msg.equals("cancel") && !msg.equals("quit") && !msg.equals("exit")) {
         if (!msg.isEmpty()) {
            CategoryData categoryData = CraftEnhance.self().getCategoryDataCache().get(currentCatogory);
            if (categoryData == null) {
               Messenger.Message("Your category name not exist", player);
               return true;
            } else {
               CategoryData movedcategoryData = CraftEnhance.self().getCategoryDataCache().move(currentCatogory, msg);
               Bukkit.getScheduler().runTaskLaterAsynchronously(CraftEnhance.self(), () -> {
                  CraftEnhance.self().getCategoryDataCache().save();
                  if (movedcategoryData != null) {
                     movedcategoryData.getEnhancedRecipes().forEach(EnhancedRecipe::save);
                  }

               }, 1L);
               return false;
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean newCategory(String msg, Player player) {
      if (!msg.equals("") && !msg.equals("cancel") && !msg.equals("quit") && !msg.equals("exit")) {
         if (!msg.isEmpty()) {
            String[] split = msg.split(" ");
            if (split.length > 1) {
               Material material = Material.getMaterial(split[1].toUpperCase());
               if (material == null) {
                  Messenger.Message("Please input valid item name. Your input " + split[1], player);
                  return true;
               }

               if (CraftEnhance.self().getCategoryDataCache().addCategory(split[0], new ItemStack(material), (String)null)) {
                  Messenger.Message("Your category name alredy exist", player);
                  return true;
               }

               Bukkit.getScheduler().runTaskLaterAsynchronously(CraftEnhance.self(), () -> {
                  CraftEnhance.self().getCategoryDataCache().save();
               }, 1L);
               return false;
            }

            Messenger.Message("Please input valid item name and category. Your input " + msg, player);
            Messenger.Message("Type it like this 'category' 'itemname' ", player);
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean changeOrCreateCategory(String msg, Player player) {
      if (!msg.equals("") && !msg.equalsIgnoreCase("q") && !msg.equalsIgnoreCase("cancel") && !msg.equalsIgnoreCase("quit") && !msg.equalsIgnoreCase("exit")) {
         if (!msg.isEmpty()) {
            String[] split = msg.split(" ");
            if (split.length > 1) {
               Material material = null;
               CategoryData categoryData = CraftEnhance.self().getCategoryDataCache().get(split[0]);
               if (split.length >= 3) {
                  material = Material.getMaterial(split[2].toUpperCase());
               }

               if (categoryData == null) {
                  if (material == null) {
                     material = Material.getMaterial(split[1].toUpperCase());
                  }

                  if (material == null) {
                     Messenger.Message("Please input valid item name. Your input " + msg, player);
                     return true;
                  }

                  CraftEnhance.self().getCategoryDataCache().addCategory(split[0], new ItemStack(material), (String)null);
               } else {
                  CategoryData newCategoryData = CraftEnhance.self().getCategoryDataCache().of(split[1], material != null ? new ItemStack(material) : categoryData.getRecipeCategoryItem(), (String)null);
                  CraftEnhance.self().getCategoryDataCache().remove(split[0]);
                  newCategoryData.setEnhancedRecipes(categoryData.getEnhancedRecipes());
                  CraftEnhance.self().getCategoryDataCache().put(split[1], newCategoryData);
               }

               return false;
            }

            Messenger.Message("Please input valid item name and category. Your input " + msg, player);
            Messenger.Message("Type it like this 'category' 'itemname' ", player);
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean seachCategory(String msg) {
      if (msg.equals("")) {
         return false;
      } else {
         return !msg.equals("cancel") && !msg.equals("quit") && !msg.equals("exit");
      }
   }
}
