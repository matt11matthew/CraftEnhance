package craftenhance.libs.menulib.utility;

import craftenhance.libs.menulib.MenuMetadataKey;
import craftenhance.libs.menulib.MenuUtility;
import craftenhance.libs.menulib.RegisterMenuAPI;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
;
;

public final class Metadata {
   private static final Plugin plugin = RegisterMenuAPI.getPLUGIN();

   public static boolean hasPlayerMetadata( Player player,  MenuMetadataKey key) {
      return player.hasMetadata(key + "_" + plugin);
   }

   public static List<MetadataValue> getPlayerMenuMetadataList( Player player,  MenuMetadataKey key) {
      return player.getMetadata(key + "_" + plugin);
   }

   
   public static MenuUtility getPlayerMenuMetadata( Player player,  MenuMetadataKey key) {
      List<MetadataValue> playerMetadata = player.getMetadata(key + "_" + plugin);
      return playerMetadata.isEmpty() ? null : (MenuUtility)((MetadataValue)playerMetadata.get(0)).value();
   }

   
   public static Object getPlayerMetadata( Player player,  MenuMetadataKey key) {
      List<MetadataValue> playerMetadata = player.getMetadata(key + "_" + plugin);
      return playerMetadata.isEmpty() ? null : ((MetadataValue)playerMetadata.get(0)).value();
   }

   public static void setPlayerMetadata( Player player,  String key,  Object object) {
      player.setMetadata(key + "_" + plugin, new FixedMetadataValue(plugin, object));
   }

   public static void setPlayerMenuMetadata( Player player,  MenuMetadataKey key,  MenuUtility menu) {
      player.setMetadata(key + "_" + plugin, new FixedMetadataValue(plugin, menu));
   }

   public static void setPlayerLocationMetadata( Player player,  MenuMetadataKey key,  Object location) {
      player.setMetadata(key + "_" + plugin, new FixedMetadataValue(plugin, location));
   }

   public static void removePlayerMenuMetadata( Player player,  MenuMetadataKey key) {
      player.removeMetadata(key + "_" + plugin, plugin);
   }
}
