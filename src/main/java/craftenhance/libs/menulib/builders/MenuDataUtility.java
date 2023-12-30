package craftenhance.libs.menulib.builders;

import craftenhance.libs.menulib.MenuButton;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MenuDataUtility {
   private final Map<Integer, ButtonData> buttons = new HashMap();
   private Map<Integer, MenuButton> fillMenuButtons;
   private MenuButton fillMenuButton;

   public static MenuDataUtility of() {
      return new MenuDataUtility();
   }

   public MenuDataUtility putButton(int slot, @Nonnull ButtonData buttonData) {
      return this.putButton(slot, buttonData, this.getFillMenuButton());
   }

   public MenuDataUtility putButton(int slot, @Nonnull ButtonData buttonData, @Nullable MenuButton fillMenuButton) {
      this.buttons.put(slot, buttonData);
      if (fillMenuButton != null) {
         if (this.getFillMenuButton() == null || this.getFillMenuButton().getId() == fillMenuButton.getId()) {
            return this.setFillMenuButton(fillMenuButton);
         }

         if (this.fillMenuButtons == null) {
            this.fillMenuButtons = new HashMap();
         }

         this.fillMenuButtons.put(slot, fillMenuButton);
      }

      return this;
   }

   public MenuDataUtility setFillMenuButton(MenuButton fillMenuButton) {
      this.fillMenuButton = fillMenuButton;
      return this;
   }

   @Nullable
   public MenuButton getSimilarFillMenuButton(@Nullable MenuButton button) {
      MenuButton menuButton = this.fillMenuButton;
      if (menuButton != null && button != null) {
         return menuButton.getId() != button.getId() ? null : menuButton;
      } else {
         return null;
      }
   }

   @Nullable
   public MenuButton getFillMenuButton(@Nonnull MenuButton menuButton) {
      if (this.getFillMenuButton() != null && this.getFillMenuButton().getId() == menuButton.getId()) {
         return this.getFillMenuButton();
      } else {
         if (this.fillMenuButtons != null) {
            Iterator var2 = this.getFillMenuButtons().values().iterator();

            while(var2.hasNext()) {
               MenuButton button = (MenuButton)var2.next();
               if (button.getId() == menuButton.getId()) {
                  return button;
               }
            }
         }

         return null;
      }
   }

   @Nullable
   public MenuButton getFillMenuButton(int slot) {
      MenuButton menuButton = null;
      if (this.fillMenuButtons != null) {
         menuButton = (MenuButton)this.fillMenuButtons.get(slot);
      }

      if (menuButton == null) {
         menuButton = this.getFillMenuButton();
      }

      return menuButton;
   }

   @Nullable
   private MenuButton getFillMenuButton() {
      return this.fillMenuButton;
   }

   @Nullable
   public ButtonData getButton(int slot) {
      return (ButtonData)this.buttons.get(slot);
   }

   public Map<Integer, ButtonData> getButtons() {
      return Collections.unmodifiableMap(this.buttons);
   }

   public Map<Integer, MenuButton> getFillMenuButtons() {
      return (Map)(this.fillMenuButtons == null ? new HashMap() : Collections.unmodifiableMap(this.fillMenuButtons));
   }

   @Nullable
   public MenuButton getMenuButton(int slot) {
      ButtonData buttonData = this.getButton(slot);
      MenuButton menuButton = null;
      if (buttonData != null) {
         menuButton = buttonData.getMenuButton();
         if (menuButton == null) {
            menuButton = this.getFillMenuButton(slot);
         }
      }

      return menuButton;
   }

   public String toString() {
      return "MenuDataUtility{buttons=" + this.buttons + ", fillMenuButton=" + this.fillMenuButton + ", fillMenuButtons=" + this.fillMenuButtons + '}';
   }
}
