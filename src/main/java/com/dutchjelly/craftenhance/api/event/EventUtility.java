package com.dutchjelly.craftenhance.api.event;

import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class EventUtility extends Event implements Cancellable {
   private final HandlerList handler;

   public EventUtility(HandlerList handler) {
      this(handler, false);
   }

   public EventUtility(HandlerList handler, boolean isAsync) {
      super(isAsync);
      this.handler = handler;
   }

   public void registerEvent() {
      Bukkit.getPluginManager().callEvent(this);
   }

   public boolean isCancelled() {
      return false;
   }

   public void setCancelled(boolean cancel) {
      Messenger.Error("You can't cancel this event.");
   }

   public HandlerList getHandlers() {
      return this.handler;
   }
}
