package com.dutchjelly.craftenhance.gui.templates;

import com.dutchjelly.craftenhance.exceptions.ConfigError;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class GuiTemplate {
   private static final int RowSize = 9;
   private final ItemStack[] template;
   private final String invTitle;
   private final List<String> invTitles;
   @NonNull
   private final Map<Integer, ButtonType> buttonMapping;
   @NonNull
   private final List<Integer> fillSpace;

   public GuiTemplate(ConfigurationSection config) {
      String name = config.getString("name");
      List<String> names = config.getStringList("names");
      if (name == null && names == null) {
         throw new ConfigError("no gui name is specified");
      } else {
         if (names == null) {
            names = new ArrayList();
         }

         if (name != null) {
            ((List)names).add(name);
         }

          names = names.stream().map((x) -> {
            return ChatColor.translateAlternateColorCodes('&', x);
         }).collect(Collectors.toList());
         if (names.isEmpty()) {
            throw new ConfigError("a template has no name");
         } else {
            ConfigurationSection templateSection = config.getConfigurationSection("template");
            if (templateSection == null) {
               throw new ConfigError("template is not specified");
            } else {
               List<ItemStack> templateInventoryContent = new ArrayList();
               Iterator var6 = templateSection.getKeys(false).iterator();

               while(var6.hasNext()) {
                  String key = (String)var6.next();
                  List<Integer> slots = this.parseRange(key);
                  ItemStack item = (new GuiItemTemplate(templateSection.getConfigurationSection(key))).getItem();
                  Iterator var10 = slots.iterator();

                  while(var10.hasNext()) {
                     int slot = (Integer)var10.next();

                     while(templateInventoryContent.size() - 1 < slot) {
                        templateInventoryContent.addAll(Arrays.asList());
                     }

                     templateInventoryContent.set(slot, item.clone());
                  }
               }

               this.invTitles = names;
               this.invTitle = (String)names.get(0);
               this.template = (ItemStack[])templateInventoryContent.stream().toArray((x$0) -> {
                  return new ItemStack[x$0];
               });
               ConfigurationSection buttonSection = config.getConfigurationSection("button-mapping");
               this.buttonMapping = new HashMap();
               if (buttonSection != null) {
                  Iterator var14 = buttonSection.getKeys(false).iterator();

                  while(var14.hasNext()) {
                     String s = (String)var14.next();
                     List<Integer> slots = this.parseRange(s);
                     ButtonType btn = ButtonType.valueOf(buttonSection.getString(s));
                     slots.forEach((x) -> {
                        ButtonType var10000 = (ButtonType)this.buttonMapping.put(x, btn);
                     });
                  }
               }

               this.fillSpace = this.parseRange(config.getString("fill-space"));
            }
         }
      }
   }

   private List<Integer> parseRange(String range) {
      List<Integer> slots = new ArrayList();
      if (range != null && range != "") {
         try {
            String[] var3 = range.split(",");
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               String subRange = var3[var5];
               if (subRange != "") {
                  if (subRange.contains("-")) {
                     int first = Integer.valueOf(subRange.split("-")[0]);
                     int second = Integer.valueOf(subRange.split("-")[1]);
                     slots.addAll((Collection)IntStream.range(first, second + 1).mapToObj((x) -> {
                        return x;
                     }).collect(Collectors.toList()));
                  } else {
                     slots.add(Integer.valueOf(subRange));
                  }
               }
            }

            return slots;
         } catch (NumberFormatException var9) {
            throw new ConfigError("Couldn't parse range " + range);
         }
      } else {
         return slots;
      }
   }

   public ItemStack[] getTemplate() {
      return this.template;
   }

   public String getInvTitle() {
      return this.invTitle;
   }

   public List<String> getInvTitles() {
      return this.invTitles;
   }

   @NonNull
   public Map<Integer, ButtonType> getButtonMapping() {
      return this.buttonMapping;
   }

   @NonNull
   public List<Integer> getFillSpace() {
      return this.fillSpace;
   }
}
