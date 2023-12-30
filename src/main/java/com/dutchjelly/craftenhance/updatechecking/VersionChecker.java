package com.dutchjelly.craftenhance.updatechecking;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.messaging.Messenger;
import java.util.Iterator;

public class VersionChecker {
   private CraftEnhance plugin;
   private String serverVersion;
   private int currentServerVersion;

   public static VersionChecker init(CraftEnhance plugin) {
      VersionChecker checker = new VersionChecker();
      checker.serverVersion = plugin.getServer().getBukkitVersion();
      String version = checker.serverVersion.split("\\.")[1];
      if (version.contains("-")) {
         version = version.substring(0, version.indexOf(45));
      }

      checker.currentServerVersion = Integer.parseInt(version);
      checker.plugin = plugin;
      return checker;
   }

   public void runUpdateCheck() {
      if (this.plugin.getConfig().getBoolean("enable-updatechecker")) {
         GithubLoader loader = GithubLoader.init(this);
         loader.readVersion();
         String version = loader.getVersion();
         if (version != null) {
            version = version.trim();
            String currentVersion = this.versionCheck(version);
            if (!this.isOutDated(currentVersion)) {
               Messenger.Message("CraftEnhance is up to date.");
            } else {
               Messenger.Message("There's a new version (" + currentVersion + ") of the plugin available on https://www.spigotmc.org/resources/1-9-1-19-custom-recipes-and-crafting-craftenhance.65058/ or https://dev.bukkit.org/projects/craftenhance/files.");
            }

         }
      }
   }

   public String versionCheck(String version) {
      String[] currentVersions = version.split("\n");
      return currentVersions[currentVersions.length - 1];
   }

   public boolean runVersionCheck() {
      Messenger.Message("Running a version check to check that the server is compatible with game version " + String.join(", ", Adapter.CompatibleVersions()) + ".");
      Iterator var1 = Adapter.CompatibleVersions().iterator();

      String version;
      do {
         if (!var1.hasNext()) {
            Messenger.Message("");
            Messenger.Message("!! Incompatibility found !!");
            Messenger.Message("The installed version of CraftEnhance only supports spigot/bukkit versions \"" + String.join(", ", Adapter.CompatibleVersions()) + "\"");
            Messenger.Message("while your server is running " + this.serverVersion + ".");
            Messenger.Message("The correct version can be installed here: https://dev.bukkit.org/projects/craftenhance/files");
            Messenger.Message("When installing the plugin make sure that the game version matches your bukkit or spigot version.");
            Messenger.Message("Please note that this incompatibility could cause duping glitches.");
            return false;
         }

         version = (String)var1.next();
      } while(!this.serverVersion.contains(version));

      Messenger.Message("The correct version is installed.");
      return true;
   }

   public CraftEnhance getPlugin() {
      return this.plugin;
   }

   private boolean isOutDated(String version) {
      String currentVersion = this.plugin.getDescription().getVersion();
      return !version.equalsIgnoreCase(currentVersion);
   }

   public boolean equals(VersionChecker.ServerVersion version) {
      return this.serverVersion(version) == 0;
   }

   public boolean newerThan(VersionChecker.ServerVersion version) {
      return this.serverVersion(version) > 0;
   }

   public boolean olderThan(VersionChecker.ServerVersion version) {
      return this.serverVersion(version) < 0;
   }

   public int serverVersion(VersionChecker.ServerVersion version) {
      return this.currentServerVersion - version.getVersion();
   }

   public static enum ServerVersion {
      v1_18(18),
      v1_17(17),
      v1_16(16),
      v1_15(15),
      v1_14(14),
      v1_13(13),
      v1_12(12),
      v1_11(11),
      v1_10(10),
      v1_9(9),
      v1_8(8),
      v1_7(7),
      v1_6(6),
      v1_5(5),
      v1_4(4),
      v1_3_AND_BELOW(3);

      private final int version;

      private ServerVersion(int version) {
         this.version = version;
      }

      public int getVersion() {
         return this.version;
      }
   }
}
