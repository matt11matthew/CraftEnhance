package craftenhance.libs.menulib.utility.Item;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MetaDataWraper {
   private final Map<String, Object> itemMetaMap = new LinkedHashMap();

   public static MetaDataWraper of() {
      return new MetaDataWraper();
   }

   public MetaDataWraper add(String key, Object value) {
      return this.add(key, value, false);
   }

   public MetaDataWraper add(String key, Object value, boolean keepClazzData) {
      this.itemMetaMap.put(key, keepClazzData ? value + "" : value);
      return this;
   }

   public Map<String, Object> getMetaDataMap() {
      return this.itemMetaMap;
   }
}
