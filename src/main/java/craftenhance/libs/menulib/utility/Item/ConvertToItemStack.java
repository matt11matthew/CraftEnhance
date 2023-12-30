package craftenhance.libs.menulib.utility.Item;

import com.google.common.base.Enums;
import craftenhance.libs.menulib.utility.ServerVersion;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
;

public class ConvertToItemStack {
   protected ConvertToItemStack() {
   }

   public ItemStack checkItem(Object object) {
      if (object instanceof ItemStack) {
         return (ItemStack)object;
      } else if (object instanceof Material) {
         return new ItemStack((Material)object);
      } else if (object instanceof String) {
         String stringName = ((String)object).toUpperCase(Locale.ROOT);
         return this.checkString(stringName);
      } else {
         return null;
      }
   }

   public ItemStack checkItem(Object object, String color) {
      color = color.toUpperCase(Locale.ROOT);
      if (object instanceof ItemStack) {
         return this.checkItemStack((ItemStack)object, color);
      } else {
         short colorNumber;
         if (object instanceof Material) {
            colorNumber = this.checkColor(color);
            return colorNumber > 0 ? new ItemStack((Material)object, 1, colorNumber) : new ItemStack((Material)object, 1);
         } else if (object instanceof String) {
            String stringName = ((String)object).toUpperCase(Locale.ROOT);
            colorNumber = this.checkColor(color);
            return colorNumber > 0 ? new ItemStack(Enums.getIfPresent(Material.class, stringName).orNull() == null ? Material.AIR : Material.valueOf(stringName), 1, colorNumber) : new ItemStack(Enums.getIfPresent(Material.class, stringName).orNull() == null ? Material.AIR : Material.valueOf(stringName), 1);
         } else {
            return null;
         }
      }
   }

   public ItemStack checkItemStack(ItemStack itemStack, String color) {
      if (ServerVersion.olderThan(ServerVersion.v1_13) && itemStack != null) {
         ItemStack stack = new ItemStack(itemStack.getType(), itemStack.getAmount(), this.checkColor(color));
         ItemMeta itemMeta = itemStack.getItemMeta();
         if (itemMeta != null) {
            stack.setItemMeta(itemMeta);
         }

         return stack;
      } else {
         return itemStack;
      }
   }

