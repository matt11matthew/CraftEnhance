package craftenhance.libs.menulib;

import craftenhance.libs.RegisterNbtAPI;
import craftenhance.libs.menulib.builders.ButtonData;
import craftenhance.libs.menulib.builders.MenuDataUtility;
import craftenhance.libs.menulib.utility.Metadata;
import craftenhance.libs.menulib.utility.ServerVersion;
import craftenhance.libs.menulib.utility.Item.CreateItemStack;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
;

public class RegisterMenuAPI {
   private static Plugin PLUGIN;
   private static RegisterNbtAPI nbtApi;

   private RegisterMenuAPI() {
      throw new UnsupportedOperationException("You need specify your main class");
   }

   public RegisterMenuAPI(Plugin plugin) {
      PLUGIN = plugin;
      if (PLUGIN == null) {
         Bukkit.getServer().getLogger().log(Level.WARNING, "You have not set plugin, becuse plugin is null");
      } else {
         ServerVersion.setServerVersion(plugin);
         this.versionCheck();
         this.registerMenuEvent(plugin);
         nbtApi = new RegisterNbtAPI(plugin, false);
      }
   }

   private void versionCheck() {
      PLUGIN.getLogger().log(Level.INFO, "Now starting MenuApi. Any errors will be shown below.");
      if (ServerVersion.newerThan(ServerVersion.v1_19_4)) {
         PLUGIN.getLogger().log(Level.WARNING, "It is not tested on versions beyond 1.19.4");
      }

   }

   public static void getLogger(Level level, String messsage) {
      PLUGIN.getLogger().log(level, messsage);
   }

   public static Plugin getPLUGIN() {
      return PLUGIN;
   }

   private void registerMenuEvent(Plugin plugin) {
      RegisterMenuAPI.MenuHolderListener menuHolderListener = new RegisterMenuAPI.MenuHolderListener();
      Bukkit.getPluginManager().registerEvents(menuHolderListener, plugin);
   }

   public static RegisterNbtAPI getNbtApi() {
      return nbtApi;
   }

   private static class MenuHolderListener implements Listener {
      private final MenuCache menuCache;
      private final Map<UUID, RegisterMenuAPI.MenuHolderListener.SwapData> cacheData;

      private MenuHolderListener() {
         this.menuCache = MenuCache.getInstance();
         this.cacheData = new HashMap();
      }

      @EventHandler(
         priority = EventPriority.LOW
      )
      public void onMenuClicking(InventoryClickEvent event) {
         Player player = (Player)event.getWhoClicked();
         if (event.getClickedInventory() != null) {
            ItemStack clickedItem = event.getCurrentItem();
            ItemStack cursor = event.getCursor();
            MenuUtility menuUtility = this.getMenuHolder(player);
            if (menuUtility != null) {
               if (event.getView().getTopInventory().equals(menuUtility.getMenu())) {
                  if (!menuUtility.isAddedButtonsCacheEmpty()) {
                     int clickedSlot = event.getSlot();
                     int clickedPos = menuUtility.getSlot(clickedSlot);
                     if (!menuUtility.isAllowShiftClick() && event.getClick().isShiftClick()) {
                        event.setCancelled(true);
                        return;
                     }

                     if (menuUtility.isSlotsYouCanAddItems()) {
                        if (menuUtility.getFillSpace().contains(clickedPos)) {
                           return;
                        }

                        if (event.getClickedInventory().getType() != InventoryType.PLAYER) {
                           event.setCancelled(true);
                        }
                     } else {
                        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
                           if (event.getClick().isShiftClick()) {
                              event.setCancelled(true);
                           } else {
                              event.setCancelled(true);
                           }
                        }

                        if (cursor != null && cursor.getType() != Material.AIR) {
                           event.setCancelled(true);
                        }
                     }

                     MenuButton menuButton = this.getClickedButton(menuUtility, clickedItem, clickedPos);
                     if (menuButton != null) {
                        event.setCancelled(true);
                        Object object = menuUtility.getObjectFromList(clickedPos);
                        Object objectData = object != null && !object.equals("") ? object : clickedItem;
                        if (clickedItem == null) {
                           clickedItem = CreateItemStack.of("AIR").makeItemStack();
                        }

                        menuButton.onClickInsideMenu(player, menuUtility.getMenu(), event.getClick(), clickedItem, objectData);
                        if (ServerVersion.newerThan(ServerVersion.v1_15) && event.getClick() == ClickType.SWAP_OFFHAND) {
                           RegisterMenuAPI.MenuHolderListener.SwapData data = (RegisterMenuAPI.MenuHolderListener.SwapData)this.cacheData.get(player.getUniqueId());
                           ItemStack item = null;
                           if (data != null) {
                              item = data.getItemInOfBeforeOpenMenuHand();
                           }

                           this.cacheData.put(player.getUniqueId(), new RegisterMenuAPI.MenuHolderListener.SwapData(true, item));
                        }
                     }
                  }

               }
            }
         }
      }

