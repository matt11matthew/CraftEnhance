package craftenhance.libs.menulib;

import com.google.common.base.Enums;
import craftenhance.libs.menulib.NMS.UpdateTittleContainers;
import craftenhance.libs.menulib.builders.ButtonData;
import craftenhance.libs.menulib.builders.MenuDataUtility;
import craftenhance.libs.menulib.utility.Function;
import craftenhance.libs.menulib.utility.Metadata;
import craftenhance.libs.menulib.utility.PairFunction;
import craftenhance.libs.menulib.utility.Validate;
import craftenhance.libs.menulib.utility.Item.CreateItemStack;
import craftenhance.libs.menulib.utility.Item.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MenuUtility {
   MenuCacheKey menuCacheKey;
   private final MenuCache menuCache = MenuCache.getInstance();
   private final List<MenuButton> buttonsToUpdate = new ArrayList();
   private final Map<Integer, MenuDataUtility> pagesOfButtonsData = new HashMap();
   private final Map<Integer, Long> timeWhenUpdatesButtons = new HashMap();
   protected List<Integer> fillSpace;
   private final List<?> listOfFillItems;
   protected final Plugin plugin = RegisterMenuAPI.getPLUGIN();
   private Inventory inventory;
   protected InventoryType inventoryType;
   protected int taskid;
   protected boolean shallCacheItems;
   protected boolean slotsYouCanAddItems;
   protected boolean allowShiftClick;
   protected boolean ignoreValidCheck;
   protected boolean autoClearCache;
   protected boolean ignoreItemCheck;
   protected boolean autoTitleCurrentPage;
   protected int slotIndex;
   private int numberOfFillitems;
   private int requiredPages;
   private int manuallySetPages = -1;
   protected int inventorySize;
   protected int itemsPerPage;
   protected int pageNumber;
   protected int updateTime;
   protected int taskidAnimateTitle;
   protected int animateTitleTime;
   protected PairFunction<String> animateTitle;
   protected Player player;
   protected Sound menuOpenSound;
   protected String title;
   protected Function<String> function;
   private String playermetadataKey;
   private String uniqueKey;
   protected Location location;

   public MenuUtility(@Nullable List<Integer> fillSlots, @Nullable List<?> fillItems, boolean shallCacheItems) {
      this.itemsPerPage = this.inventorySize;
      this.fillSpace = fillSlots;
      this.listOfFillItems = fillItems;
      this.shallCacheItems = shallCacheItems;
      this.allowShiftClick = true;
      this.autoClearCache = true;
      this.ignoreItemCheck = false;
      this.autoTitleCurrentPage = true;
      this.slotIndex = 0;
      this.updateTime = -1;
      this.menuOpenSound = Enums.getIfPresent(Sound.class, "BLOCK_NOTE_BLOCK_BASEDRUM").orNull() == null ? (Sound)Enums.getIfPresent(Sound.class, "BLOCK_NOTE_BASEDRUM").orNull() : (Sound)Enums.getIfPresent(Sound.class, "BLOCK_NOTE_BLOCK_BASEDRUM").orNull();
      this.uniqueKey = "";
   }

   public ItemStack getItemAt(int slot) {
      throw new Validate.CatchExceptions("WARN not in use");
   }

   public ItemStack getFillItemsAt(Object o) {
      throw new Validate.CatchExceptions("WARN not in use");
   }

   public ItemStack getFillItemsAt(int slot) {
      throw new Validate.CatchExceptions("WARN not in use");
   }

   @Nullable
   public MenuButton getButtonAt(int slot) {
      return null;
   }

   @Nullable
   public MenuButton getFillButtonAt(@Nonnull Object object) {
      return null;
   }

   @Nullable
   public MenuButton getFillButtonAt(int slot) {
      return null;
   }

   @Nullable
   public InventoryType getInventoryType() {
      return this.inventoryType;
   }

   @Nonnull
   protected Map<Integer, Long> getTimeWhenUpdatesButtons() {
      return this.timeWhenUpdatesButtons;
   }

   @Nullable
   protected Long getTimeWhenUpdatesButton(MenuButton menuButton) {
      return (Long)this.getTimeWhenUpdatesButtons().getOrDefault(menuButton.getId(), null);
   }

   public boolean isAllowShiftClick() {
      return this.allowShiftClick;
   }

   public boolean isSlotsYouCanAddItems() {
      return this.slotsYouCanAddItems;
   }

   public int getUpdateTime() {
      return this.updateTime;
   }

   public boolean isIgnoreItemCheck() {
      return this.ignoreItemCheck;
   }

   public boolean isAddedButtonsCacheEmpty() {
      return this.pagesOfButtonsData.isEmpty();
   }

   public boolean containsPage(Integer pageNumber) {
      return this.pagesOfButtonsData.containsKey(pageNumber);
   }

   @Nullable
   public MenuDataUtility getMenuData(int pageNumber) {
      return (MenuDataUtility)this.pagesOfButtonsData.get(pageNumber);
   }

   public Map<Integer, ButtonData> getMenuButtons(int pageNumber) {
      MenuDataUtility utilityMap = (MenuDataUtility)this.pagesOfButtonsData.get(pageNumber);
      return (Map)(utilityMap != null ? utilityMap.getButtons() : new HashMap());
   }

   @Nonnull
   public ButtonData getAddedButtons(int pageNumber, int slotIndex) {
      Map<Integer, ButtonData> data = this.getMenuButtons(pageNumber);
      if (data != null) {
         ButtonData buttonData = (ButtonData)data.get(slotIndex);
         if (buttonData != null) {
            return buttonData;
         }
      }

      return new ButtonData((ItemStack)null, (MenuButton)null, "");
   }

   public int getButtonSlot(MenuButton menuButton) {
      Map<Integer, ButtonData> data = this.getMenuButtons(this.getPageNumber());
      if (data == null) {
         return -1;
      } else {
         Iterator var3 = data.entrySet().iterator();

         Entry entry;
         do {
            if (!var3.hasNext()) {
               return -1;
            }

            entry = (Entry)var3.next();
         } while(((ButtonData)entry.getValue()).getMenuButton() != menuButton);

         return (Integer)entry.getKey() - this.getPageNumber() * this.getInventorySize();
      }
   }

   @Nonnull
   public Set<Integer> getButtonSlots(MenuDataUtility menuDataUtility, MenuButton menuButton) {
      Set<Integer> slots = new HashSet();
      if (menuDataUtility == null) {
         return slots;
      } else {
         int menuButtonId = menuButton.getId();
         Iterator var5 = menuDataUtility.getButtons().entrySet().iterator();

         while(var5.hasNext()) {
            Entry<Integer, ButtonData> entry = (Entry)var5.next();
            MenuButton chacheMenuButton = ((ButtonData)entry.getValue()).getMenuButton();
            MenuButton fillMenuButton = menuDataUtility.getFillMenuButton(menuButton);
            if (chacheMenuButton == null) {
               if (fillMenuButton != null && fillMenuButton.getId() == menuButtonId) {
                  slots.add((Integer)entry.getKey() - this.getPageNumber() * this.getInventorySize());
               }
            } else if (menuButtonId == chacheMenuButton.getId()) {
               slots.add((Integer)entry.getKey() - this.getPageNumber() * this.getInventorySize());
            }
         }

         return slots;
      }
   }

   /** @deprecated */
   @Deprecated
   public List<MenuButton> getButtons() {
      return null;
   }

   public List<MenuButton> getButtonsToUpdate() {
      return this.buttonsToUpdate;
   }

   public Player getViewer() {
      return this.player;
   }

   public int getAmountOfViewers() {
      return (int)(this.getMenu() == null ? -1L : this.getMenu().getViewers().stream().filter((entity) -> {
         return entity instanceof Player;
      }).count() - 1L);
   }

   @Nullable
   public Inventory getMenu() {
      return this.inventory;
   }

   public void setManuallyAmountOfPages(int amountOfPages) {
      this.manuallySetPages = amountOfPages;
   }

   public int getPageNumber() {
      return this.pageNumber;
   }

   public int getRequiredPages() {
      return this.requiredPages;
   }

   public boolean isAutoTitleCurrentPage() {
      return this.autoTitleCurrentPage;
   }

   @Nonnull
   public Map<Integer, ButtonData> getMenuButtonsCache() {
      return this.addItemsToCache();
   }

   public String getPlayermetadataKey() {
      return this.playermetadataKey;
   }

   @Nullable
   public MenuUtility getMenuholder(Player player) {
      return this.getMenuholder(player, MenuMetadataKey.MENU_OPEN);
   }

   public MenuUtility getPreviousMenuholder(Player player) {
      return this.getMenuholder(player, MenuMetadataKey.MENU_OPEN_PREVIOUS);
   }

   private MenuUtility getMenuholder(Player player, MenuMetadataKey metadataKey) {
      return Metadata.hasPlayerMetadata(player, metadataKey) ? Metadata.getPlayerMenuMetadata(player, metadataKey) : null;
   }

   public Object getObjectFromList(int clickedPos) {
      return this.getAddedButtons(this.getPageNumber(), clickedPos).getObject();
   }

   @Nonnull
   public List<Integer> getFillSpace() {
      return (List)(this.fillSpace != null ? this.fillSpace : new ArrayList());
   }

   @Nullable
   public List<?> getListOfFillItems() {
      return this.listOfFillItems;
   }

   public int getInventorySize() {
      return this.inventorySize;
   }

   public boolean isIgnoreValidCheck() {
      return this.ignoreValidCheck;
   }

   public boolean isAutoClearCache() {
      return this.autoClearCache;
   }

   public void menuClose(InventoryCloseEvent event, MenuUtility menu) {
   }

   public int getSlot(int slot) {
      return this.getPageNumber() * this.getInventorySize() + slot;
   }

   public String getTitle() {
      String title = this.title;
      if (this.function != null) {
         title = (String)this.function.apply();
         if (title == null) {
            title = this.title;
         }
      }

      return title;
   }

   protected void putAddedButtonsCache(Integer pageNumber, MenuDataUtility menuDataUtility) {
      this.pagesOfButtonsData.put(pageNumber, menuDataUtility);
   }

   protected void putTimeWhenUpdatesButtons(MenuButton menuButton, Long time) {
      this.getTimeWhenUpdatesButtons().put(menuButton.getId(), time);
   }

   protected void changePage(boolean nextPage) {
      int pageNumber = this.pageNumber;
      if (nextPage) {
         ++pageNumber;
      } else {
         --pageNumber;
      }

      if (pageNumber < 0) {
         pageNumber = this.getRequiredPages() - 1;
      } else if (pageNumber >= this.getRequiredPages()) {
         pageNumber = 0;
      }

      if (pageNumber == -1) {
         pageNumber = 0;
      }

      this.pageNumber = pageNumber;
      this.updateButtons();
      this.updateTittle();
   }

   protected void updateButtons() {
      this.slotIndex = this.getPageNumber() * this.numberOfFillitems;
      this.addItemsToCache(this.getPageNumber());
      this.slotIndex = 0;
      this.reddrawInventory();
      this.updateTimeButtons();
   }

   protected void updateTimeButtons() {
      boolean cancelTask = false;
      if (this.taskid > 0 && (Bukkit.getScheduler().isCurrentlyRunning(this.taskid) || Bukkit.getScheduler().isQueued(this.taskid))) {
         Bukkit.getScheduler().cancelTask(this.taskid);
         cancelTask = true;
      }

      if (cancelTask) {
         this.updateButtonsInList();
         this.getTimeWhenUpdatesButtons().clear();
      }

   }

   protected void updateTittle() {
      String title = this.getTitle();
      if (title == null || title.equals("")) {
         this.title = "Menu" + (this.getRequiredPages() > 1 ? " page: " : "");
         title = this.getTitle();
      }

      UpdateTittleContainers.update(this.player, title + (this.getRequiredPages() > 1 && this.isAutoTitleCurrentPage() ? " " + (this.getPageNumber() + 1) + "" : ""));
   }

   private Object toMenuCache(Player player, Location location) {
      Object obj = null;
      if (player != null && location != null) {
         obj = location;
      }

      if (player != null && location == null) {
         obj = player;
      }

      return obj;
   }

   private void saveMenuCache(@Nonnull Location location) {
      this.menuCache.addToCache(location, this.uniqueKey, this);
   }

   private MenuUtility getMenuCache() {
      return this.menuCache.getMenuInCache(this.menuCacheKey);
   }

   public void removeMenuCache() {
      this.menuCache.removeMenuCached(this.menuCacheKey);
   }

   public void setUniqueKeyMenuCache(String uniqueKey) {
      this.uniqueKey = uniqueKey;
   }

   private boolean checkLastOpenMenu() {
      if (this.getPreviousMenuholder(this.player) != null) {
         if (Metadata.hasPlayerMetadata(this.player, MenuMetadataKey.MENU_OPEN_PREVIOUS)) {
            Metadata.removePlayerMenuMetadata(this.player, MenuMetadataKey.MENU_OPEN_PREVIOUS);
         }

         return false;
      } else {
         return true;
      }
   }

   protected void setLocationMetaOnPlayer(Player player, Location location) {
      String uniqueKey = this.uniqueKey;
      if (uniqueKey != null && uniqueKey.isEmpty()) {
         this.uniqueKey = this.getClass().getName();
         uniqueKey = this.uniqueKey;
      }

      this.menuCacheKey = this.menuCache.getMenuCacheKey(location, uniqueKey);
      if (this.menuCacheKey == null) {
         this.menuCacheKey = new MenuCacheKey(location, uniqueKey);
      }

      Metadata.setPlayerLocationMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION, this.menuCacheKey);
   }

   protected void setMetadataKey(String setPlayerMetadataKey) {
      this.playermetadataKey = setPlayerMetadataKey;
   }

   protected void onMenuOpenPlaySound() {
      Sound sound = this.menuOpenSound;
      if (sound != null) {
         this.player.playSound(this.player.getLocation(), sound, 1.0F, 1.0F);
      }
   }

   /** @deprecated */
   @Deprecated
   protected void onMenuClose(InventoryCloseEvent event) {
      if (Bukkit.getScheduler().isCurrentlyRunning(this.taskid) || Bukkit.getScheduler().isQueued(this.taskid)) {
         Bukkit.getScheduler().cancelTask(this.taskid);
      }

      if (Bukkit.getScheduler().isCurrentlyRunning(this.taskidAnimateTitle) || Bukkit.getScheduler().isQueued(this.taskidAnimateTitle)) {
         Bukkit.getScheduler().cancelTask(this.taskidAnimateTitle);
      }

   }

   protected Inventory loadInventory(Player player, boolean loadToCahe) {
      Inventory menu = null;
      MenuUtility menuCached;
      if (loadToCahe && this.location != null) {
         menuCached = this.getMenuCache();
         if (menuCached == null || menuCached.getMenu() == null) {
            this.saveMenuCache(this.location);
            menuCached = this.getMenuCache();
         }

         if (this.isIgnoreValidCheck()) {
            this.saveMenuCache(this.location);
            menuCached = this.getMenuCache();
         } else {
            Validate.checkBoolean(!menuCached.getClass().equals(this.getClass()) && (this.uniqueKey == null || this.uniqueKey.isEmpty()), "You need set uniqueKey for this menu " + menuCached.getClass() + " or it will replace the old menu and players left can take items, set method setIgnoreValidCheck() to ignore this or set the uniqueKey");
         }

         menu = menuCached.getMenu();
      } else {
         Metadata.setPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN_PREVIOUS, this);
         Metadata.setPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN, this);
         menuCached = Metadata.getPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN);
         if (menuCached != null) {
            menu = menuCached.getMenu();
         }
      }

      return menu;
   }

   private double amountOfpages() {
      List<?> fillItems = this.getListOfFillItems();
      List<Integer> fillSpace = this.getFillSpace();
      if (this.itemsPerPage > 0) {
         if (this.itemsPerPage > this.inventorySize) {
            this.plugin.getLogger().log(Level.SEVERE, "Items per page are biger an Inventory size, items items per page " + this.itemsPerPage + ". Inventory size " + this.inventorySize, (new Throwable()).fillInStackTrace());
         }

         if (!fillSpace.isEmpty()) {
            return (double)fillSpace.size() / (double)this.itemsPerPage;
         } else {
            return fillItems != null && !fillItems.isEmpty() ? (double)fillItems.size() / (double)this.itemsPerPage : (double)this.pagesOfButtonsData.size() / (double)this.itemsPerPage;
         }
      } else {
         return fillItems != null && !fillItems.isEmpty() ? (double)fillItems.size() / (double)(fillSpace.isEmpty() ? this.inventorySize - 9 : fillSpace.size()) : (double)this.pagesOfButtonsData.size() / (double)this.inventorySize;
      }
   }

   protected Map<Integer, ButtonData> addItemsToCache(int pageNumber) {
      MenuDataUtility menuDataUtility = this.cacheMenuData(pageNumber);
      if (!this.shallCacheItems) {
         this.putAddedButtonsCache(pageNumber, menuDataUtility);
      }

      return menuDataUtility.getButtons();
   }

   protected Map<Integer, ButtonData> addItemsToCache() {
      Map<Integer, ButtonData> addedButtons = new HashMap();
      this.requiredPages = Math.max((int)Math.ceil(this.amountOfpages()), 1);
      if (this.manuallySetPages > 0) {
         this.requiredPages = this.manuallySetPages;
      }

      for(int i = 0; i < this.requiredPages; ++i) {
         addedButtons = this.addItemsToCache(i);
         if (i == 0) {
            this.numberOfFillitems = this.slotIndex;
         }
      }

      this.slotIndex = 0;
      return (Map)addedButtons;
   }

   @Nonnull
   private MenuDataUtility cacheMenuData(int pageNumber) {
      MenuDataUtility menuDataUtility = MenuDataUtility.of();

      for(int slot = 0; slot < this.inventorySize; ++slot) {
         Object objectFromlistOfFillItems = "";
         int slotIndexOld = this.slotIndex;
         boolean isFillButton = false;
         if (!this.getFillSpace().isEmpty() && this.getFillSpace().contains(slot)) {
            objectFromlistOfFillItems = this.getObjectFromlistOfFillItems(slotIndexOld);
            ++this.slotIndex;
            isFillButton = true;
         }

         MenuButton menuButton = this.getMenuButtonAtSlot(slot, slotIndexOld, objectFromlistOfFillItems);
         ItemStack result = this.getItemAtSlot(menuButton, slot, slotIndexOld, objectFromlistOfFillItems);
         if (menuButton != null) {
            boolean shallAddMenuButton = isFillButton && this.getListOfFillItems() != null && !this.getListOfFillItems().isEmpty();
            if (menuButton.shouldUpdateButtons()) {
               this.buttonsToUpdate.add(menuButton);
            }

            ButtonData buttonData = new ButtonData(result, shallAddMenuButton ? null : menuButton, objectFromlistOfFillItems);
            menuDataUtility.putButton(pageNumber * this.getInventorySize() + slot, buttonData, shallAddMenuButton ? menuButton : null);
         }
      }

      return menuDataUtility;
   }

   private MenuButton getMenuButtonAtSlot(int slot, int oldSlotIndex, Object objectFromlistOfFillItems) {
      MenuButton result;
      if (!this.getFillSpace().isEmpty() && this.getFillSpace().contains(slot)) {
         if (objectFromlistOfFillItems != null && !objectFromlistOfFillItems.equals("")) {
            result = this.getFillButtonAt(objectFromlistOfFillItems);
         } else {
            result = this.getFillButtonAt(oldSlotIndex);
         }
      } else {
         result = this.getButtonAt(slot);
      }

      return result;
   }

   private ItemStack getItemAtSlot(MenuButton menuButton, int slot, int oldSlotIndex, Object objectFromlistOfFillItems) {
      if (menuButton == null) {
         return null;
      } else {
         ItemStack result = null;
         if (!this.getFillSpace().isEmpty() && this.getFillSpace().contains(slot)) {
            if (objectFromlistOfFillItems != null && !objectFromlistOfFillItems.equals("")) {
               result = menuButton.getItem(objectFromlistOfFillItems);
               if (result == null) {
                  result = menuButton.getItem(oldSlotIndex, objectFromlistOfFillItems);
               }
            } else {
               result = menuButton.getItem(oldSlotIndex, objectFromlistOfFillItems);
            }

            if (result == null) {
               result = menuButton.getItem();
            }
         } else {
            result = menuButton.getItem();
            if (result == null) {
               result = menuButton.getItem(oldSlotIndex, objectFromlistOfFillItems);
            }
         }

         return result;
      }
   }

   private Object getObjectFromlistOfFillItems(int slotIndex) {
      List<?> fillItems = this.getListOfFillItems();
      return fillItems != null && fillItems.size() > slotIndex ? fillItems.get(slotIndex) : null;
   }

   protected void reddrawInventory() {
      if (this.getMenu() == null || this.inventorySize > this.getMenu().getSize()) {
         this.inventory = this.createInventory();
      }

      int fillSpace = !this.getFillSpace().isEmpty() ? this.getFillSpace().size() : this.getMenu().getSize();

      for(int i = (Integer)this.getFillSpace().stream().findFirst().orElse(0); i < fillSpace; ++i) {
         this.getMenu().setItem(i, new ItemStack(Material.AIR));
      }

      Map<Integer, ButtonData> entity = this.getMenuButtons(this.getPageNumber());
      if (entity != null && !entity.isEmpty()) {
         for(int i = 0; i < this.getMenu().getSize(); ++i) {
            ButtonData buttonData = (ButtonData)entity.get(this.getPageNumber() * this.inventorySize + i);
            ItemStack itemStack;
            if (buttonData == null) {
               itemStack = CreateItemStack.createItemStackAsOne((Material)null);
            } else {
               itemStack = buttonData.getItemStack();
            }

            this.getMenu().setItem(i, itemStack);
         }
      }

   }

   private Inventory createInventory() {
      String title = this.getTitle();
      if (this.getInventoryType() != null) {
         return Bukkit.createInventory((InventoryHolder)null, this.getInventoryType(), title != null ? title : "");
      } else {
         if (this.inventorySize != 5 && this.inventorySize % 9 != 0) {
            this.plugin.getLogger().log(Level.WARNING, "wrong inverntory size , you has put in " + this.inventorySize + " it need to be valid number.");
         }

         return this.inventorySize == 5 ? Bukkit.createInventory((InventoryHolder)null, InventoryType.HOPPER, title != null ? title : "") : Bukkit.createInventory((InventoryHolder)null, this.inventorySize % 9 == 0 ? this.inventorySize : 9, title != null ? title : "");
      }
   }

   private long getupdateTime(MenuButton menuButton) {
      return menuButton.setUpdateTime() == -1L ? (long)this.getUpdateTime() : menuButton.setUpdateTime();
   }

   protected void updateButtonsInList() {
      this.taskid = (new BukkitRunnable() {
         private int counter = 0;

         public void run() {
            Iterator var1 = MenuUtility.this.getButtonsToUpdate().iterator();

            while(true) {
               while(true) {
                  MenuButton menuButton;
                  Long timeleft;
                  do {
                     while(true) {
                        do {
                           if (!var1.hasNext()) {
                              ++this.counter;
                              return;
                           }

                           menuButton = (MenuButton)var1.next();
                           timeleft = MenuUtility.this.getTimeWhenUpdatesButton(menuButton);
                        } while(timeleft != null && timeleft == -1L);

                        if (timeleft != null && timeleft != 0L) {
                           break;
                        }

                        MenuUtility.this.putTimeWhenUpdatesButtons(menuButton, (long)this.counter + MenuUtility.this.getupdateTime(menuButton));
                     }
                  } while((long)this.counter < timeleft);

                  MenuUtility.this.getMenuData(MenuUtility.this.getPageNumber());
                  MenuDataUtility menuDataUtility = MenuUtility.this.getMenuData(MenuUtility.this.getPageNumber());
                  if (menuDataUtility == null) {
                     this.cancel();
                     return;
                  }

                  Set<Integer> itemSlots = MenuUtility.this.getItemSlotsMap(menuDataUtility, menuButton);
                  if (itemSlots.isEmpty()) {
                     MenuUtility.this.putTimeWhenUpdatesButtons(menuButton, (long)this.counter + MenuUtility.this.getupdateTime(menuButton));
                  } else {
                     Iterator slotList = itemSlots.iterator();

                     while(slotList.hasNext()) {
                        Integer slot = (Integer)slotList.next();
                        ButtonData buttonData = menuDataUtility.getButton(MenuUtility.this.getSlot(slot));
                        if (buttonData != null) {
                           ItemStack menuItem = MenuUtility.this.getMenuItem(menuButton, buttonData, slot);
                           ButtonData newButtonData = new ButtonData(menuItem, buttonData.getMenuButton(), buttonData.getObject());
                           menuDataUtility.putButton(MenuUtility.this.getSlot(slot), newButtonData, menuDataUtility.getFillMenuButton(MenuUtility.this.getSlot(slot)));
                           MenuUtility.this.putAddedButtonsCache(MenuUtility.this.getPageNumber(), menuDataUtility);
                           MenuUtility.this.getMenu().setItem(slot, menuItem);
                           slotList.remove();
                        }
                     }

                     MenuUtility.this.putTimeWhenUpdatesButtons(menuButton, (long)this.counter + MenuUtility.this.getupdateTime(menuButton));
                  }
               }
            }
         }
      }).runTaskTimer(this.plugin, 1L, 20L).getTaskId();
   }

   @Nullable
   private ItemStack getMenuItem(MenuButton menuButton, ButtonData cachedButtons, int slot) {
      return this.getMenuItem(menuButton, cachedButtons, slot, menuButton.shouldUpdateButtons());
   }

   @Nullable
   protected ItemStack getMenuItem(MenuButton menuButton, ButtonData cachedButtons, int slot, boolean updateButton) {
      if (menuButton == null) {
         return null;
      } else if (updateButton) {
         ItemStack itemStack = menuButton.getItem();
         if (itemStack != null) {
            return itemStack;
         } else {
            itemStack = menuButton.getItem(cachedButtons.getObject());
            if (itemStack != null) {
               return itemStack;
            } else {
               itemStack = menuButton.getItem(this.getSlot(slot), cachedButtons.getObject());
               return itemStack;
            }
         }
      } else {
         return null;
      }
   }

   @Nonnull
   private Set<Integer> getItemSlotsMap(MenuDataUtility menuDataMap, MenuButton menuButton) {
      Set<Integer> slotList = new HashSet();
      if (menuDataMap == null) {
         return slotList;
      } else {
         for(int slot = 0; slot < this.inventorySize; ++slot) {
            ButtonData addedButtons = (ButtonData)menuDataMap.getButtons().get(this.getSlot(slot));
            if (addedButtons != null) {
               MenuButton chacheMenuButton = addedButtons.getMenuButton();
               MenuButton fillMenuButton = menuDataMap.getFillMenuButton(this.getSlot(slot));
               int menuButtonId = menuButton.getId();
               if (chacheMenuButton == null && fillMenuButton != null && fillMenuButton.getId() == menuButtonId || chacheMenuButton != null && Objects.equals(menuButtonId, chacheMenuButton.getId())) {
                  slotList.add(slot);
               }
            }
         }

         return slotList;
      }
   }

   protected void animateTitle() {
      final PairFunction<String> task = this.animateTitle;
      if (task != null) {
         this.taskidAnimateTitle = (new BukkitRunnable() {
            public void run() {
               Pair<String, Boolean> apply = task.apply();
               String text = (String)apply.getFirst();
               if (text != null && (Boolean)apply.getSecond()) {
                  if (!text.isEmpty()) {
                     UpdateTittleContainers.update(MenuUtility.this.player, text);
                  }

               } else {
                  this.cancel();
                  UpdateTittleContainers.update(MenuUtility.this.player, MenuUtility.this.getTitle());
               }
            }
         }).runTaskTimerAsynchronously(this.plugin, 1L, (long)(20 + this.animateTitleTime)).getTaskId();
      }
   }
}
