package com.dutchjelly.craftenhance.gui.interfaces;

import com.dutchjelly.craftenhance.gui.util.ButtonType;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public interface IButtonHandler {
   void handleClick(ClickType var1, ItemStack var2, ButtonType var3);
}
