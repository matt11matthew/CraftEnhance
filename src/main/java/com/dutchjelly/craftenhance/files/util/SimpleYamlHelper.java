package com.dutchjelly.craftenhance.files.util;

import com.dutchjelly.craftenhance.CraftEnhance;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
;
;

public abstract class SimpleYamlHelper {
   private final boolean shallGenerateFiles;
   private final boolean singelFile;
   private boolean firstLoad;
   private FileConfiguration customConfig;
   private final String name;
   private String extension;
   private File customConfigFile;
   private final File dataFolder;
   private Set<String> filesFromResource;
   protected final Plugin plugin;

   public SimpleYamlHelper(String name, boolean shallGenerateFiles) {
      this(name, false, shallGenerateFiles);
   }

   public SimpleYamlHelper(String name, boolean singelFile, boolean shallGenerateFiles) {
      this.firstLoad = true;
      this.plugin = CraftEnhance.self();
      if (this.plugin == null) {
         throw new RuntimeException("You have not set the plugin, becuse it is null");
      } else {
         this.dataFolder = this.plugin.getDataFolder();
         this.singelFile = singelFile;
         this.name = this.checkIfFileHasExtension(name);
         this.shallGenerateFiles = shallGenerateFiles;
         File folder = this.plugin.getDataFolder();
         if (!folder.exists()) {
            folder.mkdir();
         }

      }
   }

   protected abstract void saveDataToFile(File var1);

   protected abstract void loadSettingsFromYaml(File var1);

   public FileConfiguration getCustomConfig() {
      return this.customConfig;
   }

   public File getCustomConfigFile() {
      return this.customConfigFile;
   }

   public String checkIfFileHasExtension(String name) {
      SimpleYamlHelper.Valid.checkBoolean(name != null && !name.isEmpty(), "The given path must not be empty!");
      if (!this.isSingelFile()) {
         return name;
      } else {
         int pos = name.lastIndexOf(".");
         if (pos == -1) {
            this.setExtension(name.substring(pos));
         }

         return name;
      }
   }

   public String getExtension() {
      if (this.extension == null) {
         return "yml";
      } else {
         String extension = this.extension;
         if (extension.startsWith(".")) {
            extension = extension.substring(1);
         }

         return extension;
      }
   }

   public void setExtension(String extension) {
      this.extension = extension;
   }

   public void reload() {
      try {
         if (this.getCustomConfigFile() == null) {
            this.load(this.getAllFilesInPluginJar());
         } else {
            this.load(this.getFilesInPluginFolder(this.getName()));
         }
      } catch (InvalidConfigurationException | IOException var2) {
         var2.printStackTrace();
      }

   }

