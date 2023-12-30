package com.dutchjelly.craftenhance.files;

import com.dutchjelly.craftenhance.CraftEnhance;
import java.util.Iterator;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigFormatter {
   private CraftEnhance main;

   private ConfigFormatter(CraftEnhance main) {
      this.main = main;
   }

   public static ConfigFormatter init(CraftEnhance main) {
      return new ConfigFormatter(main);
   }

   public void formatConfigMessages() {
      FileConfiguration config = this.main.getConfig();
      this.giveColors(config, "global-prefix");
      Iterator var3 = config.getConfigurationSection("messages").getKeys(false).iterator();

      while(var3.hasNext()) {
         String msgSection = (String)var3.next();
         String path = "messages." + msgSection;
         Iterator var5 = config.getConfigurationSection(path).getKeys(false).iterator();

         while(var5.hasNext()) {
            String msgKey = (String)var5.next();
            this.giveColors(config, path + "." + msgKey);
            this.givePrefix(config, path + "." + msgKey);
         }
      }

   }

   private void giveColors(FileConfiguration config, String path) {
      config.set(path, ChatColor.translateAlternateColorCodes('&', config.getString(path)));
   }

   private void givePrefix(FileConfiguration config, String path) {
      config.set(path, config.getString("global-prefix") + config.getString(path));
   }
}
