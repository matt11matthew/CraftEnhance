package com.dutchjelly.craftenhance.gui.templates;

import com.dutchjelly.craftenhance.util.SoundUtillity;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Sound;

public class MenuTemplate {
   private final String menuTitel;
   private final List<Integer> fillSlots;
   private final Map<List<Integer>, MenuButton> menuButtons;
   private final int amountOfButtons;
   private final Sound sound;

   public MenuTemplate(String menuTitel, List<Integer> fillSlots, Map<List<Integer>, MenuButton> menuButtons, String sound) {
      this.menuTitel = menuTitel;
      this.fillSlots = fillSlots;
      this.menuButtons = menuButtons;
      if (fillSlots != null && !fillSlots.isEmpty()) {
         this.amountOfButtons = this.calculateAmountOfButtons(menuButtons, fillSlots);
      } else {
         this.amountOfButtons = this.calculateAmountOfButtons(menuButtons);
      }

      this.sound = SoundUtillity.getSound(sound);
   }

   public int getAmountOfButtons() {
      return this.amountOfButtons;
   }

   public String getMenuTitel() {
      return this.menuTitel;
   }

   public List<Integer> getFillSlots() {
      return this.fillSlots;
   }

   public Map<List<Integer>, MenuButton> getMenuButtons() {
      return this.menuButtons;
   }

   public Sound getSound() {
      return this.sound;
   }

   public MenuButton getMenuButton(int slot) {
      Iterator var2 = this.menuButtons.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<List<Integer>, MenuButton> slots = (Entry)var2.next();
         Iterator var4 = ((List)slots.getKey()).iterator();

         while(var4.hasNext()) {
            int menuSlot = (Integer)var4.next();
            if (menuSlot == slot) {
               return (MenuButton)slots.getValue();
            }
         }
      }

      return null;
   }

   public int calculateAmountOfButtons(Map<List<Integer>, MenuButton> menuButtons, List<Integer> fillSlots) {
      int lastButton = 0;
      Iterator var4 = menuButtons.keySet().iterator();

      while(var4.hasNext()) {
         List<Integer> slots = (List)var4.next();

         Integer slot;
         for(Iterator var6 = slots.iterator(); var6.hasNext(); lastButton = Math.max(lastButton, slot)) {
            slot = (Integer)var6.next();
         }
      }

      return lastButton;
   }

   public int calculateAmountOfButtons(Map<List<Integer>, MenuButton> menuButtons) {
      int amountOfButtons = 0;

      List slots;
      for(Iterator var3 = menuButtons.keySet().iterator(); var3.hasNext(); amountOfButtons += slots.size()) {
         slots = (List)var3.next();
      }

      return amountOfButtons;
   }
}
