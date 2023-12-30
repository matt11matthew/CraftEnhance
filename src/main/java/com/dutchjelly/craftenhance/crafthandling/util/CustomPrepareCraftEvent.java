package com.dutchjelly.craftenhance.crafthandling.util;

import com.dutchjelly.craftenhance.gui.customcrafting.CustomCraftingTable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomPrepareCraftEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private CustomCraftingTable table;

   public CustomPrepareCraftEvent(CustomCraftingTable table) {
      this.table = table;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public CustomCraftingTable getTable() {
      return this.table;
   }
}
