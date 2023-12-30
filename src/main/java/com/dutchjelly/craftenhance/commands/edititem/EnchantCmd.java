package com.dutchjelly.craftenhance.commands.edititem;

import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.commandhandling.ICompletionProvider;
import com.dutchjelly.craftenhance.itemcreation.ItemCreator;
import com.dutchjelly.craftenhance.itemcreation.ParseResult;
import com.dutchjelly.craftenhance.messaging.Messenger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

@CommandRoute(
   cmdPath = {"edititem.enchant"},
   perms = "perms.item-editor"
)
public class EnchantCmd implements ICommand, ICompletionProvider {
   private CustomCmdHandler handler;

   public EnchantCmd(CustomCmdHandler handler) {
      this.handler = handler;
   }

   public String getDescription() {
      return "The enchant command allows users to efficiently enchant items with any enchantment with any level. On use this command will remove all enchantments currently on the held item and enchant it with all specified enchantments. An example of the usage is /edititem enchant protection 10 punch 10 unbreaking 3 fire_protection 4.";
   }

   public void handlePlayerCommand(Player p, String[] args) {
      ItemCreator creator = new ItemCreator(p.getInventory().getItemInHand(), args);
      ParseResult result = creator.enchant();
      p.getInventory().setItemInHand(creator.getItem());
      Messenger.Message(result.getMessage(), p);
   }

   public void handleConsoleCommand(CommandSender sender, String[] args) {
      Messenger.MessageFromConfig("messages.commands.only-for-players", sender);
   }

   public List<String> handleTabCompletion(CommandSender sender, String[] args) {
      List<String> list = new ArrayList();
      if (args.length == 1) {
         list.add("enchant");
      }

      if (args.length >= 2 && args.length % 2 == 0) {
         list.add("clear");
         String var10000 = args[args.length - 1];
         List<Enchantment> enchants = Arrays.asList(Enchantment.values());
         enchants.stream().filter((x) -> {
            return !this.containsEnchant(x.getName().toLowerCase(), args);
         }).collect(Collectors.toList()).forEach((x) -> {
            list.add(x.getName().toLowerCase());
         });
      }

      if (args.length >= 3 && args.length % 2 != 0) {
         list.addAll(Arrays.asList("1", "2", "3", "4", "5"));
      }

      return list;
   }

   public boolean containsEnchant(String enchantName, String[] args) {
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

   public List<String> getCompletions(String[] args) {
      if (args == null) {
         args = new String[0];
      }

      boolean provideEnchantment = args.length % 2 != 0;
      String toComplete = args[args.length - 1];
      if (!provideEnchantment) {
         return Arrays.asList("1", "2", "3", "4", "5");
      } else {
         List<Enchantment> enchants = Arrays.asList(Enchantment.values());
         List<String> completions = new ArrayList();
         enchants.stream().filter((x) -> {
            return x.getName().toLowerCase().startsWith(toComplete.toLowerCase());
         }).collect(Collectors.toList()).forEach((x) -> {
            completions.add(x.getName());
         });
         return completions;
      }
   }
}
