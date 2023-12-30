package com.dutchjelly.craftenhance.gui.util;

public enum ButtonType {
   NxtPage(""),
   PrvPage(""),
   Back(""),
   SaveRecipe(""),
   DeleteRecipe(""),
   SwitchShaped(""),
   SwitchMatchMeta(""),
   ResetRecipe(""),
   SetPosition(""),
   SwitchHidden(""),
   SetPermission(""),
   SwitchDisablerMode(""),
   SetCookTime(""),
   SetExp(""),
   ChooseWorkbenchType("WBRecipeEditor"),
   ChooseFurnaceType("FurnaceRecipeEditor"),
   Search(""),
   NewCategory(""),
   ChangeCategoryName(""),
   ChangeCategoryList(""),
   ChangeCategory(""),
   ChangeCategoryItem(""),
   RemoveCategory(""),
   FillItems("");

   private String type;

   private ButtonType(String type) {
      this.type = type;
   }

   public String getType() {
      return this.type;
   }

   public static ButtonType valueOfType(String buttontype) {
      ButtonType[] buttonTypes = values();
      ButtonType[] var2 = buttonTypes;
      int var3 = buttonTypes.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         ButtonType buttonType = var2[var4];
         if (buttonType.name().equalsIgnoreCase(buttontype)) {
            return buttonType;
         }
      }

      return null;
   }
}
