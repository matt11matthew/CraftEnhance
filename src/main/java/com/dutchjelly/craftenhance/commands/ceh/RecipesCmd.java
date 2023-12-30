package com.dutchjelly.craftenhance.commands.ceh;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.files.CategoryData;
import com.dutchjelly.craftenhance.gui.guis.RecipesViewer;
import com.dutchjelly.craftenhance.gui.guis.RecipesViewerCategorys;
import com.dutchjelly.craftenhance.messaging.Messenger;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandRoute(
   cmdPath = {"recipes", "ceh.viewer"},
   perms = "perms.recipe-viewer"
)
public class RecipesCmd implements ICommand {
   private final CustomCmdHandler handler;

   public RecipesCmd(CustomCmdHandler handler) {
      this.handler = handler;
   }

   public String getDescription() {
      return "The view command opens an inventory that contains all available recipes for the sender of the command, unless it's configured to show all. The usage is /ceh view or /recipes";
   }

   public void handlePlayerCommand(Player p, String[] args) {
      RecipesViewerCategorys menu = new RecipesViewerCategorys("");
      if (args.length > 1) {
         if (args[0].equals("page")) {
            try {
               int pageIndex = Integer.parseInt(args[1]);
               boolean b = menu.setPage(pageIndex);
               if (!b) {
                  p.sendMessage("Could not open this page " + args[1] + " , will open first page.");
               }
            } catch (NumberFormatException var6) {
               p.sendMessage("that's not a number " + args[1]);
            }
         }

         if (args[0].equals("category")) {
            CategoryData categoryData = CraftEnhance.self().getCategoryDataCache().get(args[1]);
            if (categoryData != null) {
               (new RecipesViewer(categoryData, "", p)).menuOpen(p);
            } else {
               p.sendMessage("that's not a valid category " + args[1]);
            }

            return;
         }
      }

      menu.menuOpen(p);
   }

   public List<String> handleTabCompletion(CommandSender sender, String[] args) {
      List<String> list = new ArrayList();
      if (args.length == 2) {
         list.add("page");
         list.add("category");
      }

      if (args.length == 3 && args[1].contains("category")) {
         list.addAll(CraftEnhance.self().getCategoryDataCache().getCategoryNames());
      }

      return list;
   }

   public void handleConsoleCommand(CommandSender sender, String[] args) {
      Messenger.MessageFromConfig("messages.commands.only-for-players", sender);
   }
}
