package craftenhance.libs.menulib;

import craftenhance.libs.menulib.messages.SendMsgDuplicatedItems;
import craftenhance.libs.menulib.utility.Item.CreateItemStack;
import craftenhance.libs.menulib.utility.Item.ItemStackCounters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CheckItemsInsideInventory {
   private static final Map<UUID, Map<ItemStack, Integer>> duplicatedItems = new HashMap();
   private boolean sendMsgPlayer = false;
   private final List<String> blacklistedItems = new ArrayList();
   private final List<Integer> slotsToCheck = new ArrayList();

   public void setBlacklistedItems(List<String> blacklistedItems) {
      this.blacklistedItems.addAll(blacklistedItems);
   }

   public List<Integer> getSlotsToCheck() {
      return this.slotsToCheck;
   }

   public void setSlotsToCheck(int... slotsToCheck) {
      if (slotsToCheck != null) {
         int[] var2 = slotsToCheck;
         int var3 = slotsToCheck.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            int slot = var2[var4];
            this.slotsToCheck.add(slot);
         }
      }

   }

   public void setSlotsToCheck(List<Integer> slotsToCheck) {
      if (slotsToCheck != null) {
         this.slotsToCheck.addAll(slotsToCheck);
      }

   }

   public Map<Integer, ItemStack> getItemsOnSpecifiedSlots(Inventory inv, Player player, boolean shallCheckDuplicates) {
      return this.getItemsOnSpecifiedSlots(inv, player, (Location)null, shallCheckDuplicates);
   }

   public Map<Integer, ItemStack> getItemsOnSpecifiedSlots(Inventory inv, Player player) {
      return this.getItemsOnSpecifiedSlots(inv, player, (Location)null, true);
   }

   public Map<Integer, ItemStack> getItemsOnSpecifiedSlots(Inventory inv, Player player, Location location) {
      return this.getItemsOnSpecifiedSlots(inv, player, location, true);
   }

   public Map<Integer, ItemStack> getItemsOnSpecifiedSlots(Inventory inv, Player player, Location location, boolean shallCheckDuplicates) {
      Map<Integer, ItemStack> items = new HashMap();
      if (!this.getSlotsToCheck().isEmpty()) {
         Iterator var6 = this.getSlotsToCheck().iterator();

         while(var6.hasNext()) {
            int slot = (Integer)var6.next();
            this.getInventoryItems(items, inv, player, slot);
         }
      } else {
         for(int i = 0; i < inv.getSize() - 9; ++i) {
            this.getInventoryItems(items, inv, player, i);
         }
      }

      return (Map)(shallCheckDuplicates ? this.addToMuchItems(items, player, inv, location) : items);
   }

   public Map<Integer, ItemStack> getInventoryItems(Map<Integer, ItemStack> items, Inventory inv, Player player, int slot) {
      if (slot > inv.getSize()) {
         return items;
      } else {
         ItemStack item = inv.getItem(slot);
         if (this.chekItemAreOnBlacklist(item)) {
            this.addItemsBackToPlayer(player, item);
         } else {
            items.put(slot, item != null && !isAir(item.getType()) ? item : null);
         }

         return items;
      }
   }

   private Map<Integer, ItemStack> addToMuchItems(Map<Integer, ItemStack> items, Player player, Inventory inventory, Location location) {
      Map<Integer, ItemStack> itemStacksNoDubbleEntity = new HashMap();
      Map<ItemStack, Integer> chachedDuplicatedItems = new HashMap();
      Set<ItemStack> set = new HashSet();
      this.sendMsgPlayer = false;
      Iterator var8 = items.entrySet().iterator();

      while(var8.hasNext()) {
         Entry<Integer, ItemStack> entitys = (Entry)var8.next();
         if (entitys.getValue() != null) {
            if (((ItemStack)entitys.getValue()).getAmount() > 1) {
               chachedDuplicatedItems.put(CreateItemStack.createItemStackAsOne((ItemStack)entitys.getValue()), ItemStackCounters.countItemStacks((ItemStack)entitys.getValue(), inventory) - 1);
               duplicatedItems.put(player.getUniqueId(), chachedDuplicatedItems);
            }

            if (!set.add(CreateItemStack.createItemStackAsOne((ItemStack)entitys.getValue()))) {
               chachedDuplicatedItems.put(CreateItemStack.createItemStackAsOne((ItemStack)entitys.getValue()), ItemStackCounters.countItemStacks((ItemStack)entitys.getValue(), inventory) - 1);
               duplicatedItems.put(player.getUniqueId(), chachedDuplicatedItems);
            } else {
               itemStacksNoDubbleEntity.put(entitys.getKey(), CreateItemStack.createItemStackAsOne((ItemStack)entitys.getValue()));
            }
         }
      }

      this.addItemsBackToPlayer(location);
      return itemStacksNoDubbleEntity;
   }

   private void addItemsBackToPlayer(Player player, ItemStack itemStack) {
      HashMap<Integer, ItemStack> ifInventorFull = player.getInventory().addItem(new ItemStack[]{itemStack});
      if (!ifInventorFull.isEmpty() && player.getLocation().getWorld() != null) {
         player.getLocation().getWorld().dropItemNaturally(player.getLocation(), (ItemStack)ifInventorFull.get(0));
      }

      if (!this.sendMsgPlayer) {
         SendMsgDuplicatedItems.sendBlacklistMessage(player, itemStack.getType().name().toLowerCase());
         this.sendMsgPlayer = true;
      }

   }

   private void addItemsBackToPlayer(Location location) {
      Iterator var2 = duplicatedItems.keySet().iterator();

      while(var2.hasNext()) {
         UUID playerUUID = (UUID)var2.next();
         Iterator var4 = ((Map)duplicatedItems.get(playerUUID)).entrySet().iterator();

         while(var4.hasNext()) {
            Entry<ItemStack, Integer> items = (Entry)var4.next();
            ItemStack itemStack = (ItemStack)items.getKey();
            int amount = (Integer)items.getValue();
            itemStack.setAmount(amount);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
            if (offlinePlayer.getPlayer() != null) {
               HashMap<Integer, ItemStack> ifInventorFull = offlinePlayer.getPlayer().getInventory().addItem(new ItemStack[]{itemStack});
               if (!ifInventorFull.isEmpty() && offlinePlayer.getPlayer().getLocation().getWorld() != null) {
                  offlinePlayer.getPlayer().getLocation().getWorld().dropItemNaturally(offlinePlayer.getPlayer().getLocation(), (ItemStack)ifInventorFull.get(0));
               }

               SendMsgDuplicatedItems.sendDublicatedMessage(offlinePlayer.getPlayer(), itemStack.getType(), duplicatedItems.size(), amount);
            } else if (location != null && location.getWorld() != null) {
               location.getWorld().dropItemNaturally(location, itemStack);
            }
         }

         duplicatedItems.remove(playerUUID);
      }

   }

   private boolean chekItemAreOnBlacklist(ItemStack itemStack) {
      List<String> itemStacks = this.blacklistedItems;
      if (itemStack != null && itemStacks != null) {
         Iterator var3 = itemStacks.iterator();

         while(var3.hasNext()) {
            String item = (String)var3.next();
            if (CreateItemStack.of(item).makeItemStack().isSimilar(itemStack)) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean isAir(Material material) {
      return nameEquals(material, "AIR", "CAVE_AIR", "VOID_AIR", "LEGACY_AIR");
   }

   private static boolean nameEquals(Material mat, String... names) {
      String matName = mat.toString();
      String[] var3 = names;
      int var4 = names.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String name = var3[var5];
         if (matName.equals(name)) {
            return true;
         }
      }

      return false;
   }
}
