package com.dutchjelly.craftenhance.commands.edititem;

import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.itemcreation.ItemCreator;
import com.dutchjelly.craftenhance.itemcreation.ParseResult;
import com.dutchjelly.craftenhance.messaging.Messenger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

@CommandRoute(
   cmdPath = {"edititem.itemflag"},
   perms = "perms.item-editor"
)
public class ItemFlagCmd implements ICommand {
   private final CustomCmdHandler handler;

   public ItemFlagCmd(CustomCmdHandler handler) {
      this.handler = handler;
   }

   public String getDescription() {
      return "The itemflag command allows users to toggle itemflags of the held item. An example of the usage is /edititem itemflag hide_enchants hide_attributes. These itemflag names are documented in the bukkit documentation, google \"itemflags bukkit\", and the first result should contain a list of all itemflags and what they do.";
   }

   public void handlePlayerCommand(Player p, String[] args) {
      ItemCreator creator = new ItemCreator(p.getInventory().getItemInHand(), args);
      ParseResult result = creator.setItemFlags();
      p.getInventory().setItemInHand(creator.getItem());
      Messenger.Message(result.getMessage(), p);
   }

   public void handleConsoleCommand(CommandSender sender, String[] args) {
      Messenger.MessageFromConfig("messages.commands.only-for-players", sender);
   }

   public List<String> handleTabCompletion(CommandSender sender, String[] args) {
      List<String> list = new ArrayList();
      if (args.length == 1) {
         list.add("itemflag");
      }

      if (args.length >= 2) {
         String var10000 = args[args.length - 1];
         List<ItemFlag> enchants = Arrays.asList(ItemFlag.values());
         enchants.stream().filter((x) -> {
            return !this.containsFlag(x.name().toLowerCase(), args);
         }).collect(Collectors.toList()).forEach((x) -> {
            list.add(x.name().toLowerCase());
         });
      }

      return list;
   }

   public boolean containsFlag(String enchantName, String[] args) {
      String[] var3 = args;
      int var4 = args.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String arg = var3[var5];
         if (arg.toLowerCase().startsWith(enchantName)) {
            return true;
         }
      }

      return false;
   }
}
