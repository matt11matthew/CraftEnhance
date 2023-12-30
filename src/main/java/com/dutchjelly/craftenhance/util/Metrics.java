package com.dutchjelly.craftenhance.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.HttpsURLConnection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

public class Metrics {
   public static final int B_STATS_VERSION = 1;
   private static final String URL = "https://bStats.org/submitData/bukkit";
   private boolean enabled;
   private static boolean logFailedRequests;
   private static boolean logSentData;
   private static boolean logResponseStatusText;
   private static String serverUUID;
   private final Plugin plugin;
   private final int pluginId;
   private final List<Metrics.CustomChart> charts = new ArrayList();

   public Metrics(Plugin plugin, int pluginId) {
      if (plugin == null) {
         throw new IllegalArgumentException("Plugin cannot be null!");
      } else {
         this.plugin = plugin;
         this.pluginId = pluginId;
         File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
         File configFile = new File(bStatsFolder, "config.yml");
         YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
         if (!config.isSet("serverUuid")) {
            config.addDefault("enabled", true);
            config.addDefault("serverUuid", UUID.randomUUID().toString());
            config.addDefault("logFailedRequests", false);
            config.addDefault("logSentData", false);
            config.addDefault("logResponseStatusText", false);
            config.options().header("bStats collects some data for plugin authors like how many servers are using their plugins.\nTo honor their work, you should not disable it.\nThis has nearly no effect on the server performance!\nCheck out https://bStats.org/ to learn more :)").copyDefaults(true);

            try {
               config.save(configFile);
            } catch (IOException var10) {
            }
         }

         this.enabled = config.getBoolean("enabled", true);
         serverUUID = config.getString("serverUuid");
         logFailedRequests = config.getBoolean("logFailedRequests", false);
         logSentData = config.getBoolean("logSentData", false);
         logResponseStatusText = config.getBoolean("logResponseStatusText", false);
         if (this.enabled) {
            boolean found = false;
            Iterator var7 = Bukkit.getServicesManager().getKnownServices().iterator();

            while(var7.hasNext()) {
               Class service = (Class)var7.next();

               try {
                  service.getField("B_STATS_VERSION");
                  found = true;
                  break;
               } catch (NoSuchFieldException var11) {
               }
            }

            Bukkit.getServicesManager().register(Metrics.class, this, plugin, ServicePriority.Normal);
            if (!found) {
               this.startSubmitting();
            }
         }

      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void addCustomChart(Metrics.CustomChart chart) {
      if (chart == null) {
         throw new IllegalArgumentException("Chart cannot be null!");
      } else {
         this.charts.add(chart);
      }
   }

   private void startSubmitting() {
      final Timer timer = new Timer(true);
      timer.scheduleAtFixedRate(new TimerTask() {
         public void run() {
            if (!Metrics.this.plugin.isEnabled()) {
               timer.cancel();
            } else {
               Bukkit.getScheduler().runTask(Metrics.this.plugin, () -> {
                  Metrics.this.submitData();
               });
            }
         }
      }, 300000L, 1800000L);
   }

   public JsonObject getPluginData() {
      JsonObject data = new JsonObject();
      String pluginName = this.plugin.getDescription().getName();
      String pluginVersion = this.plugin.getDescription().getVersion();
      data.addProperty("pluginName", pluginName);
      data.addProperty("id", this.pluginId);
      data.addProperty("pluginVersion", pluginVersion);
      JsonArray customCharts = new JsonArray();
      Iterator var5 = this.charts.iterator();

      while(var5.hasNext()) {
         Metrics.CustomChart customChart = (Metrics.CustomChart)var5.next();
         JsonObject chart = customChart.getRequestJsonObject();
         if (chart != null) {
            customCharts.add(chart);
         }
      }

      data.add("customCharts", customCharts);
      return data;
   }

   private JsonObject getServerData() {
      int playerAmount;
      try {
         Method onlinePlayersMethod = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers");
         playerAmount = onlinePlayersMethod.getReturnType().equals(Collection.class) ? ((Collection)onlinePlayersMethod.invoke(Bukkit.getServer())).size() : ((Player[])((Player[])onlinePlayersMethod.invoke(Bukkit.getServer()))).length;
      } catch (Exception var11) {
         playerAmount = Bukkit.getOnlinePlayers().size();
      }

      int onlineMode = Bukkit.getOnlineMode() ? 1 : 0;
      String bukkitVersion = Bukkit.getVersion();
      String bukkitName = Bukkit.getName();
      String javaVersion = System.getProperty("java.version");
      String osName = System.getProperty("os.name");
      String osArch = System.getProperty("os.arch");
      String osVersion = System.getProperty("os.version");
      int coreCount = Runtime.getRuntime().availableProcessors();
      JsonObject data = new JsonObject();
      data.addProperty("serverUUID", serverUUID);
      data.addProperty("playerAmount", playerAmount);
      data.addProperty("onlineMode", onlineMode);
      data.addProperty("bukkitVersion", bukkitVersion);
      data.addProperty("bukkitName", bukkitName);
      data.addProperty("javaVersion", javaVersion);
      data.addProperty("osName", osName);
      data.addProperty("osArch", osArch);
      data.addProperty("osVersion", osVersion);
      data.addProperty("coreCount", coreCount);
      return data;
   }

   private void submitData() {
      JsonObject data = this.getServerData();
      JsonArray pluginData = new JsonArray();
      Iterator var3 = Bukkit.getServicesManager().getKnownServices().iterator();

      while(var3.hasNext()) {
         Class service = (Class)var3.next();

         try {
            service.getField("B_STATS_VERSION");
            Iterator var5 = Bukkit.getServicesManager().getRegistrations(service).iterator();

            while(var5.hasNext()) {
               RegisteredServiceProvider provider = (RegisteredServiceProvider)var5.next();

               try {
                  Object plugin = provider.getService().getMethod("getPluginData").invoke(provider.getProvider());
                  if (plugin instanceof JsonObject) {
                     pluginData.add((JsonObject)plugin);
                  } else {
                     try {
                        Class<?> jsonObjectJsonSimple = Class.forName("org.json.simple.JSONObject");
                        if (plugin.getClass().isAssignableFrom(jsonObjectJsonSimple)) {
                           Method jsonStringGetter = jsonObjectJsonSimple.getDeclaredMethod("toJSONString");
                           jsonStringGetter.setAccessible(true);
                           String jsonString = (String)jsonStringGetter.invoke(plugin);
                           JsonObject object = (new JsonParser()).parse(jsonString).getAsJsonObject();
                           pluginData.add(object);
                        }
                     } catch (ClassNotFoundException var12) {
                        if (logFailedRequests) {
                           this.plugin.getLogger().log(Level.SEVERE, "Encountered unexpected exception", var12);
                        }
                     }
                  }
               } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NullPointerException var13) {
               }
            }
         } catch (NoSuchFieldException var14) {
         }
      }

      data.add("plugins", pluginData);
      (new Thread(() -> {
         try {
            sendData(this.plugin, data);
         } catch (Exception e) {
            if (logFailedRequests) {
               this.plugin.getLogger().log(Level.WARNING, "Could not submit plugin stats of " + this.plugin.getName(), e);
            }
         }

      })).start();
   }

   private static void sendData(Plugin plugin, JsonObject data) throws Exception {
      if (data == null) {
         throw new IllegalArgumentException("Data cannot be null!");
      } else if (Bukkit.isPrimaryThread()) {
         throw new IllegalAccessException("This method must not be called from the main thread!");
      } else {
         if (logSentData) {
            plugin.getLogger().info("Sending data to bStats: " + data);
         }

         HttpsURLConnection connection = (HttpsURLConnection)(new URL("https://bStats.org/submitData/bukkit")).openConnection();
         byte[] compressedData = compress(data.toString());
         connection.setRequestMethod("POST");
         connection.addRequestProperty("Accept", "application/json");
         connection.addRequestProperty("Connection", "close");
         connection.addRequestProperty("Content-Encoding", "gzip");
         connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
         connection.setRequestProperty("Content-Type", "application/json");
         connection.setRequestProperty("User-Agent", "MC-Server/1");
         connection.setDoOutput(true);
         DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
         Throwable var5 = null;

         try {
            outputStream.write(compressedData);
         } catch (Throwable var28) {
            var5 = var28;
            throw var28;
         } finally {
            if (outputStream != null) {
               if (var5 != null) {
                  try {
                     outputStream.close();
                  } catch (Throwable var26) {
                     var5.addSuppressed(var26);
                  }
               } else {
                  outputStream.close();
               }
            }

         }

         StringBuilder builder = new StringBuilder();
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         Throwable var6 = null;

         try {
            String line;
            try {
               while((line = bufferedReader.readLine()) != null) {
                  builder.append(line);
               }
            } catch (Throwable var30) {
               var6 = var30;
               throw var30;
            }
         } finally {
            if (bufferedReader != null) {
               if (var6 != null) {
                  try {
                     bufferedReader.close();
                  } catch (Throwable var27) {
                     var6.addSuppressed(var27);
                  }
               } else {
                  bufferedReader.close();
               }
            }

         }

         if (logResponseStatusText) {
            plugin.getLogger().info("Sent data to bStats and received response: " + builder);
         }

      }
   }

   private static byte[] compress(String str) throws IOException {
      if (str == null) {
         return null;
      } else {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
         Throwable var3 = null;

         try {
            gzip.write(str.getBytes(StandardCharsets.UTF_8));
         } catch (Throwable var12) {
            var3 = var12;
            throw var12;
         } finally {
            if (gzip != null) {
               if (var3 != null) {
                  try {
                     gzip.close();
                  } catch (Throwable var11) {
                     var3.addSuppressed(var11);
                  }
               } else {
                  gzip.close();
               }
            }

         }

         return outputStream.toByteArray();
      }
   }

   static {
      if (System.getProperty("bstats.relocatecheck") == null || !System.getProperty("bstats.relocatecheck").equals("false")) {
         String defaultPackage = new String(new byte[]{111, 114, 103, 46, 98, 115, 116, 97, 116, 115, 46, 98, 117, 107, 107, 105, 116});
         String examplePackage = new String(new byte[]{121, 111, 117, 114, 46, 112, 97, 99, 107, 97, 103, 101});
         if (Metrics.class.getPackage().getName().equals(defaultPackage) || Metrics.class.getPackage().getName().equals(examplePackage)) {
            throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
         }
      }

   }

   public static class AdvancedBarChart extends Metrics.CustomChart {
      private final Callable<Map<String, int[]>> callable;

      public AdvancedBarChart(String chartId, Callable<Map<String, int[]>> callable) {
         super(chartId);
         this.callable = callable;
      }

      protected JsonObject getChartData() throws Exception {
         JsonObject data = new JsonObject();
         JsonObject values = new JsonObject();
         Map<String, int[]> map = (Map)this.callable.call();
         if (map != null && !map.isEmpty()) {
            boolean allSkipped = true;
            Iterator var5 = map.entrySet().iterator();

            while(true) {
               Entry entry;
               do {
                  if (!var5.hasNext()) {
                     if (allSkipped) {
                        return null;
                     }

                     data.add("values", values);
                     return data;
                  }

                  entry = (Entry)var5.next();
               } while(((int[])entry.getValue()).length == 0);

               allSkipped = false;
               JsonArray categoryValues = new JsonArray();
               int[] var8 = (int[])entry.getValue();
               int var9 = var8.length;

               for(int var10 = 0; var10 < var9; ++var10) {
                  int categoryValue = var8[var10];
                  categoryValues.add(new JsonPrimitive(categoryValue));
               }

               values.add((String)entry.getKey(), categoryValues);
            }
         } else {
            return null;
         }
      }
   }

   public static class SimpleBarChart extends Metrics.CustomChart {
      private final Callable<Map<String, Integer>> callable;

      public SimpleBarChart(String chartId, Callable<Map<String, Integer>> callable) {
         super(chartId);
         this.callable = callable;
      }

      protected JsonObject getChartData() throws Exception {
         JsonObject data = new JsonObject();
         JsonObject values = new JsonObject();
         Map<String, Integer> map = (Map)this.callable.call();
         if (map != null && !map.isEmpty()) {
            Iterator var4 = map.entrySet().iterator();

            while(var4.hasNext()) {
               Entry<String, Integer> entry = (Entry)var4.next();
               JsonArray categoryValues = new JsonArray();
               categoryValues.add(new JsonPrimitive((Number)entry.getValue()));
               values.add((String)entry.getKey(), categoryValues);
            }

            data.add("values", values);
            return data;
         } else {
            return null;
         }
      }
   }

   public static class MultiLineChart extends Metrics.CustomChart {
      private final Callable<Map<String, Integer>> callable;

      public MultiLineChart(String chartId, Callable<Map<String, Integer>> callable) {
         super(chartId);
         this.callable = callable;
      }

      protected JsonObject getChartData() throws Exception {
         JsonObject data = new JsonObject();
         JsonObject values = new JsonObject();
         Map<String, Integer> map = (Map)this.callable.call();
         if (map != null && !map.isEmpty()) {
            boolean allSkipped = true;
            Iterator var5 = map.entrySet().iterator();

            while(var5.hasNext()) {
               Entry<String, Integer> entry = (Entry)var5.next();
               if ((Integer)entry.getValue() != 0) {
                  allSkipped = false;
                  values.addProperty((String)entry.getKey(), (Number)entry.getValue());
               }
            }

            if (allSkipped) {
               return null;
            } else {
               data.add("values", values);
               return data;
            }
         } else {
            return null;
         }
      }
   }

   public static class SingleLineChart extends Metrics.CustomChart {
      private final Callable<Integer> callable;

      public SingleLineChart(String chartId, Callable<Integer> callable) {
         super(chartId);
         this.callable = callable;
      }

      protected JsonObject getChartData() throws Exception {
         JsonObject data = new JsonObject();
         int value = (Integer)this.callable.call();
         if (value == 0) {
            return null;
         } else {
            data.addProperty("value", value);
            return data;
         }
      }
   }

   public static class DrilldownPie extends Metrics.CustomChart {
      private final Callable<Map<String, Map<String, Integer>>> callable;

      public DrilldownPie(String chartId, Callable<Map<String, Map<String, Integer>>> callable) {
         super(chartId);
         this.callable = callable;
      }

      public JsonObject getChartData() throws Exception {
         JsonObject data = new JsonObject();
         JsonObject values = new JsonObject();
         Map<String, Map<String, Integer>> map = (Map)this.callable.call();
         if (map != null && !map.isEmpty()) {
            boolean reallyAllSkipped = true;
            Iterator var5 = map.entrySet().iterator();

            while(var5.hasNext()) {
               Entry<String, Map<String, Integer>> entryValues = (Entry)var5.next();
               JsonObject value = new JsonObject();
               boolean allSkipped = true;

               for(Iterator var9 = ((Map)map.get(entryValues.getKey())).entrySet().iterator(); var9.hasNext(); allSkipped = false) {
                  Entry<String, Integer> valueEntry = (Entry)var9.next();
                  value.addProperty((String)valueEntry.getKey(), (Number)valueEntry.getValue());
               }

               if (!allSkipped) {
                  reallyAllSkipped = false;
                  values.add((String)entryValues.getKey(), value);
               }
            }

            if (reallyAllSkipped) {
               return null;
            } else {
               data.add("values", values);
               return data;
            }
         } else {
            return null;
         }
      }
   }

   public static class AdvancedPie extends Metrics.CustomChart {
      private final Callable<Map<String, Integer>> callable;

      public AdvancedPie(String chartId, Callable<Map<String, Integer>> callable) {
         super(chartId);
         this.callable = callable;
      }

      protected JsonObject getChartData() throws Exception {
         JsonObject data = new JsonObject();
         JsonObject values = new JsonObject();
         Map<String, Integer> map = (Map)this.callable.call();
         if (map != null && !map.isEmpty()) {
            boolean allSkipped = true;
            Iterator var5 = map.entrySet().iterator();

            while(var5.hasNext()) {
               Entry<String, Integer> entry = (Entry)var5.next();
               if ((Integer)entry.getValue() != 0) {
                  allSkipped = false;
                  values.addProperty((String)entry.getKey(), (Number)entry.getValue());
               }
            }

            if (allSkipped) {
               return null;
            } else {
               data.add("values", values);
               return data;
            }
         } else {
            return null;
         }
      }
   }

   public static class SimplePie extends Metrics.CustomChart {
      private final Callable<String> callable;

      public SimplePie(String chartId, Callable<String> callable) {
         super(chartId);
         this.callable = callable;
      }

      protected JsonObject getChartData() throws Exception {
         JsonObject data = new JsonObject();
         String value = (String)this.callable.call();
         if (value != null && !value.isEmpty()) {
            data.addProperty("value", value);
            return data;
         } else {
            return null;
         }
      }
   }

   public abstract static class CustomChart {
      final String chartId;

      CustomChart(String chartId) {
         if (chartId != null && !chartId.isEmpty()) {
            this.chartId = chartId;
         } else {
            throw new IllegalArgumentException("ChartId cannot be null or empty!");
         }
      }

      private JsonObject getRequestJsonObject() {
         JsonObject chart = new JsonObject();
         chart.addProperty("chartId", this.chartId);

         try {
            JsonObject data = this.getChartData();
            if (data == null) {
               return null;
            } else {
               chart.add("data", data);
               return chart;
            }
         } catch (Throwable var3) {
            if (Metrics.logFailedRequests) {
               Bukkit.getLogger().log(Level.WARNING, "Failed to get data for custom chart with id " + this.chartId, var3);
            }

            return null;
         }
      }

      protected abstract JsonObject getChartData() throws Exception;
   }
}
