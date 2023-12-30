package com.dutchjelly.craftenhance.crafthandling.util;

import java.util.Arrays;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class WBRecipeComparer {
   private static ItemStack[] mirror(ItemStack[] content, int size) {
      if (content == null) {
         return null;
      } else if (content.length == 0) {
         return content;
      } else {
         ItemStack[] mirrored = new ItemStack[content.length];

         for(int i = 0; i < size; ++i) {
            for(int j = 0; j < size / 2; ++j) {
               int i1 = i * size + (size - j - 1);
               mirrored[i * size + j] = content[i1];
               mirrored[i1] = content[i * size + j];
            }

            if (size % 2 != 0) {
               mirrored[i * size + size / 2] = content[i * size + size / 2];
            }
         }

         return mirrored;
      }
   }

   public static boolean shapeIterationMatches(ItemStack[] itemsOne, ItemStack[] itemsTwo, IMatcher<ItemStack> matcher, int rowSize) {
      int indexTwo = -1;
      int indexOne = -1;

      do {
         ++indexTwo;
      } while(indexTwo < itemsTwo.length && (itemsTwo[indexTwo] == null || itemsTwo[indexTwo].getType() == Material.AIR));

      do {
         ++indexOne;
      } while(indexOne < itemsOne.length && (itemsOne[indexOne] == null || itemsOne[indexOne].getType() == Material.AIR));

      if (indexTwo != itemsTwo.length && indexOne != itemsOne.length) {
         if (!matcher.match(itemsTwo[indexTwo], itemsOne[indexOne])) {
            return false;
         } else {
            while(true) {
               int twoRowOffset = 0;
               int iIndex = 0;
               int oneRowOffset = 0;
               int jIndex = 0;

               do {
                  ++indexTwo;
                  if (indexTwo >= itemsTwo.length) {
                     break;
                  }

                  ++iIndex;
                  if (indexTwo % rowSize == 0) {
                     ++twoRowOffset;
                  }
               } while(itemsTwo[indexTwo] == null || itemsTwo[indexTwo].getType() == Material.AIR);

               do {
                  ++indexOne;
                  if (indexOne >= itemsOne.length) {
                     break;
                  }

                  ++jIndex;
                  if (indexOne % rowSize == 0) {
                     ++oneRowOffset;
                  }
               } while(itemsOne[indexOne] == null || itemsOne[indexOne].getType() == Material.AIR);

               if (indexTwo != itemsTwo.length && indexOne != itemsOne.length) {
                  if (!matcher.match(itemsTwo[indexTwo], itemsOne[indexOne])) {
                     return false;
                  }

                  if (iIndex == jIndex && twoRowOffset == oneRowOffset) {
                     continue;
                  }

                  return false;
               }

               return indexTwo == itemsTwo.length && indexOne == itemsOne.length;
            }
         }
      } else {
         return indexTwo == itemsTwo.length && indexOne == itemsOne.length;
      }
   }

   public static boolean shapeMatches(ItemStack[] content, ItemStack[] stacks, IMatcher<ItemStack> matcher) {
      int rowSize = content == null ? 0 : (int)Math.sqrt((double)content.length);
      return shapeIterationMatches(content, stacks, matcher, rowSize) || shapeIterationMatches(mirror(content, rowSize), stacks, matcher, rowSize);
   }

   private static ItemStack[] ensureNoGaps(ItemStack[] items) {
      return (ItemStack[])Arrays.asList(items).stream().filter((x) -> {
         return x != null && x.getType() != Material.AIR;
      }).toArray((x$0) -> {
         return new ItemStack[x$0];
      });
   }

   public static boolean ingredientsMatch(ItemStack[] a, ItemStack[] b, IMatcher<ItemStack> matcher) {
      a = ensureNoGaps(a);
      b = ensureNoGaps(b);
      if (a.length != 0 && b.length != 0) {
         if (a.length != b.length) {
            return false;
         } else {
            Boolean[] used = new Boolean[a.length];
            Arrays.fill(used, false);
            ItemStack[] var4 = a;
            int var5 = a.length;

            for(int var6 = 0; var6 < var5; ++var6) {
               ItemStack inRecipe = var4[var6];
               if (inRecipe != null) {
                  for(int i = 0; i < used.length; ++i) {
                     if (!used[i]) {
                        if (b[i] == null) {
                           Bukkit.getLogger().log(Level.SEVERE, "Error, found null ingredient.");
                           return false;
                        }

                        if (matcher.match(b[i], inRecipe)) {
                           used[i] = true;
                           break;
                        }
                     }
                  }
               }
            }

            return !Arrays.stream(used).anyMatch((x) -> {
               return !x;
            });
         }
      } else {
         return false;
      }
   }
}
