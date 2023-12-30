package com.dutchjelly.craftenhance.commandhandling;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface ICommand {
   String getDescription();

   void handlePlayerCommand(Player var1, String[] var2);

   void handleConsoleCommand(CommandSender var1, String[] var2);

   default List<String> handleTabCompletion(CommandSender sender, String[] args) {
      return null;
   }
}
