package com.dutchjelly.craftenhance.commands.ceh;

import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.RecipeType;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.files.CategoryData;
import com.dutchjelly.craftenhance.gui.guis.EditorTypeSelector;
import com.dutchjelly.craftenhance.gui.guis.editors.RecipeEditor;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandRoute(
   cmdPath = {"ceh.createrecipe"},
   perms = "perms.recipe-editor"
)
public class CreateRecipeCmd implements ICommand {
   private CustomCmdHandler handler;

   public CreateRecipeCmd(CustomCmdHandler handler) {
      this.handler = handler;
   }

   public String getDescription() {
      return "The create recipe command allows users to create a recipe and open the editor of it. The usage is /ceh createrecipe [key] [permission]. You can leave both parameters empty. However, if you do want to customise recipe keys and permissions: the key has to be unique, and the permission can be empty to not have any permission. An example: /ceh createrecipe army_chest ceh.army-chest. The now created recipe has a key of army_chest and a permission of ceh.army-chest.";
   }

   public void handlePlayerCommand(Player p, String[] args) {
      if (args.length != 0) {
         if (args.length == 1) {
            args = this.addEmptyString(args);
         } else if (args.length != 2) {
            Messenger.MessageFromConfig("messages.commands.few-arguments", p, "2");
            return;
         }

         if (!this.handler.getMain().getFm().isUniqueRecipeKey(args[0])) {
            Messenger.Message("The specified recipe key isn't unique.", p);
         } else {
            WBRecipe newRecipe = new WBRecipe(args[1], (ItemStack)null, new ItemStack[9]);
            ButtonType buttonType = ButtonType.ChooseWorkbenchType;
            if (newRecipe.getType() == RecipeType.FURNACE) {
               buttonType = ButtonType.ChooseFurnaceType;
               newRecipe = new WBRecipe(args[1], (ItemStack)null, new ItemStack[1]);
            }

            newRecipe.setKey(args[0]);
            RecipeEditor<EnhancedRecipe> menu = new RecipeEditor(newRecipe, (CategoryData)null, (String)null, buttonType);
            menu.menuOpen(p);
         }
      } else {
         int uniqueKeyIndex;
         for(uniqueKeyIndex = 1; !this.handler.getMain().getFm().isUniqueRecipeKey("recipe" + uniqueKeyIndex); ++uniqueKeyIndex) {
         }

         WBRecipe newRecipe = new WBRecipe((String)null, (ItemStack)null, new ItemStack[9]);
         newRecipe.setKey("recipe" + uniqueKeyIndex);
         EditorTypeSelector guis = new EditorTypeSelector("recipe" + uniqueKeyIndex, (String)null);
         guis.menuOpen(p);
      }
   }

   public void handleConsoleCommand(CommandSender sender, String[] args) {
      Messenger.MessageFromConfig("messages.commands.only-for-players", sender);
   }

   private String[] addEmptyString(String[] args) {
      String[] newArray = new String[args.length + 1];
      newArray[0] = args[0];
      newArray[1] = "";
      return newArray;
   }
}
