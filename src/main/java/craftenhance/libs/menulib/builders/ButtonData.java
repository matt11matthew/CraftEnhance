package craftenhance.libs.menulib.builders;

import craftenhance.libs.menulib.MenuButton;
import org.bukkit.inventory.ItemStack;

public class ButtonData {
   private final ItemStack itemStack;
   private final MenuButton menuButtonLinkedToThisItem;
   private final int id;
   private final Object object;

   public ButtonData(ItemStack itemStack, MenuButton menuButton, Object object) {
      this.itemStack = itemStack;
      this.menuButtonLinkedToThisItem = menuButton;
      this.id = menuButton != null ? menuButton.getId() : 0;
      this.object = object;
   }

   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public MenuButton getMenuButton() {
      return this.menuButtonLinkedToThisItem;
   }

   public int getId() {
      return this.id;
   }

   public Object getObject() {
      return this.object;
   }
}
