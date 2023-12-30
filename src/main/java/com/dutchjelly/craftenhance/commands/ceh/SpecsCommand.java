package com.dutchjelly.craftenhance.commands.ceh;

import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandRoute(
   cmdPath = {"ceh.specs"},
   perms = "perms.recipe-editor"
)
public class SpecsCommand implements ICommand {
   private CustomCmdHandler handler;

   public SpecsCommand(CustomCmdHandler handler) {
      this.handler = handler;
   }

   public String getDescription() {
      return "The view command opens an inventory that contains all available recipes for the sender of the command, unless it's configured to show all. The usage is /ceh view or /recipes";
   }

   public void handlePlayerCommand(Player p, String[] args) {
      if (args.length != 1) {
         Messenger.MessageFromConfig("messages.commands.few-arguments", p, "1");
      } else {
         EnhancedRecipe recipe = this.handler.getMain().getFm().getRecipe(args[0]);
         if (recipe == null) {
            Messenger.Message("That recipe key doesn't exist", p);
         } else {
            Messenger.Message("&fKey: &e" + recipe.getKey() + " &fPerms: &e" + recipe.getPermissions(), p);
         }
      }
   }

   public void handleConsoleCommand(CommandSender sender, String[] args) {
      Messenger.MessageFromConfig("messages.commands.only-for-players", sender);
   }
}
