package craftenhance.libs.menulib.utility.Item;

import java.util.ListIterator;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemStackCounters {
   public static int countItemStacks(ItemStack[] itemStacks, ItemStack[] items) {
      int countItems = 0;
      ItemStack[] var3 = itemStacks;
      int var4 = itemStacks.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         ItemStack itemStack = var3[var5];
         ItemStack[] var7 = items;
         int var8 = items.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            ItemStack item = var7[var9];
            if (itemStack != null && itemStack.isSimilar(item) && itemStack.getType() != Material.AIR) {
               countItems += itemStack.getAmount();
            }
         }
      }

      return countItems;
   }

   public static int countItemStacks(Inventory inventoryItems, ItemStack item) {
      int countItems = 0;
      ItemStack[] var3 = inventoryItems.getContents();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         ItemStack itemStack = var3[var5];
         if (itemStack != null && itemStack.isSimilar(item) && itemStack.getType() != Material.AIR) {
            ++countItems;
         }
      }

      return countItems;
   }

   public static int countItemStacks(ItemStack item, Inventory inventoryItems) {
      return countItemStacks(item, inventoryItems, false);
   }

   public static int countItemStacks(ItemStack item, Inventory inventoryItems, boolean onlyNoFullItems) {
      int countItems = 0;
      ItemStack[] var4 = inventoryItems.getContents();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         ItemStack itemStack = var4[var6];
         if (onlyNoFullItems) {
            if (itemStack.getAmount() != itemStack.getMaxStackSize() && itemStack != null && itemStack.isSimilar(item) && itemStack.getType() != Material.AIR) {
               countItems += itemStack.getAmount();
            }
         } else if (itemStack != null && itemStack.isSimilar(item) && itemStack.getType() != Material.AIR) {
            countItems += itemStack.getAmount();
         }
      }

      return countItems;
   }

   public static int countItemStacks(ItemStack[] itemStacks, Inventory inventoryItems) {
      int countItems = 0;
      ItemStack[] var3 = inventoryItems.getContents();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         ItemStack itemStack = var3[var5];
         ItemStack[] var7 = itemStacks;
         int var8 = itemStacks.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            ItemStack item = var7[var9];
            if (itemStack != null && itemStack.isSimilar(item) && itemStack.getType() != Material.AIR) {
               countItems += itemStack.getAmount();
            }
         }
      }

      return countItems;
   }

   public static int countItemStacks(ItemStack[] itemStacks) {
      int countItems = 0;
      ItemStack[] var2 = itemStacks;
      int var3 = itemStacks.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         ItemStack item = var2[var4];
         if (item != null && item.getType() != Material.AIR) {
            countItems += item.getAmount();
         }
      }

      return countItems;
   }

   public static boolean isPlaceLeftInventory(Inventory inventory, int amount, Material materialToMatch) {
      ListIterator var3 = inventory.iterator();

      ItemStack item;
      do {
         do {
            do {
               if (!var3.hasNext()) {
                  return false;
               }

               item = (ItemStack)var3.next();
            } while(item == null);
         } while(item.getType() == materialToMatch && amount > countItemStacks(item, inventory, true));
      } while(item.getType() != materialToMatch || amount + item.getAmount() > item.getMaxStackSize());

      return true;
   }
}
