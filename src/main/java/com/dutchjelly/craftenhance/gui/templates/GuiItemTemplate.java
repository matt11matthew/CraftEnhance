package com.dutchjelly.craftenhance.gui.templates;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.exceptions.ConfigError;
import com.dutchjelly.craftenhance.gui.util.SkullCreator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiItemTemplate {
   public static final String GUI_SKULL_MATERIAL_NAME = "GUI_SKULL_ITEM";
   private final ItemStack item;

   public GuiItemTemplate(ConfigurationSection config) {
      if (config == null) {
         this.item = null;
      } else {
         String material = config.getString("material");
         if (material == null) {
            throw new ConfigError("found null material");
         } else {
            String color = config.getString("color");
            if (color == null) {
               Material mat = Adapter.getMaterial(material);
               if (mat == null) {
                  throw new ConfigError("material " + material + " not found");
               }

               this.item = new ItemStack(mat);
            } else if (material.equalsIgnoreCase("GUI_SKULL_ITEM")) {
               if (color.startsWith("uuid")) {
                  this.item = SkullCreator.itemFromUuid(UUID.fromString(color.replaceFirst("uuid", "")));
               } else if (color.startsWith("base64")) {
                  this.item = SkullCreator.itemFromBase64(color.replaceFirst("base64", ""));
               } else {
                  if (!color.startsWith("url")) {
                     throw new ConfigError("specified skull meta is invalid");
                  }

                  this.item = SkullCreator.itemFromUrl(color.replaceFirst("url", ""));
               }
            } else {
               DyeColor dColor = DyeColor.valueOf(color);
               if (dColor == null) {
                  throw new ConfigError("color " + color + " not found");
               }

               this.item = Adapter.getColoredItem(material, dColor);
            }

            List<String> lore = config.getStringList("lore");
            if (lore != null) {
               lore = (List)lore.stream().map((x) -> {
                  return ChatColor.translateAlternateColorCodes('&', x);
               }).collect(Collectors.toList());
            }

            String name = ChatColor.translateAlternateColorCodes('&', config.getString("name"));
            boolean glow = config.getBoolean("glow");
            ItemMeta meta = this.item.getItemMeta();
            meta.setLore((List)(lore == null ? new ArrayList() : lore));
            meta.setDisplayName(name);
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_POTION_EFFECTS});
            if (glow) {
               meta.addEnchant(Enchantment.DURABILITY, 10, true);
            }

            this.item.setItemMeta(meta);
         }
      }
   }

   public ItemStack getItem() {
      return this.item;
   }
}
