package craftenhance.libs;

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.plugin.Plugin;

public class RegisterNbtAPI {
   private static Plugin PLUGIN;
   private final CompMetadata compMetadata;

   public RegisterNbtAPI(Plugin plugin, boolean turnOffLogger) {
      PLUGIN = plugin;
      MinecraftVersion.getVersion();
      this.compMetadata = new CompMetadata();
   }

   public void yamlLoad() {
   }

   public CompMetadata getCompMetadata() {
      return this.compMetadata;
   }

   public static Plugin getPlugin() {
      return PLUGIN;
   }
}
