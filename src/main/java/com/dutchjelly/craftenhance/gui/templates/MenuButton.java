package com.dutchjelly.craftenhance.gui.templates;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.files.util.ConfigurationSerializeUtility;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MenuButton implements ConfigurationSerializeUtility {
   private final ItemStack itemStack;
   private final DyeColor color;
   private final Material material;
   private final String displayName;
   private final List<String> lore;
   private final boolean glow;
   private final ButtonType buttonType;

   public MenuButton(MenuButton.Builder builder) {
      this.itemStack = builder.itemStack;
      this.color = builder.color;
      this.material = builder.material;
      this.displayName = builder.displayName;
      this.lore = builder.lore;
      this.glow = builder.glow;
      this.buttonType = builder.buttonType;
   }

   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public DyeColor getColor() {
      return this.color;
   }

   public Material getMaterial() {
      return this.material;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public List<String> getLore() {
      return this.lore;
   }

   public boolean isGlow() {
      return this.glow;
   }

   public ButtonType getButtonType() {
      return this.buttonType;
   }

   public Map<String, Object> serialize() {
      Map<String, Object> map = new LinkedHashMap();
      map.put("color", this.color);
      map.put("material", this.material + "");
      map.put("name", this.displayName);
      map.put("lore", this.lore);
      map.put("glow", this.glow);
      if (this.buttonType != null) {
         map.put("buttonType", this.buttonType);
      }

      return map;
   }

   public static MenuButton deserialize(Map<String, Object> map) {
      String color = (String)map.get("color");
      String material = (String)map.get("material");
      String displayName = (String)map.get("name");
      List<String> lore = (List)map.get("lore");
      boolean glow = (Boolean)map.getOrDefault("glow", false);
      String buttonType = (String)map.get("buttonType");
      ItemStack itemStack = Adapter.getItemStack(material, displayName, lore, color, glow);
      MenuButton.Builder builder = new MenuButton.Builder();
      builder.setButtonType(ButtonType.valueOfType(buttonType)).setItemStack(itemStack).setColor(Adapter.dyeColor(color)).setDisplayName(displayName).setMaterial(Material.getMaterial(material)).setGlow(glow).setLore(lore);
      return builder.build();
   }

   public String toString() {
      return "MenuButton{itemStack=" + this.itemStack + ", color=" + this.color + ", material=" + this.material + ", displayName='" + this.displayName + '\'' + ", lore=" + this.lore + ", glow=" + this.glow + ", buttonType=" + this.buttonType + '}';
   }

   public static class Builder {
      public ItemStack itemStack;
      private DyeColor color;
      private Material material;
      private String displayName;
      private List<String> lore;
      private boolean glow;
      private ButtonType buttonType;

      public MenuButton.Builder setItemStack(ItemStack itemStack) {
         this.itemStack = itemStack;
         return this;
      }

      public MenuButton.Builder setColor(DyeColor color) {
         this.color = color;
         return this;
      }

      public MenuButton.Builder setMaterial(Material material) {
         this.material = material;
         return this;
      }

      public MenuButton.Builder setDisplayName(String displayName) {
         this.displayName = displayName;
         return this;
      }

      public MenuButton.Builder setLore(List<String> lore) {
         this.lore = lore;
         return this;
      }

      public MenuButton.Builder setGlow(boolean glow) {
         this.glow = glow;
         return this;
      }

      public MenuButton.Builder setButtonType(ButtonType buttonType) {
         this.buttonType = buttonType;
         return this;
      }

      public MenuButton build() {
         return new MenuButton(this);
      }
   }
}
