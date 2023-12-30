package craftenhance.libs.menulib.utility.Item;

public class Pair<K, V> {
   private final K firstValue;
   private final V secondValue;

   public Pair(K firstValue, V secondValue) {
      this.firstValue = firstValue;
      this.secondValue = secondValue;
   }

   public K getFirst() {
      return this.firstValue;
   }

   public V getSecond() {
      return this.secondValue;
   }

   public String toString() {
      return this.firstValue + "_" + this.secondValue;
   }
}
