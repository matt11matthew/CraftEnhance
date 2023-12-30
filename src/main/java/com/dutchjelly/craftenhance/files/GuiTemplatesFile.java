package com.dutchjelly.craftenhance.files;

import com.dutchjelly.craftenhance.exceptions.ConfigError;
import com.dutchjelly.craftenhance.gui.guis.GUIElement;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import java.io.File;
import java.util.Map;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class GuiTemplatesFile {
   private YamlConfiguration templateConfig;
   private File file;
   private static final String GUI_FOLDER = "com.dutchjelly.craftenhance.gui.guis";
   private static final String GUI_FILE_NAME = "guitemplates.yml";
   private Map<Class<? extends GUIElement>, GuiTemplate> templates;
   private JavaPlugin plugin;

   public GuiTemplatesFile(JavaPlugin plugin) {
      this.file = new File(plugin.getDataFolder(), "guitemplates.yml");
      this.plugin = plugin;
   }

   public void load() {
   }

   public GuiTemplate getTemplate(Class<? extends GUIElement> clazz) {
      if (!this.templates.containsKey(clazz)) {
         throw new ConfigError("Cannot find template of " + clazz.getSimpleName());
      } else {
         return (GuiTemplate)this.templates.get(clazz);
      }
   }
}
