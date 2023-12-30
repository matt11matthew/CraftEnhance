package craftenhance.libs.menulib;

import craftenhance.libs.menulib.builders.ButtonData;
import craftenhance.libs.menulib.builders.MenuDataUtility;
import craftenhance.libs.menulib.utility.Function;
import craftenhance.libs.menulib.utility.PairFunction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MenuHolder extends MenuUtility {
   public MenuHolder() {
      this((List)null, (List)null, false);
   }

   public MenuHolder(List<?> fillItems) {
      this((List)null, fillItems, false);
   }

   public MenuHolder(boolean shallCacheItems) {
      this((List)null, (List)null, shallCacheItems);
   }

   public MenuHolder(List<Integer> fillSlots, List<?> fillItems) {
      this(fillSlots, fillItems, false);
   }

   public MenuHolder(List<Integer> fillSlots, List<?> fillItems, boolean shallCacheItems) {
      super(fillSlots, fillItems, shallCacheItems);
   }

   public void menuClose(InventoryCloseEvent event, MenuUtility menu) {
   }

   public void menuOpen(Player player, Location location) {
      this.menuOpen(player, location, true);
   }

   public void menuOpen(Player player) {
      this.menuOpen(player, (Location)null, false);
   }

   public void menuOpen(Player player, Location location, boolean loadToCahe) {
      this.player = player;
      this.location = location;
      player.closeInventory();
      if (location != null) {
         this.setLocationMetaOnPlayer(player, location);
      }

      if (!this.shallCacheItems) {
         this.addItemsToCache();
      }

      this.reddrawInventory();
      Inventory menu = this.loadInventory(player, loadToCahe);
      if (menu != null) {
         player.openInventory(menu);
         this.onMenuOpenPlaySound();
         this.setMetadataKey(MenuMetadataKey.MENU_OPEN.name());
         if (!this.getButtonsToUpdate().isEmpty()) {
            this.updateButtonsInList();
         }

         Bukkit.getScheduler().runTaskLater(this.plugin, this::updateTittle, 1L);
      }
   }

   public void setMenuSize(int inventorySize) {
      this.inventorySize = inventorySize;
   }

   public void setTitle(String title) {
      this.setTitle(() -> {
         return title;
      });
   }

   public void setTitle(Function<String> function) {
      this.function = function;
   }

   public void setAnimateTitle(int time, PairFunction<String> function) {
      this.animateTitleTime = time;
      this.animateTitle = function;
      this.animateTitle();
   }

   public void setInventoryType(InventoryType inventoryType) {
      this.inventoryType = inventoryType;
   }

   public void setItemsPerPage(int itemsPerPage) {
      if (itemsPerPage <= 0) {
         this.itemsPerPage = this.inventorySize;
      } else {
         this.itemsPerPage = itemsPerPage;
      }

   }

   public void setFillSpace(String fillSpace) {
      ArrayList slotList = new ArrayList();

      try {
         String[] var3 = fillSpace.split(",");
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String slot = var3[var5];
            if (!slot.equals("")) {
               if (slot.contains("-")) {
                  int firstSlot = Integer.parseInt(slot.split("-")[0]);
                  int lastSlot = Integer.parseInt(slot.split("-")[1]);
                  slotList.addAll((Collection)IntStream.rangeClosed(firstSlot, lastSlot).boxed().collect(Collectors.toList()));
               } else {
                  slotList.add(Integer.valueOf(slot));
               }
            }
         }
      } catch (NumberFormatException var9) {
         throw new NumberFormatException("can not parse this " + fillSpace + " as numbers.");
      }

      this.setFillSpace((List)slotList);
   }

   public void setFillSpace(List<Integer> fillSpace) {
      this.fillSpace = fillSpace;
   }

   public void setMenuOpenSound(Sound sound) {
      this.menuOpenSound = sound;
   }

   public void setSlotsYouCanAddItems(boolean slotsYouCanAddItems) {
      this.slotsYouCanAddItems = slotsYouCanAddItems;
   }

   public void setAllowShiftClick(boolean allowShiftClick) {
      this.allowShiftClick = allowShiftClick;
   }

   public boolean setPage(int page) {
      if (!this.containsPage(page)) {
         return false;
      } else {
         this.pageNumber = page;
         this.updateButtons();
         this.updateTittle();
         return true;
      }
   }

   public int getSlotIndex() {
      return this.slotIndex;
   }

   public void previousPage() {
      this.changePage(false);
   }

   public void nextPage() {
      this.changePage(true);
   }

   public void updateButton(MenuButton menuButton) {
      MenuDataUtility menuDataUtility = this.getMenuData(this.getPageNumber());
      Set<Integer> buttonSlots = this.getButtonSlots(menuDataUtility, menuButton);
      if (menuDataUtility != null && this.getMenu() != null) {
         if (!buttonSlots.isEmpty()) {
            Iterator var8 = buttonSlots.iterator();

            while(var8.hasNext()) {
               int slot = (Integer)var8.next();
               ButtonData buttonData = menuDataUtility.getButton(this.getSlot(slot));
               if (buttonData == null) {
                  return;
               }

               ItemStack menuItem = this.getMenuItem(menuButton, buttonData, slot, true);
               this.getMenu().setItem(slot, menuItem);
               menuDataUtility.putButton(this.getSlot(slot), new ButtonData(menuItem, buttonData.getMenuButton(), buttonData.getObject()), menuDataUtility.getFillMenuButton(this.getSlot(slot)));
            }
         } else {
            int buttonSlot = this.getButtonSlot(menuButton);
            ButtonData buttonData = menuDataUtility.getButton(this.getSlot(buttonSlot));
            if (buttonData == null) {
               return;
            }

            ItemStack itemStack = this.getMenuItem(menuButton, buttonData, buttonSlot, true);
            this.getMenu().setItem(buttonSlot, itemStack);
            menuDataUtility.putButton(this.getSlot(buttonSlot), new ButtonData(itemStack, menuButton, buttonData.getObject()), menuDataUtility.getFillMenuButton(this.getSlot(buttonSlot)));
         }

         this.putAddedButtonsCache(this.getPageNumber(), menuDataUtility);
      }

   }

   public void updateButtons() {
      super.updateButtons();
   }

   public void setignoreItemCheck(boolean ignoreItemCheck) {
      this.ignoreItemCheck = ignoreItemCheck;
   }

   public void setUpdateTime(int updateTime) {
      this.updateTime = updateTime;
   }

   public void setAutoClearCache(boolean autoClearCache) {
      this.autoClearCache = autoClearCache;
   }

   public void setIgnoreValidCheck(boolean ignoreValidCheck) {
      this.ignoreValidCheck = ignoreValidCheck;
   }

   public int getAmountOfViewers() {
      return (int)(this.getMenu() == null ? -1L : this.getMenu().getViewers().stream().filter((entity) -> {
         return entity instanceof Player;
      }).count() - 1L);
   }

   public void setAutoTitleCurrentPage(boolean autoTitleCurrentPage) {
      this.autoTitleCurrentPage = autoTitleCurrentPage;
   }

   @Nullable
   public MenuCacheKey getMenuCacheKey() {
      return this.menuCacheKey;
   }
}
