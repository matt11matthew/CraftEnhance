package com.dutchjelly.craftenhance.commands.edititem;

import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandRoute(
   cmdPath = {"edititem.localizedname"},
   perms = "perms.item-editor"
)
public class LocalizedNameCmd implements ICommand {
   private CustomCmdHandler handler;

   public LocalizedNameCmd(CustomCmdHandler handler) {
      this.handler = handler;
   }

   public String getDescription() {
      return "The localizedname command is used to edit the name that is stored server-sided, which means that the in-game users can't see it. However, some plugins may use this itemstack property. An example of the usage is /edititem localizedname example name.";
   }

   public void handlePlayerCommand(Player p, String[] args) {
      Messenger.Message("This command is not supported in this version of the plugin.");
   }

   public void handleConsoleCommand(CommandSender sender, String[] args) {
      Messenger.MessageFromConfig("messages.commands.only-for-players", sender);
   }
}
