package com.dutchjelly.craftenhance.itemcreation;

import com.dutchjelly.bukkitadapter.Adapter;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemCreator {
   private ItemStack item;
   private String[] args;

   public ItemCreator(ItemStack item, String[] args) {
      this.item = item;
      this.args = args;
   }

   public ItemStack getItem() {
      return this.item;
   }

   public ParseResult setDurability() {
      if (this.item != null && this.item.getItemMeta() != null) {
         if (this.args.length != 1) {
            return ParseResult.NO_ARGS;
         } else {
            int durability = tryParse(this.args[0], -1);
            if (durability >= 0 && durability <= 100) {
               short maxDurability = this.item.getType().getMaxDurability();
               this.item = Adapter.SetDurability(this.item, (int)((double)maxDurability - (double)maxDurability * (double)durability / 100.0D));
               return ParseResult.SUCCESS;
            } else {
               return ParseResult.NO_PERCENT;
            }
         }
      } else {
         return ParseResult.NULL_ITEM;
      }
   }

   public ParseResult setLore() {
      if (this.item != null && this.item.getItemMeta() != null) {
         if (this.args.length < 1) {
            return ParseResult.NO_ARGS;
         } else {
            int lineNumber = tryParse(this.args[0]);
            if (lineNumber == 0) {
               return ParseResult.NO_NUMBER;
            } else {
               String loreLine = ChatColor.translateAlternateColorCodes('&', this.joinRemaining(1));
               ItemMeta meta = this.item.getItemMeta();
               Object lore = meta.hasLore() ? meta.getLore() : new ArrayList();

               while(((List)lore).size() < lineNumber) {
                  ((List)lore).add("");
               }

               ((List)lore).set(lineNumber - 1, loreLine);
               meta.setLore((List)lore);
               this.item.setItemMeta(meta);
               return ParseResult.SUCCESS;
            }
         }
      } else {
         return ParseResult.NULL_ITEM;
      }
   }

   public ParseResult setDisplayName() {
      if (this.item != null && this.item.getItemMeta() != null) {
         String name = ChatColor.translateAlternateColorCodes('&', this.joinRemaining(0));
         ItemMeta meta = this.item.getItemMeta();
         meta.setDisplayName(name);
         this.item.setItemMeta(meta);
         return ParseResult.SUCCESS;
      } else {
         return ParseResult.NULL_ITEM;
      }
   }

   private void clearEnchants() {
      ItemMeta meta = this.item.getItemMeta();
      meta.getEnchants().keySet().forEach((x) -> {
         meta.removeEnchant(x);
      });
      this.item.setItemMeta(meta);
   }

   public ParseResult setItemFlags() {
      if (this.item != null && this.item.getItemMeta() != null) {
         if (this.args.length < 1) {
            return ParseResult.NO_ARGS;
         } else {
            ItemMeta meta = this.item.getItemMeta();

            while(this.args.length > 0) {
               ItemFlag flag = this.getItemFlag(this.popFirstArg());
               if (flag == null) {
                  return ParseResult.INVALID_ITEMFLAG;
               }

               if (meta.getItemFlags().contains(flag)) {
                  meta.removeItemFlags(new ItemFlag[]{flag});
               } else {
                  meta.addItemFlags(new ItemFlag[]{flag});
               }
            }

            this.item.setItemMeta(meta);
            return ParseResult.SUCCESS;
         }
      } else {
         return ParseResult.NULL_ITEM;
      }
   }

   public ParseResult enchant() {
      if (this.item != null && this.item.getItemMeta() != null) {
         if (this.args.length == 1 && this.args[0].equals("clear")) {
            this.clearEnchants();
            return ParseResult.SUCCESS;
         } else if (this.args.length < 2) {
            return ParseResult.NO_ARGS;
         } else if (this.args.length % 2 != 0) {
            return ParseResult.MISSING_VALUE;
         } else {
            while(this.args.length > 0) {
               Enchantment currentEnch = this.getEnchantment(this.popFirstArg().toUpperCase());
               if (currentEnch == null) {
                  return ParseResult.INVALID_ENCHANTMENT;
               }

               int currentLevel = tryParse(this.popFirstArg().toUpperCase());
               if (currentLevel == 0) {
                  return ParseResult.NO_NUMBER;
               }

               this.addEnchantment(currentEnch, currentLevel);
            }

            return ParseResult.SUCCESS;
         }
      } else {
         return ParseResult.NULL_ITEM;
      }
   }

   private ItemFlag getItemFlag(String arg) {
      try {
         return ItemFlag.valueOf(arg.toUpperCase());
      } catch (Exception var3) {
         return null;
      }
   }

   private Enchantment getEnchantment(String arg) {
      try {
         Enchantment olderMethod = EnchantmentUtil.getByName(arg);
         return olderMethod;
      } catch (Exception var3) {
         return null;
      }
   }

   private void addEnchantment(Enchantment ench, int level) {
      ItemMeta meta = this.item.getItemMeta();
      meta.addEnchant(ench, level, true);
      this.item.setItemMeta(meta);
   }

   private static int tryParse(String arg) {
      return tryParse(arg, 0);
   }

   private static int tryParse(String arg, int defaultVal) {
      try {
         return Integer.parseInt(arg);
      } catch (NumberFormatException var3) {
         return defaultVal;
      }
   }

   private String joinRemaining(int start) {
      String joined = "";

      for(int i = start; i < this.args.length; ++i) {
         joined = joined + this.args[i] + (i + 1 == this.args.length ? "" : " ");
      }

      return joined;
   }

   private String popFirstArg() {
      String first = this.args[0];
      String[] newArgs = new String[this.args.length - 1];

      for(int i = 0; i < this.args.length - 1; ++i) {
         newArgs[i] = this.args[i + 1];
      }

      this.args = newArgs;
      return first;
   }
}
