package com.dutchjelly.craftenhance.files;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.exceptions.ConfigError;
import com.dutchjelly.craftenhance.files.util.SimpleYamlHelper;
import com.dutchjelly.craftenhance.gui.templates.MenuButton;
import com.dutchjelly.craftenhance.gui.templates.MenuTemplate;
import com.dutchjelly.craftenhance.updatechecking.VersionChecker;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class MenuSettingsCache extends SimpleYamlHelper {
   private final Plugin plugin;
   private static final int version = 5;
   private final Map<String, MenuTemplate> templates = new HashMap();

   public MenuSettingsCache(Plugin plugin) {
      super("guitemplates.yml", true, true);
      this.plugin = plugin;
      this.checkFileVersion();
   }

   public void checkFileVersion() {
      File file = new File(this.plugin.getDataFolder(), "guitemplates.yml");
      if (file.exists()) {
         FileConfiguration templateConfig = YamlConfiguration.loadConfiguration(file);
         if (templateConfig.contains("Version")) {
            int configVersion = templateConfig.getInt("Version");
            if (configVersion < 5) {
               this.updateFile(file);
            }
         } else {
            this.updateFile(file);
         }
      }

   }

   public void updateFile(File file) {
      try {
         Files.move(Paths.get(file.getPath()), Paths.get(this.plugin.getDataFolder().getPath(), "guitemplates_backup_5.yml"), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException var8) {
         var8.printStackTrace();
      }

      InputStream file1 = this.plugin.getResource("guitemplates.yml");
      if (file1 != null) {
         FileConfiguration templateConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(file1));
         templateConfig.set("Version", (Object)null);
         Iterator var4 = templateConfig.getKeys(true).iterator();

         while(var4.hasNext()) {
            String templet = (String)var4.next();
            templateConfig.set(templet, templateConfig.get(templet));
         }

         try {
            templateConfig.save(file);
         } catch (IOException var7) {
            var7.printStackTrace();
         }

         File newFile = new File(this.plugin.getDataFolder(), "guitemplates.yml");

         try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(newFile, true));
            bw.append("#Do not change this.\n");
            bw.append("Version: 5");
            bw.close();
         } catch (IOException var6) {
            var6.printStackTrace();
         }
      }

   }

   public Map<String, MenuTemplate> getTemplates() {
      return this.templates;
   }

   public void loadSettingsFromYaml(File file) {
      FileConfiguration templateConfig = this.getCustomConfig();
      Iterator var3 = templateConfig.getKeys(false).iterator();

      while(true) {
         String key;
         do {
            if (!var3.hasNext()) {
               return;
            }

            key = (String)var3.next();
         } while(key.equalsIgnoreCase("Version"));

         ConfigurationSection menuData = templateConfig.getConfigurationSection(key + ".buttons");
         Map<List<Integer>, MenuButton> menuButtonMap = new HashMap();
         String menuSettings = templateConfig.getString(key + ".menu_settings.name");
         List<Integer> fillSpace = this.parseRange(templateConfig.getString(key + ".menu_settings.fill-space"));
         String sound = templateConfig.getString(key + ".menu_settings.sound");
         if (menuData != null) {
            Iterator var10 = menuData.getKeys(false).iterator();

            while(var10.hasNext()) {
               String menuButtons = (String)var10.next();
               MenuButton menuButton = (MenuButton)this.getData(key + ".buttons." + menuButtons, MenuButton.class);
               menuButtonMap.put(this.parseRange(menuButtons), menuButton);
            }
         }

         if (CraftEnhance.self().getVersionChecker().olderThan(VersionChecker.ServerVersion.v1_13) && sound != null && sound.equals("BLOCK_NOTE_BLOCK_BASEDRUM")) {
            sound = "BLOCK_NOTE_BASEDRUM";
         }

         MenuTemplate menuTemplate = new MenuTemplate(menuSettings, fillSpace, menuButtonMap, sound);
         this.templates.put(key, menuTemplate);
         YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
         int configVersion = configuration.getInt("Version", -1);
         if (configVersion <= 1) {
            configuration.set("Version", 5);

            try {
               configuration.save(file);
            } catch (IOException var14) {
               var14.printStackTrace();
            }
         }
      }
   }

   private List<Integer> parseRange(String range) {
      List<Integer> slots = new ArrayList();
      if (range != null && !range.equals("")) {
         try {
            String[] var3 = range.split(",");
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               String subRange = var3[var5];
               if (!Objects.equals(subRange, "")) {
                  if (subRange.contains("-")) {
                     String[] numbers = subRange.split("-");
                     if (!numbers[0].isEmpty() && !numbers[1].isEmpty()) {
                        int first = Integer.parseInt(numbers[0]);
                        int second = Integer.parseInt(numbers[1]);
                        slots.addAll((Collection)IntStream.range(first, second + 1).boxed().collect(Collectors.toList()));
                     } else {
                        slots.add(Integer.parseInt(subRange));
                     }
                  } else {
                     slots.add(Integer.parseInt(subRange));
                  }
               }
            }

            return slots;
         } catch (NumberFormatException var10) {
            throw new ConfigError("Couldn't parse range " + range);
         }
      } else {
         return slots;
      }
   }

   protected void saveDataToFile(File file) {
   }
}
