package com.dutchjelly.craftenhance.util;

import com.dutchjelly.craftenhance.CraftEnhance;

public enum PermissionTypes {
   Edit("perms.recipe-editor"),
   View("perms.recipe-viewer"),
   EditItem("perms.edit-item"),
   Categorys_editor("perms.categorys-editor");

   public final String permPath;

   private PermissionTypes(String permPath) {
      this.permPath = permPath;
   }

   public String getPerm() {
      return CraftEnhance.self().getConfig().getString(this.permPath);
   }
}
