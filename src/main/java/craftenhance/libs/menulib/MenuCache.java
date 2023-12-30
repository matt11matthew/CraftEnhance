package craftenhance.libs.menulib;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nonnull;
import org.bukkit.Location;
;

public enum MenuCache {
   instance;

   private final Map<MenuCacheKey, MenuUtility> menusCached = new HashMap();

   @Nonnull
   MenuCacheKey addToCache(@Nonnull Location location, String key, @Nonnull MenuUtility menu) {
      MenuCacheKey menuCacheKey = new MenuCacheKey(location, key);
      this.menusCached.put(menuCacheKey, menu);
      return menuCacheKey;
   }

   
   public MenuUtility getMenuInCache(Object object) {
      return object instanceof MenuCacheKey ? (MenuUtility)this.menusCached.get(object) : null;
   }

   
   public MenuCacheKey getMenuCacheKey(@Nonnull Location location,  String key) {
      Iterator var3 = this.menusCached.keySet().iterator();

      MenuCacheKey menuCacheKey;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         menuCacheKey = (MenuCacheKey)var3.next();
      } while(!menuCacheKey.equals(location, key));

      return menuCacheKey;
   }

   
   public MenuUtility getMenuInCache(@Nonnull MenuCacheKey key) {
      return (MenuUtility)this.menusCached.get(key);
   }

   public boolean removeMenuCached(Object object) {
      if (object instanceof MenuCacheKey) {
         return this.menusCached.remove(object) != null;
      } else {
         return false;
      }
   }

   public boolean removeMenuCached(@Nonnull MenuCacheKey key) {
      return this.menusCached.remove(key) != null;
   }

   public boolean removeMenuCached(@Nonnull Location location,  String key) {
      Iterator var3 = this.menusCached.keySet().iterator();

      MenuCacheKey menuCacheKey;
      do {
         if (!var3.hasNext()) {
            return false;
         }

         menuCacheKey = (MenuCacheKey)var3.next();
      } while(!menuCacheKey.equals(location, key));

      return this.menusCached.remove(menuCacheKey) != null;
   }

   public Map<Object, MenuUtility> getMenusCached() {
      return Collections.unmodifiableMap(this.menusCached);
   }

   public static MenuCache getInstance() {
      return instance;
   }
}
