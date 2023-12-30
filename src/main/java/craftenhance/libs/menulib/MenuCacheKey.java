package craftenhance.libs.menulib;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Location;

public final class MenuCacheKey {
   private final Location location;
   private final String key;

   public MenuCacheKey(@Nonnull Location location, @Nullable String key) {
      this.location = location;
      this.key = key != null && !key.isEmpty() ? key : null;
   }

   @Nonnull
   public Location getLocation() {
      return this.location;
   }

   @Nullable
   public String getKey() {
      return this.key;
   }

   public boolean equals(@Nonnull Location location, @Nullable String key) {
      if (this.getKey() == null) {
         return key != null ? false : this.getLocation().equals(location);
      } else {
         return this.getLocation().equals(location) && this.getKey().equals(key);
      }
   }

   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof MenuCacheKey)) {
         return false;
      } else {
         MenuCacheKey other = (MenuCacheKey)obj;
         return this.location.equals(other.location) && Objects.equals(this.key, other.key);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.location, this.key});
   }

   public String toString() {
      return this.key != null ? this.key + ":" + this.location : this.location + "";
   }
}
