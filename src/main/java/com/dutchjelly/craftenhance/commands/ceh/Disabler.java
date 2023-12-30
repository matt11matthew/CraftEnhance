package com.dutchjelly.craftenhance.commands.ceh;

import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.crafthandling.RecipeLoader;
import com.dutchjelly.craftenhance.gui.guis.RecipeDisabler;
import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandRoute(
   cmdPath = {"ceh.disabler", "ceh.disable"},
   perms = "perms.recipe-editor"
)
public class Disabler implements ICommand {
   private CustomCmdHandler handler;

   public Disabler(CustomCmdHandler handler) {
      this.handler = handler;
   }

   public String getDescription() {
      return "Use the command to open a gui in which you can disable/enable server recipes.";
   }

   public void handlePlayerCommand(Player p, String[] args) {
      RecipeDisabler menu = new RecipeDisabler(RecipeLoader.getInstance().getServerRecipes(), RecipeLoader.getInstance().getDisabledServerRecipes(), false, "");
      menu.menuOpen(p);
      if (args.length == 1) {
         try {
            int var4 = Integer.valueOf(args[0]);
         } catch (NumberFormatException var5) {
            p.sendMessage("that's not a number");
         }
      }

   }

   public void handleConsoleCommand(CommandSender sender, String[] args) {
      Messenger.MessageFromConfig("messages.commands.only-for-players", sender);
   }
}