   public void load(File[] files) throws IOException, InvalidConfigurationException {
      if (files != null && files.length > 0) {
         File[] var6 = files;
         int var3 = files.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            File file = var6[var4];
            if (file != null) {
               if (this.getCustomConfigFile() == null) {
                  this.customConfigFile = file;
               }

               if (!file.exists()) {
                  this.plugin.saveResource(file.getName(), false);
               }

               if (this.firstLoad) {
                  this.customConfig = YamlConfiguration.loadConfiguration(file);
                  this.firstLoad = false;
               } else {
                  this.customConfig.load(file);
               }

               this.loadSettingsFromYaml(file);
            }
         }
      } else if (this.shallGenerateFiles) {
         File file = new File(this.getDataFolder(), this.getName());
         if (!this.dataFolder.exists() && !this.isSingelFile()) {
            this.dataFolder.mkdirs();
         }

         if (this.isSingelFile()) {
            file.createNewFile();
         }

         this.customConfig = YamlConfiguration.loadConfiguration(file);
      }

   }

   public File getDataFolder() {
      return this.dataFolder;
   }

   public boolean removeFile(String fileName) {
      File dataFolder = new File(this.isSingelFile() ? this.getDataFolder().getParent() : this.getPath(), fileName + "." + this.getExtension());
      return dataFolder.delete();
   }

   public void save() {
      this.save((String)null);
   }

   public void save(String fileToSave) {
      File dataFolder = new File(this.getPath());
      if (!dataFolder.isDirectory()) {
         this.saveData(dataFolder);
      } else {
         File[] listOfFiles = dataFolder.listFiles();
         if (dataFolder.exists() && listOfFiles != null) {
            int var5;
            int var6;
            File file;
            File[] var12;
            if (fileToSave != null) {
               if (!this.checkFolderExist(fileToSave, listOfFiles)) {
                  File newDataFolder = new File(this.getPath(), fileToSave + "." + this.getExtension());

                  try {
                     newDataFolder.createNewFile();
                  } catch (IOException var10) {
                     var10.printStackTrace();
                  } finally {
                     this.saveData(newDataFolder);
                  }
               } else {
                  var12 = listOfFiles;
                  var5 = listOfFiles.length;

                  for(var6 = 0; var6 < var5; ++var6) {
                     file = var12[var6];
                     if (this.getNameOfFile(file.getName()).equals(fileToSave)) {
                        this.saveData(file);
                     }
                  }
               }
            } else {
               var12 = listOfFiles;
               var5 = listOfFiles.length;

               for(var6 = 0; var6 < var5; ++var6) {
                  file = var12[var6];
                  this.saveData(file);
               }
            }
         }

      }
   }

   private void saveData(File file) {
      this.saveDataToFile(file);
   }

   
   public <T extends ConfigurationSerializeUtility> T getData(String path, Class<T> clazz) {
      SimpleYamlHelper.Valid.checkBoolean(path != null, "path can't be null");
      if (clazz == null) {
         return null;
      } else {
         Map<String, Object> fileData = new HashMap();
         ConfigurationSection configurationSection = this.customConfig.getConfigurationSection(path);
         if (configurationSection != null) {
            Iterator var5 = configurationSection.getKeys(true).iterator();

            while(var5.hasNext()) {
               String data = (String)var5.next();
               Object object = this.customConfig.get(path + "." + data);
               if (!(object instanceof MemorySection)) {
                  fileData.put(data, object);
               }
            }
         }

         Method deserializeMethod = this.getMethod(clazz, "deserialize", Map.class);
         return this.invokeStatic(clazz, deserializeMethod, fileData);
      }
   }

   public void setData( File file,  String path,  ConfigurationSerializeUtility configuration) {
      SimpleYamlHelper.Valid.checkBoolean(path != null, "path can't be null");
      SimpleYamlHelper.Valid.checkBoolean(configuration != null, "Serialize utility can't be null, need provide a class instance some implements ConfigurationSerializeUtility");
      SimpleYamlHelper.Valid.checkBoolean(configuration.serialize() != null, "Missing serialize method or it is null, can't serialize the class data.");
      this.getCustomConfig().set(path, (Object)null);
      Iterator var4 = configuration.serialize().entrySet().iterator();

      while(var4.hasNext()) {
         Entry<String, Object> key = (Entry)var4.next();
         this.getCustomConfig().set(path + "." + (String)key.getKey(), SerializeData.serialize(key.getValue()));
      }

      try {
         this.getCustomConfig().save(file);
      } catch (IOException var6) {
         var6.printStackTrace();
      }

   }

   public String getName() {
      return this.name;
   }

   public boolean isSingelFile() {
      return this.singelFile;
   }

   public boolean isFolderNameEmpty() {
      return this.getName() == null || this.getName().isEmpty();
   }

   public String getFileName() {
      return this.getNameOfFile(this.getName());
   }

   public File[] getAllFilesInPluginJar() {
      if (this.shallGenerateFiles) {
         List<String> filenamesFromDir = this.getFilenamesForDirnameFromCP(this.getName());
         if (filenamesFromDir != null) {
            this.filesFromResource = new HashSet(filenamesFromDir);
         }
      }

      return this.getFilesInPluginFolder(this.getName());
   }

   public List<String> getFiles() {
      return this.getFilenamesForDirnameFromCP(this.getName());
   }

   public boolean checkFolderExist(String fileToSave, File[] dataFolders) {
      if (fileToSave != null) {
         File[] var3 = dataFolders;
         int var4 = dataFolders.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            File file = var3[var5];
            String fileName = this.getNameOfFile(file.getName());
            if (fileName.equals(fileToSave)) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean fileExists(String path) {
      File outFile;
      if (path.contains("/")) {
         outFile = new File(this.getDataFolder() + "/" + path);
      } else {
         outFile = new File(this.getPath());
      }

      return outFile.exists();
   }

   public String getPath() {
      return this.getDataFolder() + "/" + this.getName();
   }

   public File[] getFilesInPluginFolder(String directory) {
      File dataFolder;
      if (this.isSingelFile()) {
         dataFolder = new File(this.getDataFolder(), this.getName());
         if (!dataFolder.exists() && this.shallGenerateFiles) {
            this.createMissingFile();
         }

         return (new File(dataFolder.getParent())).listFiles((file) -> {
            return !file.isDirectory() && file.getName().equals(this.getName(this.getName()));
         });
      } else {
         dataFolder = new File(this.getDataFolder(), directory);
         if (!dataFolder.exists() && !directory.isEmpty()) {
            dataFolder.mkdirs();
         }

         if (this.filesFromResource != null) {
            this.createMissingFiles(dataFolder.listFiles((file) -> {
               return !file.isDirectory() && file.getName().endsWith("." + this.getExtension());
            }));
         }

         return dataFolder.listFiles((file) -> {
            return !file.isDirectory() && file.getName().endsWith("." + this.getExtension());
         });
      }
   }

   public String getNameOfFile(String path) {
      SimpleYamlHelper.Valid.checkBoolean(path != null && !path.isEmpty(), "The given path must not be empty!");
      int pos;
      if (path.lastIndexOf("/") == -1) {
         pos = path.lastIndexOf("\\");
      } else {
         pos = path.lastIndexOf("/");
      }

      if (pos > 0) {
         path = path.substring(pos + 1);
      }

      pos = path.lastIndexOf(".");
      if (pos > 0) {
         path = path.substring(0, pos);
      }

      return path;
   }

   public String getName(String path) {
      SimpleYamlHelper.Valid.checkBoolean(path != null && !path.isEmpty(), "The given path must not be empty!");
      int pos;
      if (path.lastIndexOf("/") == -1) {
         pos = path.lastIndexOf("\\");
      } else {
         pos = path.lastIndexOf("/");
      }

      if (pos > 0) {
         path = path.substring(pos + 1);
      }

      return path;
   }

   public Map<String, Object> createFileFromResource(String path) {
      InputStream inputStream = this.plugin.getResource(path);
      if (inputStream == null) {
         return null;
      } else {
         Map<String, Object> values = new LinkedHashMap();
         FileConfiguration newConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
         Iterator var5 = newConfig.getKeys(true).iterator();

         while(var5.hasNext()) {
            String key = (String)var5.next();
            Object value = newConfig.get(key);
            if (value != null && !value.toString().startsWith("MemorySection")) {
               values.put(key, value);
            }
         }

         return values;
      }
   }

   public List<String> getFilenamesForDirnameFromCP(String directoryName) {
      List<String> filenames = new ArrayList();
      URL url = this.plugin.getClass().getClassLoader().getResource(directoryName);
      if (url != null) {
         if (url.getProtocol().equals("file")) {
            try {
               File file = Paths.get(url.toURI()).toFile();
               File[] files = file.listFiles();
               if (files != null) {
                  File[] var6 = files;
                  int var7 = files.length;

                  for(int var8 = 0; var8 < var7; ++var8) {
                     File filename = var6[var8];
                     filenames.add(filename.toString());
                  }
               }
            } catch (URISyntaxException var25) {
               var25.printStackTrace();
            }
         } else if (url.getProtocol().equals("jar")) {
            String dirname = this.isSingelFile() ? directoryName : directoryName + "/";
            String path = url.getPath();
            String jarPath = path.substring(5, path.indexOf("!"));

            try {
               JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8.name()));
               Throwable var30 = null;

               try {
                  Enumeration entries = jar.entries();

                  while(true) {
                     while(entries.hasMoreElements()) {
                        JarEntry entry = (JarEntry)entries.nextElement();
                        String name = entry.getName();
                        if (!this.isSingelFile() && name.startsWith(this.getFileName())) {
                           filenames.add(name);
                        } else if (name.startsWith(dirname)) {
                           URL resource = this.plugin.getClass().getClassLoader().getResource(name);
                           if (resource != null) {
                              filenames.add(name);
                           } else {
                              this.plugin.getLogger().warning("Missing files in plugins/" + this.plugin + ".jar/" + directoryName + "/, contact the author of " + this.plugin.getName() + ".");
                           }
                        }
                     }

                     return filenames;
                  }
               } catch (Throwable var22) {
                  var30 = var22;
                  throw var22;
               } finally {
                  if (jar != null) {
                     if (var30 != null) {
                        try {
                           jar.close();
                        } catch (Throwable var21) {
                           var30.addSuppressed(var21);
                        }
                     } else {
                        jar.close();
                     }
                  }

               }
            } catch (IOException var24) {
               var24.printStackTrace();
            }
         }
      }

      return filenames;
   }

   private Method getMethod(Class<?> clazz, String methodName, Class<?>... args) {
      Method[] var4 = clazz.getMethods();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Method method = var4[var6];
         if (method.getName().equals(methodName) && this.isClassListEqual(args, method.getParameterTypes())) {
            method.setAccessible(true);
            return method;
         }
      }

      return null;
   }

   private <T extends ConfigurationSerializeUtility> T invokeStatic(Class<T> clazz, Method method, Object... params) {
      if (method == null) {
         return null;
      } else {
         try {
            SimpleYamlHelper.Valid.checkBoolean(Modifier.isStatic(method.getModifiers()), "deserialize method need to be static");
            return (T) clazz.cast(method.invoke(method, params));
         } catch (InvocationTargetException | IllegalAccessException var5) {
            throw new SimpleYamlHelper.Valid.CatchExceptions(var5, "Could not invoke static method " + method + " with params " + StringUtils.join(params));
         }
      }
   }

   private void createMissingFile() {
      try {
         this.plugin.saveResource(this.getName(), false);
      } catch (IllegalArgumentException var6) {
         InputStream inputStream = this.plugin.getResource(this.getName());
         if (inputStream == null) {
            return;
         }

         YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));

         try {
            newConfig.save(this.getName());
         } catch (IOException var5) {
            var5.printStackTrace();
         }
      }

   }

   private void createMissingFiles(File[] listFiles) {
      if (this.filesFromResource != null) {
         if (listFiles != null && listFiles.length >= 1) {
            this.filesFromResource.stream().filter((files) -> {
               if (!files.endsWith(this.getExtension())) {
                  return false;
               } else {
                  File[] var3 = listFiles;
                  int var4 = listFiles.length;

                  for(int var5 = 0; var5 < var4; ++var5) {
                     File file = var3[var5];
                     if (this.getName(files).equals(file.getName())) {
                        return false;
                     }
                  }

                  return true;
               }
            }).forEach((files) -> {
               this.plugin.saveResource(files, false);
            });
         } else {
            this.filesFromResource.forEach((files) -> {
               if (files.endsWith(this.getExtension())) {
                  this.plugin.saveResource(files, false);
               }

            });
         }
      }
   }

   private boolean isClassListEqual(Class<?>[] first, Class<?>[] second) {
      if (first.length != second.length) {
         return false;
      } else {
         for(int i = 0; i < first.length; ++i) {
            if (first[i] != second[i]) {
               return false;
            }
         }

         return true;
      }
   }

   private static class Valid extends RuntimeException {
      public static void checkBoolean(boolean b, String s) {
         if (!b) {
            throw new SimpleYamlHelper.Valid.CatchExceptions(s);
         }
      }

      private static class CatchExceptions extends RuntimeException {
         public CatchExceptions(String message) {
            super(message);
         }

         public CatchExceptions(Exception ex, String message) {
            super(message, ex);
         }
      }
   }
}
