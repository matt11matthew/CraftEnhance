package craftenhance.libs.menulib;

import javax.annotation.Nonnull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
;

public abstract class MenuButton {
   private static int counter = 0;
   private final int id;

   public MenuButton() {
      this.id = counter++;
   }

   public abstract void onClickInsideMenu(@Nonnull Player var1, @Nonnull Inventory var2, @Nonnull ClickType var3, @Nonnull ItemStack var4, Object var5);

   public abstract ItemStack getItem();

   public ItemStack getItem(@Nonnull Object object) {
      return null;
   }

   public ItemStack getItem(int slot,  Object object) {
      return null;
   }

   public long setUpdateTime() {
      return -1L;
   }

   public boolean shouldUpdateButtons() {
      return false;
   }

   public int getId() {
      return this.id;
   }
}
