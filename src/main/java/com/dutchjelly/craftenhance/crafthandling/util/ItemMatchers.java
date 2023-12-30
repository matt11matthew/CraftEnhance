package com.dutchjelly.craftenhance.crafthandling.util;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.updatechecking.VersionChecker;
import com.dutchjelly.craftenhance.util.StripColors;
import java.util.Arrays;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemMatchers {
   private static boolean backwardsCompatibleMatching = false;

   public static void init(boolean backwardsCompatibleMatching) {
      ItemMatchers.backwardsCompatibleMatching = backwardsCompatibleMatching;
   }

   public static boolean matchItems(ItemStack a, ItemStack b) {
      if (a != null && b != null) {
         return a.equals(b);
      } else {
         return a == null && b == null;
      }
   }

   public static boolean matchModelData(ItemStack a, ItemStack b) {
      ItemMeta am = a.getItemMeta();
      ItemMeta bm = b.getItemMeta();
      if (am == null) {
         return bm == null || !bm.hasCustomModelData();
      } else if (bm == null) {
         return am == null || !am.hasCustomModelData();
      } else {
         return am.hasCustomModelData() == bm.hasCustomModelData() && (!am.hasCustomModelData() || am.getCustomModelData() == bm.getCustomModelData());
      }
   }

   public static boolean matchMeta(ItemStack a, ItemStack b) {
      if (a != null && b != null) {
         boolean canUseModeldata = Adapter.canUseModeldata();
         if (backwardsCompatibleMatching) {
            return a.getType().equals(b.getType()) && a.getDurability() == b.getDurability() && a.hasItemMeta() == b.hasItemMeta() && (!a.hasItemMeta() || a.getItemMeta().toString().equals(b.getItemMeta().toString()) && (canUseModeldata && matchModelData(a, b) || !canUseModeldata));
         } else {
            return a.isSimilar(b) && (!canUseModeldata || matchModelData(a, b));
         }
      } else {
         return a == null && b == null;
      }
   }

   public static boolean matchType(ItemStack a, ItemStack b) {
      if (a != null && b != null) {
         return a.getType().equals(b.getType());
      } else {
         return a == null && b == null;
      }
   }

   @SafeVarargs
   public static <T> IMatcher<T> constructIMatcher(IMatcher<T>... matchers) {
      return (a, b) -> {
         return Arrays.stream(matchers).allMatch((x) -> {
            return x.match(a, b);
         });
      };
   }

   public static boolean matchCustomModelData(ItemStack a, ItemStack b) {
      if (a != null && b != null) {
         if (a.hasItemMeta() && b.hasItemMeta()) {
            ItemMeta itemMetaA = a.getItemMeta();
            ItemMeta itemMetaB = b.getItemMeta();
            if (itemMetaA != null && itemMetaB != null && itemMetaA.hasCustomModelData() && itemMetaB.hasCustomModelData()) {
               return itemMetaA.getCustomModelData() == itemMetaB.getCustomModelData();
            }
         }

         return false;
      } else {
         return a == null && b == null;
      }
   }

   public static boolean matchTypeData(ItemStack a, ItemStack b) {
      if (a != null && b != null) {
         if (CraftEnhance.self().getVersionChecker().olderThan(VersionChecker.ServerVersion.v1_14)) {
            return a.getData() == null && b.getData() == null ? matchType(a, b) : a.getData().equals(b.getData());
         } else if (a.hasItemMeta() && b.hasItemMeta()) {
            return matchCustomModelData(a, b) || matchType(a, b);
         } else {
            return matchType(a, b);
         }
      } else {
         return a == null && b == null;
      }
   }

   public static boolean matchName(ItemStack a, ItemStack b) {
      if (a.hasItemMeta() && b.hasItemMeta()) {
         return a.getItemMeta().getDisplayName().equals(b.getItemMeta().getDisplayName());
      } else {
         return a.hasItemMeta() == b.hasItemMeta() && a.getType() == b.getType();
      }
   }

   public static boolean matchNameLore(ItemStack a, ItemStack b) {
      if (a.hasItemMeta() && b.hasItemMeta()) {
         ItemMeta itemMetaA = a.getItemMeta();
         ItemMeta itemMetaB = b.getItemMeta();
         if (itemMetaA != null && itemMetaB != null) {
            boolean hasSameLore = itemMetaA.getLore() == null || itemMetaA.getLore().equals(itemMetaB.getLore());
            if (!hasSameLore) {
               hasSameLore = StripColors.stripLore(itemMetaA.getLore()).equals(StripColors.stripLore(itemMetaB.getLore()));
            }

            return itemMetaA.getDisplayName().equals(itemMetaB.getDisplayName()) && hasSameLore;
         }
      }

      return a.hasItemMeta() == b.hasItemMeta() && a.getType() == b.getType();
   }

   public static enum MatchType {
      MATCH_TYPE(ItemMatchers.constructIMatcher(ItemMatchers::matchType), "match type"),
      MATCH_META(ItemMatchers.constructIMatcher(ItemMatchers::matchMeta), "match meta"),
      MATCH_NAME(ItemMatchers.constructIMatcher(ItemMatchers::matchName), "match name"),
      MATCH_MODELDATA_AND_TYPE(ItemMatchers.constructIMatcher(ItemMatchers::matchType, ItemMatchers::matchModelData), "match modeldata and type"),
      MATCH_NAME_LORE(ItemMatchers.constructIMatcher(ItemMatchers::matchNameLore), "match name and lore");

      private final IMatcher<ItemStack> matcher;
      private final String description;

      private MatchType(IMatcher<ItemStack> matcher, String description) {
         this.matcher = matcher;
         this.description = description;
      }

      public IMatcher<ItemStack> getMatcher() {
         return this.matcher;
      }

      public String getDescription() {
         return this.description;
      }
   }
}