   public ItemStack checkString(String stringName) {
      if (ServerVersion.olderThan(ServerVersion.v1_13)) {
         ItemStack stack = this.createStack(stringName, 1);
         if (stack != null) {
            return stack;
         }
      }

      return new ItemStack(Enums.getIfPresent(Material.class, stringName).orNull() == null ? Material.AIR : Material.valueOf(stringName));
   }

   
   public ItemStack createStack(String item, int amount) {
      if (amount <= 0) {
         amount = 1;
      }

      int color = this.checkColor(item);
      if (item.endsWith("STAINED_GLASS_PANE")) {
         return new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), amount, (short)color);
      } else if (item.endsWith("STAINED_GLASS")) {
         return new ItemStack(Material.valueOf("STAINED_GLASS"), amount, (short)color);
      } else if (item.endsWith("_WOOL")) {
         return new ItemStack(Material.valueOf("WOOL"), amount, (short)color);
      } else if (item.endsWith("_CARPET")) {
         return new ItemStack(Material.valueOf("CARPET"), amount, (short)color);
      } else {
         if (ServerVersion.newerThan(ServerVersion.v1_8)) {
            if (item.contains("CONCRETE_POWDER")) {
               return new ItemStack(Material.valueOf("CONCRETE_POWDER"), amount, (short)color);
            }

            if (item.endsWith("_CONCRETE")) {
               return new ItemStack(Material.valueOf("CONCRETE"), amount, (short)color);
            }
         }

         if ((item.endsWith("_TERRACOTTA") || item.endsWith("_STAINED_CLAY")) && !item.endsWith("GLAZED_TERRACOTTA")) {
            return new ItemStack(Material.valueOf("STAINED_CLAY"), amount, (short)color);
         } else if (item.equals("TERRACOTTA")) {
            return new ItemStack(Material.valueOf("HARD_CLAY"), amount, (short)0);
         } else if (item.equals("ENDER_EYE")) {
            return new ItemStack(Material.valueOf("ENDER_PEARL"), amount);
         } else if (item.equals("CRACKED_STONE_BRICKS")) {
            return new ItemStack(Material.valueOf("SMOOTH_BRICK"), amount);
         } else if (item.equals("SMOOTH_STONE")) {
            return new ItemStack(Material.valueOf("STEP"), amount);
         } else if (item.equals("SMOOTH_STONE_SLAB")) {
            return new ItemStack(Material.valueOf("STEP"), amount);
         } else {
            Material material;
            if (item.startsWith("GOLDEN_")) {
               material = Material.getMaterial("GOLD" + item.substring(item.indexOf("_")));
               return new ItemStack(material == null ? Material.AIR : material, amount);
            } else if (item.equals("CLOCK")) {
               return new ItemStack(Material.valueOf("WATCH"), amount);
            } else if (item.equals("CRAFTING_TABLE")) {
               return new ItemStack(Material.valueOf("WORKBENCH"), amount);
            } else if (item.equals("PLAYER_HEAD")) {
               return new ItemStack(Material.valueOf("SKULL_ITEM"), amount);
            } else if (!item.contains("ANDESITE") && !item.contains("DIORITE") && !item.contains("GRANITE")) {
               if (item.equals("CHARCOAL")) {
                  return new ItemStack(Material.valueOf("COAL"), amount, (short)1);
               } else {
                  material = null;
                  if (!item.contains("_DOOR")) {
                     material = Material.getMaterial(item);
                  }

                  if (material != null && color != -1) {
                     return new ItemStack(material, amount, (short)color);
                  } else {
                     return material != null ? new ItemStack(material, amount) : this.checkAndGetWood(item, amount);
                  }
               }
            } else {
               return this.getStoneTypes(Material.STONE, item, amount);
            }
         }
      }
   }

   
   public ItemStack checkAndGetWood(String itemName, int amount) {
      if (itemName == null) {
         return null;
      } else {
         ItemStack itemStack = null;
         if (itemName.equals("OAK_FENCE")) {
            return new ItemStack(Material.valueOf("FENCE"), amount);
         } else if (itemName.equals("OAK_FENCE_GATE")) {
            return new ItemStack(Material.valueOf("FENCE_GATE"), amount);
         } else {
            Material material;
            if (itemName.contains("_PLANKS")) {
               material = Material.getMaterial("WOOD");
               itemStack = this.getWoodItemStack(material, itemName, amount);
            }

            short woodTypeData;
            if (itemName.contains("_LOG")) {
               material = Material.getMaterial("LOG");
               if (material == null) {
                  return null;
               }

               woodTypeData = this.getWoodTypeData(itemName);
               if (woodTypeData >= 0) {
                  if (woodTypeData == 4) {
                     material = Material.getMaterial("LOG_2");
                     woodTypeData = 0;
                  }

                  if (woodTypeData == 5) {
                     material = Material.getMaterial("LOG_2");
                     woodTypeData = 1;
                  }

                  itemStack = this.getWoodItemStack(material, woodTypeData, itemName, amount);
               }
            }

            if (itemName.contains("_SLAB")) {
               material = Material.getMaterial("WOOD_STEP");
               itemStack = this.getWoodItemStack(material, itemName, amount);
            }

//            short woodTypeData;
//            Material material;
            if (itemName.contains("_STAIRS")) {
              woodTypeData = this.getWoodTypeData(itemName);
               material = null;
               if (woodTypeData == 0) {
                  material = Material.getMaterial("WOOD_STAIRS");
               }

               if (woodTypeData == 1) {
                  material = Material.getMaterial("SPRUCE_WOOD_STAIRS");
               }

               if (woodTypeData == 2) {
                  material = Material.getMaterial("BIRCH_WOOD_STAIRS");
               }

               if (woodTypeData == 3) {
                  material = Material.getMaterial("JUNGLE_WOOD_STAIRS");
               }

               if (woodTypeData == 4) {
                  material = Material.getMaterial("ACACIA_STAIRS");
               }

               if (woodTypeData == 5) {
                  material = Material.getMaterial("DARK_OAK_STAIRS");
               }

               itemStack = this.getWoodItemStack(material, woodTypeData, itemName, amount);
            }

            if (itemName.contains("_DOOR")) {
               woodTypeData = this.getWoodTypeData(itemName);
               material = null;
               if (woodTypeData > 0) {
                  material = Material.getMaterial(itemName + "_ITEM");
               }

               if (woodTypeData == 0) {
                  material = Material.getMaterial("WOOD_DOOR");
               }

               itemStack = this.getWoodItemStack(material, (short)0, itemName, amount);
            }

            if (itemName.contains("_BUTTON")) {
               material = Material.getMaterial("WOOD_BUTTON");
               itemStack = this.getWoodItemStack(material, itemName, amount);
            }

            if (itemName.contains("_LEAVES")) {
               material = Material.getMaterial("LEAVES");
               woodTypeData = this.getWoodTypeData(itemName);
               if (woodTypeData >= 0) {
                  if (woodTypeData == 4) {
                     material = Material.getMaterial("LEAVES_2");
                     woodTypeData = 0;
                  }

                  if (woodTypeData == 5) {
                     material = Material.getMaterial("LEAVES_2");
                     woodTypeData = 1;
                  }

                  itemStack = this.getWoodItemStack(material, woodTypeData, itemName, amount);
               }
            }

            if (itemName.contains("_SAPLING")) {
               material = Material.getMaterial("SAPLING");
               itemStack = this.getWoodItemStack(material, itemName, amount);
            }

            if (itemName.endsWith("_SIGN")) {
               material = Material.getMaterial("SIGN");
               itemStack = this.getWoodItemStack(material, (short)0, itemName, amount);
            }

            if (itemStack != null) {
               return itemStack;
            } else {
               material = Material.getMaterial(itemName);
               return material != null ? new ItemStack(material, amount) : null;
            }
         }
      }
   }

   public ItemStack getStoneTypes(Material material, String itemName, int amount) {
      if (material == null) {
         return null;
      } else {
         short stonetype = this.getStoneTypeData(itemName);
         if (stonetype == -1) {
            return new ItemStack(material, amount);
         } else {
            return stonetype >= 0 ? new ItemStack(material, amount, stonetype) : null;
         }
      }
   }

   public ItemStack getWoodItemStack(Material material, String itemName, int amount) {
      return this.getWoodItemStack(material, (short)-1, itemName, amount);
   }

   public ItemStack getWoodItemStack(Material material, short woodTypeData, String itemName, int amount) {
      if (material == null) {
         return null;
      } else {
         if (woodTypeData == -1) {
            woodTypeData = this.getWoodTypeData(itemName);
         }

         return woodTypeData >= 0 ? new ItemStack(material, amount, woodTypeData) : null;
      }
   }

   public short getStoneTypeData(String itemName) {
      if (itemName.equals("GRANITE")) {
         return 1;
      } else if (itemName.equals("POLISHED_GRANITE")) {
         return 2;
      } else if (itemName.equals("DIORITE")) {
         return 3;
      } else if (itemName.equals("POLISHED_DIORITE")) {
         return 4;
      } else if (itemName.equals("ANDESITE")) {
         return 5;
      } else {
         return (short)(itemName.equals("POLISHED_ANDESITE") ? 6 : -1);
      }
   }

   public short getWoodTypeData(String itemName) {
      if (itemName.startsWith("DARK_OAK_")) {
         return 5;
      } else if (itemName.startsWith("OAK_")) {
         return 0;
      } else if (itemName.startsWith("SPRUCE_")) {
         return 1;
      } else if (itemName.startsWith("BIRCH_")) {
         return 2;
      } else if (itemName.startsWith("JUNGLE_")) {
         return 3;
      } else {
         return (short)(itemName.startsWith("ACACIA_") ? 4 : -1);
      }
   }

   public short checkColor(String color) {
      int end;
      if (color.startsWith("LIGHT")) {
         end = color.indexOf("_S");
         if (end < 0) {
            end = color.indexOf("_G");
         }

         if (end < 0) {
            end = color.indexOf("_P");
         }

         if (end < 0) {
            end = color.indexOf("_C");
         }

         if (end < 0) {
            end = color.indexOf("_W");
         }
      } else {
         end = color.indexOf(95);
      }

      if (end < 0) {
         end = color.length();
      }

      color = color.substring(0, end);
      if (color.equals("WHITE")) {
         return 0;
      } else if (color.equals("ORANGE")) {
         return 1;
      } else if (color.equals("MAGENTA")) {
         return 2;
      } else if (color.equals("LIGHT_BLUE")) {
         return 3;
      } else if (color.equals("YELLOW")) {
         return 4;
      } else if (color.equals("LIME")) {
         return 5;
      } else if (color.equals("PINK")) {
         return 6;
      } else if (color.equals("GRAY")) {
         return 7;
      } else if (color.equals("LIGHT_GRAY")) {
         return 8;
      } else if (color.equals("CYAN")) {
         return 9;
      } else if (color.equals("PURPLE")) {
         return 10;
      } else if (color.equals("BLUE")) {
         return 11;
      } else if (color.equals("BROWN")) {
         return 12;
      } else if (color.equals("GREEN")) {
         return 13;
      } else if (color.equals("RED")) {
         return 14;
      } else {
         return (short)(color.equals("BLACK") ? 15 : -1);
      }
   }
}
