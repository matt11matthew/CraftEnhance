package craftenhance.libs;

import java.util.Iterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.dutchjelly.craftenhance.files.util.SimpleYamlHelper;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public final class CompMetadata {
   private static final String DELIMITER = "%-%";
   private static final Plugin plugin = RegisterNbtAPI.getPlugin();

   public ItemStack setMetadata(@Nonnull ItemStack item, @Nonnull String key, @Nonnull Object value) {
      NBTItem nbt = new NBTItem(item);
      NBTCompound tag = nbt.addCompound(this.getCompoundKey());
      if (value instanceof String) {
         tag.setString(key, (String)value);
      } else {
         tag.setObject(key, value);
      }

      return nbt.getItem();
   }

   public void setMetadata(@Nonnull Entity entity, @Nonnull String tag) {
      this.setMetadata(entity, tag, tag);
   }

   public void setMetadata(@Nonnull Entity entity, @Nonnull String key, @Nonnull String value) {
      this.format(key, value);
      entity.setMetadata(key, new FixedMetadataValue(plugin, value));
      MetadataFile.getInstance().addMetadata(entity, key, value);
   }

   private String format(String key, String value) {
      return plugin.getName() + "%-%" + key + "%-%" + value;
   }

   public void setMetadata(@Nonnull BlockState tileEntity, @Nonnull String key, @Nonnull String value) {
      if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_14_R1)) {
         this.setNamedspaced((TileState)tileEntity, key, value);
         tileEntity.update();
      } else {
         tileEntity.setMetadata(key, new FixedMetadataValue(plugin, value));
         tileEntity.update();
         MetadataFile.getInstance().addMetadata(tileEntity, key, value);
      }

   }

   public void setTempMetadata(@Nonnull Entity entity, @Nonnull String tag) {
      entity.setMetadata(this.createTempMetadataKey(tag), new FixedMetadataValue(plugin, tag));
   }

   public void setTempMetadata(@Nonnull Entity entity, @Nonnull String tag, @Nonnull Object key) {
      entity.setMetadata(this.createTempMetadataKey(tag), new FixedMetadataValue(plugin, key));
   }

   private void setNamedspaced(TileState tile, String key, String value) {
      tile.getPersistentDataContainer().set(new NamespacedKey(plugin, key), PersistentDataType.STRING, value);
   }

   @Nullable
   public String getMetadata(@Nonnull ItemStack item, @Nonnull String key) {
      if (item.getType() == Material.AIR) {
         return null;
      } else {
         String compoundTag = this.getCompoundKey();
         NBTItem nbt = new NBTItem(item);
         String value = nbt.hasKey(compoundTag) ? nbt.getCompound(compoundTag).getString(key) : null;
         return getOrNull(value);
      }
   }

   @Nullable
   public <T> T getMetadata(@Nonnull ItemStack item, @Nonnull Class<T> clazz, @Nonnull String key) {
      if (item.getType() == Material.AIR) {
         return null;
      } else {
         String compoundTag = this.getCompoundKey();
         NBTItem nbt = new NBTItem(item);
         T value = nbt.hasKey(compoundTag) ? nbt.getCompound(compoundTag).getObject(key, clazz) : null;
         return getOrNull(value);
      }
   }

   @Nullable
   public String getMetadata(@Nonnull Entity entity, @Nonnull String key) {
      String value = entity.hasMetadata(key) ? ((MetadataValue)entity.getMetadata(key).get(0)).asString() : null;
      return getOrNull(value);
   }

   private String getTag(String raw, String key) {
      String[] parts = raw.split("%-%");
      return parts.length == 3 && parts[0].equals(plugin.getName()) && parts[1].equals(key) ? parts[2] : null;
   }

   @Nullable
   public String getMetadata(@Nonnull BlockState tileEntity, @Nonnull String key) {
      return getNamedspaced((TileState)tileEntity, key);
   }

   @Nullable
   public MetadataValue getTempMetadata(@Nonnull Entity entity, @Nonnull String tag) {
      String key = this.createTempMetadataKey(tag);
      return entity.hasMetadata(key) ? (MetadataValue)entity.getMetadata(key).get(0) : null;
   }

   private static String getNamedspaced(TileState tile, String key) {
      String value = (String)tile.getPersistentDataContainer().get(new NamespacedKey(plugin, key), PersistentDataType.STRING);
      return getOrNull(value);
   }

   public boolean hasMetadata(@Nonnull ItemStack item, @Nonnull String key) {
      if (item.getType() == Material.AIR) {
         return false;
      } else {
         NBTItem nbt = new NBTItem(item);
         NBTCompound tag = nbt.getCompound(this.getCompoundKey());
         return tag != null && tag.hasKey(key);
      }
   }

   public boolean hasMetadata(Entity entity, String key) {
      Iterator var3 = entity.getScoreboardTags().iterator();

      String line;
      do {
         if (!var3.hasNext()) {
            return entity.hasMetadata(key);
         }

         line = (String)var3.next();
      } while(!this.hasTag(line, key));

      return true;
   }

   public boolean hasMetadata(BlockState tileEntity, String key) {
      return this.hasNamedspaced((TileState)tileEntity, key);
   }

   public boolean hasTempMetadata(Entity player, String tag) {
      return player.hasMetadata(this.createTempMetadataKey(tag));
   }

   private boolean hasNamedspaced(TileState tile, String key) {
      return tile.getPersistentDataContainer().has(new NamespacedKey(plugin, key), PersistentDataType.STRING);
   }

   private boolean hasTag(String raw, String tag) {
      String[] parts = raw.split("%-%");
      return parts.length == 3 && parts[0].equals(plugin.getName()) && parts[1].equals(tag);
   }

   public void removeTempMetadata(Entity player, String tag) {
      String key = this.createTempMetadataKey(tag);
      if (player.hasMetadata(key)) {
         player.removeMetadata(key, plugin);
      }

   }

   private String createTempMetadataKey(String tag) {
      return plugin.getName() + "_" + tag;
   }

   public String getCompoundKey() {
      return plugin.getName() + "_NBT";
   }

   public static String getOrNull(String input) {
      return input != null && !"none".equalsIgnoreCase(input) && !input.isEmpty() ? input : null;
   }

   public static <T> T getOrNull(T input) {
      return input != null && !"none".equalsIgnoreCase(input.toString()) && !input.toString().isEmpty() ? input : null;
   }

   public static final class MetadataFile {
      private static volatile Object LOCK = new Object();
      private static final MetadataFile instance = new MetadataFile();

      public static MetadataFile getInstance() {
         return instance;
      }

      public void addMetadata(BlockState tileEntity, String key, String value) {
      }

      public void addMetadata(Entity tileEntity, String key, String value) {
      }
   }
}
