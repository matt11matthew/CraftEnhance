package com.dutchjelly.craftenhance.crafthandling.recipes;

public enum MatchType {
   META("match meta"),
   MATERIAL("only match type"),
   NAME("only match name and type");

   private String description;

   private MatchType(String s) {
      this.description = s;
   }

   public String getDescription() {
      return this.description;
   }
}
