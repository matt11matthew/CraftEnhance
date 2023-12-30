package com.dutchjelly.craftenhance.util;

public class Pair<T, G> {
   private T first;
   private G second;

   public Pair(T first, G second) {
      this.first = first;
      this.second = second;
   }

   public T getFirst() {
      return this.first;
   }

   public void setFirst(T first) {
      this.first = first;
   }

   public G getSecond() {
      return this.second;
   }

   public void setSecond(G second) {
      this.second = second;
   }
}