      @EventHandler(
         priority = EventPriority.LOW
      )
      public void onMenuOpen(InventoryOpenEvent event) {
         Player player = (Player)event.getPlayer();
         MenuUtility menuUtility = this.getMenuHolder(player);
         if (menuUtility != null) {
            if (!ServerVersion.olderThan(ServerVersion.v1_15)) {
               this.cacheData.put(player.getUniqueId(), new RegisterMenuAPI.MenuHolderListener.SwapData(false, player.getInventory().getItemInOffHand()));
            }
         }
      }

      @EventHandler(
         priority = EventPriority.LOW
      )
      public void onMenuClose(InventoryCloseEvent event) {
         Player player = (Player)event.getPlayer();
         MenuUtility menuUtility = this.getMenuHolder(player);
         if (menuUtility != null) {
            RegisterMenuAPI.MenuHolderListener.SwapData data = (RegisterMenuAPI.MenuHolderListener.SwapData)this.cacheData.get(player.getUniqueId());
            if (data != null && data.isPlayerUseSwapoffhand()) {
               if (data.getItemInOfBeforeOpenMenuHand() != null && data.getItemInOfBeforeOpenMenuHand().getType() != Material.AIR) {
                  player.getInventory().setItemInOffHand(data.getItemInOfBeforeOpenMenuHand());
               } else {
                  player.getInventory().setItemInOffHand((ItemStack)null);
               }
            }

            this.cacheData.remove(player.getUniqueId());
            if (event.getView().getTopInventory().equals(menuUtility.getMenu())) {
               menuUtility.onMenuClose(event);

               try {
                  menuUtility.menuClose(event, menuUtility);
               } finally {
                  if (Metadata.hasPlayerMetadata(player, MenuMetadataKey.MENU_OPEN)) {
                     Metadata.removePlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN);
                  }

                  if (Metadata.hasPlayerMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION)) {
                     if (menuUtility.isAutoClearCache() && menuUtility.getAmountOfViewers() < 1) {
                        this.menuCache.removeMenuCached(Metadata.getPlayerMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION));
                     }

                     Metadata.removePlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION);
                  }

               }

            }
         }
      }

      @EventHandler(
         priority = EventPriority.LOW
      )
      public void onInventoryDragTop(InventoryDragEvent event) {
         Player player = (Player)event.getWhoClicked();
         if (event.getView().getType() != InventoryType.PLAYER) {
            MenuUtility menuUtility = this.getMenuHolder(player);
            if (menuUtility != null) {
               if (menuUtility.getMenu() != null) {
                  if (!menuUtility.isAddedButtonsCacheEmpty()) {
                     int size = event.getView().getTopInventory().getSize();
                     Iterator var5 = event.getRawSlots().iterator();

                     while(var5.hasNext()) {
                        int clickedSlot = (Integer)var5.next();
                        if (clickedSlot <= size) {
                           int clickedPos = menuUtility.getSlot(clickedSlot);
                           ItemStack cursor = this.checkIfNull(event.getCursor(), event.getOldCursor());
                           if (menuUtility.isSlotsYouCanAddItems()) {
                              if (menuUtility.getFillSpace().contains(clickedSlot)) {
                                 return;
                              }

                              event.setCancelled(true);
                           } else {
                              event.setCancelled(true);
                           }

                           if (this.getClickedButton(menuUtility, cursor, clickedPos) == null) {
                              event.setCancelled(true);
                           }
                        }
                     }
                  }

               }
            }
         }
      }

      public MenuButton getClickedButton(MenuUtility menusData, ItemStack item, int clickedPos) {
         MenuDataUtility menuData = menusData.getMenuData(menusData.getPageNumber());
         if (menuData != null) {
            ButtonData buttonData = menuData.getButton(clickedPos);
            if (buttonData == null) {
               return null;
            }

            MenuButton button = buttonData.getMenuButton();
            if (menusData.isIgnoreItemCheck()) {
               return menuData.getMenuButton(clickedPos);
            }

            if (this.isItemSimilar(buttonData.getItemStack(), item)) {
               return menuData.getMenuButton(clickedPos);
            }
         }

         return null;
      }

      public boolean isItemSimilar(ItemStack item, ItemStack clickedItem) {
         if (item != null && clickedItem != null) {
            return this.itemIsSimilar(item, clickedItem) ? true : item.isSimilar(clickedItem);
         } else {
            return false;
         }
      }

      public boolean itemIsSimilar(ItemStack firstItem, ItemStack secondItemStack) {
         if (firstItem.getType() == secondItemStack.getType()) {
            if (firstItem.hasItemMeta() && firstItem.getItemMeta() != null) {
               ItemMeta itemMeta1 = firstItem.getItemMeta();
               ItemMeta itemMeta2 = secondItemStack.getItemMeta();
               if (!itemMeta1.equals(itemMeta2)) {
                  return false;
               } else {
                  return this.getDurability(firstItem, itemMeta1) == this.getDurability(secondItemStack, itemMeta2);
               }
            } else {
               return true;
            }
         } else {
            return false;
         }
      }

      public short getDurability(ItemStack itemstack, ItemMeta itemMeta) {
         if (ServerVersion.atLeast(ServerVersion.v1_13)) {
            return itemMeta == null ? 0 : (short)((Damageable)itemMeta).getDamage();
         } else {
            return itemstack.getDurability();
         }
      }

      public ItemStack checkIfNull(ItemStack curentCursor, ItemStack oldCursor) {
         return curentCursor != null ? curentCursor : (oldCursor != null ? oldCursor : new ItemStack(Material.AIR));
      }

      
      private MenuUtility getMenuHolder(Player player) {
         Object menukey = null;
         if (Metadata.hasPlayerMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION)) {
            menukey = Metadata.getPlayerMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION);
         }

         MenuUtility menuUtility;
         if (Metadata.hasPlayerMetadata(player, MenuMetadataKey.MENU_OPEN)) {
            menuUtility = Metadata.getPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN);
         } else {
            menuUtility = this.menuCache.getMenuInCache(menukey);
         }

         return menuUtility;
      }

      // $FF: synthetic method
      MenuHolderListener(Object x0) {
         this();
      }

      private static class SwapData {
         boolean playerUseSwapoffhand;
         ItemStack itemInOfBeforeOpenMenuHand;

         public SwapData(boolean playerUseSwapoffhand, ItemStack itemInOfBeforeOpenMenuHand) {
            this.playerUseSwapoffhand = playerUseSwapoffhand;
            this.itemInOfBeforeOpenMenuHand = itemInOfBeforeOpenMenuHand;
         }

         public boolean isPlayerUseSwapoffhand() {
            return this.playerUseSwapoffhand;
         }

         public ItemStack getItemInOfBeforeOpenMenuHand() {
            return this.itemInOfBeforeOpenMenuHand;
         }
      }
   }
}
