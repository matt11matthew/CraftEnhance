package com.dutchjelly.craftenhance.gui;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.gui.guis.GUIElement;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import com.dutchjelly.craftenhance.util.Pair;
import craftenhance.libs.menulib.MenuHolder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class GuiManager implements Listener {
   private static final int MaxPreviousPageBuffer = 20;
   private final Map<UUID, Pair<GUIElement, com.dutchjelly.craftenhance.gui.interfaces.IChatInputHandler>> chatWaiting = new HashMap();
   private final Map<UUID, Pair<MenuHolder, com.dutchjelly.craftenhance.gui.interfaces.IChatInputHandler>> chatWaitingCopy = new HashMap();
   private final CraftEnhance main;

   public GuiManager(CraftEnhance main) {
      this.main = main;
   }

   public CraftEnhance getMain() {
      return this.main;
   }

   @EventHandler
   public void onDrag(InventoryDragEvent e) {
      if (e.getView().getTopInventory().getHolder() instanceof GUIElement) {
         GUIElement openGUI = (GUIElement)e.getView().getTopInventory().getHolder();
         if (openGUI != null && e.getInventory() != null) {
            try {
               openGUI.handleDragging(e);
               if (!openGUI.isCancelResponsible() && !e.isCancelled()) {
                  e.setCancelled(true);
               }
            } catch (Exception var4) {
               var4.printStackTrace();
               if (!e.isCancelled()) {
                  e.setCancelled(true);
               }
            }

         }
      }
   }

   @EventHandler
   public void onClick(InventoryClickEvent clickEvent) {
      if (clickEvent.getView().getTopInventory().getHolder() instanceof GUIElement) {
         GUIElement openGUI = (GUIElement)clickEvent.getView().getTopInventory().getHolder();
         if (openGUI != null) {
            try {
               if (clickEvent.getClickedInventory() != null && clickEvent.getClickedInventory().equals(openGUI.getInventory())) {
                  openGUI.handleEvent(clickEvent);
               } else {
                  openGUI.handleOutsideClick(clickEvent);
               }

               if (!openGUI.isCancelResponsible() && !clickEvent.isCancelled()) {
                  clickEvent.setCancelled(true);
               }
            } catch (Exception var4) {
               var4.printStackTrace();
               if (!clickEvent.isCancelled()) {
                  clickEvent.setCancelled(true);
               }
            }

         }
      }
   }

   @EventHandler
   public void onChat(AsyncPlayerChatEvent e) {
      if (e.getPlayer() != null) {
         ;
      }
   }

   @EventHandler
   public void onChatold(AsyncPlayerChatEvent e) {
      if (e.getPlayer() != null) {
         ;
      }
   }

   public boolean chatWaiting(AsyncPlayerChatEvent e) {
      UUID id = e.getPlayer().getUniqueId();
      if (!this.chatWaitingCopy.containsKey(id)) {
         return false;
      } else {
         Bukkit.getScheduler().runTask(this.getMain(), () -> {
            com.dutchjelly.craftenhance.gui.interfaces.IChatInputHandler callback = (com.dutchjelly.craftenhance.gui.interfaces.IChatInputHandler)((Pair)this.chatWaitingCopy.get(id)).getSecond();
            if (!callback.handle(e.getMessage())) {
               MenuHolder gui = (MenuHolder)((Pair)this.chatWaitingCopy.get(id)).getFirst();
               this.chatWaitingCopy.remove(id);
            }
         });
         return true;
      }
   }

   public boolean chatWaitingOld(AsyncPlayerChatEvent e) {
      UUID id = e.getPlayer().getUniqueId();
      if (!this.chatWaiting.containsKey(id)) {
         return false;
      } else {
         Bukkit.getScheduler().runTask(this.getMain(), () -> {
            com.dutchjelly.craftenhance.gui.interfaces.IChatInputHandler callback = (com.dutchjelly.craftenhance.gui.interfaces.IChatInputHandler)((Pair)this.chatWaiting.get(id)).getSecond();
            if (!callback.handle(e.getMessage())) {
               GUIElement gui = (GUIElement)((Pair)this.chatWaiting.get(id)).getFirst();
               if (gui != null) {
                  this.openGUI(e.getPlayer(), gui);
               }

               this.chatWaiting.remove(id);
            }
         });
         return true;
      }
   }

   public void openGUI(Player p, GUIElement gui) {
      if (this.countPreviousPages(gui) >= 20) {
         Messenger.Message("For performance reasons you cannot open more gui's in that chain (the server keeps track of the previous gui's so you can go back).", p);
      } else if (gui == null) {
         Debug.Send((Object)"trying to open null gui...");
      } else {
         Debug.Send((Object)("Opening a gui element: " + gui.getClass().getName()));
         p.openInventory(gui.getInventory());
      }
   }

   private int countPreviousPages(GUIElement gui) {
      if (gui == null) {
         return 0;
      } else {
         int counter;
         for(counter = 0; gui != null; ++counter) {
            gui = gui.getPreviousGui();
         }

         return counter;
      }
   }

   public void waitForChatInput(GUIElement gui, Player p, com.dutchjelly.craftenhance.gui.interfaces.IChatInputHandler callback) {
      UUID playerId = p.getUniqueId();
      p.closeInventory();
      this.chatWaiting.put(playerId, new Pair(gui, callback));
   }

   public void waitForChatInput(MenuHolder gui, Player p, com.dutchjelly.craftenhance.gui.interfaces.IChatInputHandler callback) {
      UUID playerId = p.getUniqueId();
      p.closeInventory();
      this.chatWaitingCopy.put(playerId, new Pair(gui, callback));
   }
}
