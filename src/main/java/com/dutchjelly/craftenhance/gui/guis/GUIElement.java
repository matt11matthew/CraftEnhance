package com.dutchjelly.craftenhance.gui.guis;

import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.interfaces.IButtonHandler;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public abstract class GUIElement implements InventoryHolder {
   private final GuiManager manager;
   @NonNull
   private final GuiTemplate template;
   private final Player player;
   private final GUIElement previousGui;
   private Map<ButtonType, List<IButtonHandler>> buttonClickHandlers;

   public GUIElement(GuiManager manager, GuiTemplate template, GUIElement previousGui, Player player) {
      this.manager = manager;
      this.template = template;
      this.player = player;
      this.previousGui = previousGui;
      this.buttonClickHandlers = new HashMap();
      this.buttonClickHandlers.put(ButtonType.Back, Arrays.asList(this::handleBackBtnClicked));
   }

   public GUIElement(GuiManager manager, GUIElement previousGui, Player player) {
      this.manager = manager;
      this.template = manager.getMain().getGuiTemplatesFile().getTemplate(this.getClass());
      this.player = player;
      this.previousGui = previousGui;
      this.buttonClickHandlers = new HashMap();
      this.buttonClickHandlers.put(ButtonType.Back, Arrays.asList(this::handleBackBtnClicked));
   }

   public void handleBackBtnClicked(ClickType click, ItemStack btn, ButtonType btnType) {
      if (this.previousGui != null) {
         this.manager.openGUI(this.player, this.previousGui);
      }
   }

   public void handleEvent(InventoryClickEvent e) {
      if (e.getWhoClicked() instanceof Player) {
         if (!e.getWhoClicked().equals(this.getPlayer())) {
            throw new IllegalStateException("Other player clicked than owner of GUI.");
         } else {
            int clickedSlot = e.getSlot();
            ButtonType clickedButton = (ButtonType)this.getTemplate().getButtonMapping().get(clickedSlot);
            if (clickedButton != null) {
               List<IButtonHandler> btnHandlers = (List)this.buttonClickHandlers.get(clickedButton);
               if (btnHandlers != null) {
                  btnHandlers.forEach((x) -> {
                     x.handleClick(e.getClick(), e.getCurrentItem(), clickedButton);
                  });
               }
            }

            this.handleEventRest(e);
         }
      }
   }

   public void handleOutsideClick(InventoryClickEvent e) {
      if (e.getWhoClicked() instanceof Player) {
         ;
      }
   }

   public void handleDragging(InventoryDragEvent e) {
   }

   public void addBtnListener(ButtonType type, IButtonHandler listener) {
      if (this.buttonClickHandlers.containsKey(type)) {
         ((List)this.buttonClickHandlers.get(type)).add(listener);
      } else {
         this.buttonClickHandlers.put(type, Arrays.asList(listener));
      }

   }

   public abstract void handleEventRest(InventoryClickEvent var1);

   public abstract boolean isCancelResponsible();

   public GuiManager getManager() {
      return this.manager;
   }

   @NonNull
   public GuiTemplate getTemplate() {
      return this.template;
   }

   public Player getPlayer() {
      return this.player;
   }

   public GUIElement getPreviousGui() {
      return this.previousGui;
   }
}
