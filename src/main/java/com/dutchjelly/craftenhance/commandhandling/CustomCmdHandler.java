package com.dutchjelly.craftenhance.commandhandling;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class CustomCmdHandler implements TabCompleter {
   private Map<ICommand, CommandRoute> commandClasses = new HashMap();
   private CraftEnhance main;

   public CustomCmdHandler(CraftEnhance main) {
      this.main = main;
      main.getDescription().getCommands().keySet().forEach((x) -> {
         main.getCommand(x).setTabCompleter(this);
      });
   }

   public void loadCommandClasses(List<ICommand> baseClasses) {
      if (baseClasses != null) {
         baseClasses.forEach((x) -> {
            this.loadCommandClass(x);
         });
      }
   }

   public void loadCommandClass(ICommand baseClass) {
      if (baseClass != null) {
         CommandRoute annotation = null;
         Annotation[] var3 = baseClass.getClass().getAnnotations();
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Annotation annotationItem = var3[var5];
            if (annotationItem instanceof CommandRoute) {
               annotation = (CommandRoute)annotationItem;
               break;
            }
         }

         if (annotation == null) {
            Debug.Send((Object)("Could not load the commandclass " + baseClass.getClass().getName()));
         } else {
            this.commandClasses.put(baseClass, annotation);
         }
      }
   }

   public CraftEnhance getMain() {
      return this.main;
   }

   public boolean handleCommand(CommandSender sender, String label, String[] args) {
      args = this.pushLabelArg(label, args);
      ICommand executor = this.getExecutor(args);
      if (executor == null) {
         this.sendOptions(args, sender);
         return true;
      } else {
         CommandRoute annotation = (CommandRoute)this.commandClasses.get(executor);
         if (!this.hasPermission(sender, annotation)) {
            Messenger.MessageFromConfig("messages.global.no-perms", sender);
            return true;
         } else {
            String[] commandLabels = this.getMatchingPath(args, annotation).split("\\.");
            args = this.popArguments(commandLabels.length, args);
            if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
               Messenger.Message(executor.getDescription(), sender);
               return true;
            } else {
               if (sender instanceof Player) {
                  executor.handlePlayerCommand((Player)sender, args);
               } else {
                  executor.handleConsoleCommand(sender, args);
               }

               return true;
            }
         }
      }
   }

   private void sendOptions(String[] args, CommandSender sender) {
      String[] emptyLast = new String[args.length + 1];

      for(int i = 0; i < args.length; ++i) {
         emptyLast[i] = args[i];
      }

      emptyLast[args.length] = "";
      String completions = String.join(", ", this.getTabCompleteMatches(emptyLast));
      if (completions.equals("")) {
         Messenger.Message("That is not a command.", sender);
      } else {
         Messenger.MessageFromConfig("messages.commands.show-options", sender, completions);
      }
   }

   private String[] pushLabelArg(String label, String[] args) {
      String[] pushed = new String[args.length + 1];

      for(int i = 1; i < pushed.length; ++i) {
         pushed[i] = args[i - 1];
      }

      pushed[0] = label;
      return pushed;
   }

   public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
      this.pushLabelArg(label, args);
      List<String> tabCompletion = new ArrayList();
      ICommand executor = this.getExecutor(label, args.length > 0 ? args[0] : "");
      if (executor != null) {
         List<String> handleTabCompletion = executor.handleTabCompletion(sender, args);
         if (handleTabCompletion != null) {
            CommandRoute annotation = (CommandRoute)this.commandClasses.get(executor);
            if (this.hasPermission(sender, annotation)) {
               return handleTabCompletion;
            }
         }
      }

      tabCompletion.addAll(this.getTabCompleteMatches(this.pushLabelArg(label, args)));
      return tabCompletion;
   }

   private String[] popArguments(int amount, String[] args) {
      if (amount != 0 && amount <= args.length && args.length >= 1) {
         String[] popped = new String[args.length - 1];

         for(int i = 0; i < args.length - 1; ++i) {
            popped[i] = args[i + 1];
         }

         return this.popArguments(amount - 1, popped);
      } else {
         return args;
      }
   }

   public ICommand getExecutor(String... args) {
      int maxMatching = -1;
      ICommand bestMatch = null;
      Iterator var5 = this.commandClasses.keySet().iterator();

      while(var5.hasNext()) {
         ICommand cmdClass = (ICommand)var5.next();
         CommandRoute annotation = (CommandRoute)this.commandClasses.get(cmdClass);
         String matchingPath = this.getMatchingPath(args, annotation);
         if (matchingPath != null) {
            int currentMatch = matchingPath.split("\\.").length;
            if (currentMatch > maxMatching) {
               maxMatching = currentMatch;
               bestMatch = cmdClass;
            }
         }
      }

      return bestMatch;
   }

   private String getMatchingPath(String[] args, CommandRoute annotation) {
      int bestMatch = -1;
      String bestMatchingPath = null;
      String[] var6 = annotation.cmdPath();
      int var7 = var6.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         String path = var6[var8];
         int currentMatch = this.cmdPathMatches(args, path);
         if (currentMatch > bestMatch) {
            bestMatch = currentMatch;
            bestMatchingPath = path;
         }
      }

      return bestMatchingPath;
   }

   private int cmdPathMatches(String[] args, String path) {
      String[] splitPath = path.split("\\.");
      if (splitPath.length > args.length) {
         return -1;
      } else {
         for(int i = 0; i < splitPath.length; ++i) {
            if (!args[i].equalsIgnoreCase(splitPath[i])) {
               return -1;
            }
         }

         return splitPath.length;
      }
   }

   private boolean hasPermission(CommandSender sender, CommandRoute annotation) {
      if (annotation.perms() != null && !annotation.perms().equals("")) {
         String perms = this.main.getConfig().getString(annotation.perms()) + "";
         return perms.trim().equals("") ? true : sender.hasPermission(perms);
      } else {
         return true;
      }
   }

   private List<String> getTabCompleteMatches(String[] args) {
      List<String> completions = new ArrayList();
      Iterator var4 = this.commandClasses.keySet().iterator();

      while(var4.hasNext()) {
         ICommand cmdClass = (ICommand)var4.next();
         CommandRoute annotation = (CommandRoute)this.commandClasses.get(cmdClass);
         String[] var7 = annotation.cmdPath();
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            String path = var7[var9];
            String completion = this.getCompletion(args, path);
            if (completion != null) {
               completions.add(completion);
            }
         }
      }

      return completions;
   }

   private String getCompletion(String[] args, String path) {
      String[] splitPath = path.split("\\.");
      if (args.length > splitPath.length) {
         return null;
      } else {
         for(int i = 0; i < args.length - 1; ++i) {
            if (!args[i].equalsIgnoreCase(splitPath[i])) {
               return null;
            }
         }

         if (!splitPath[args.length - 1].toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
            return null;
         } else {
            return splitPath[args.length - 1];
         }
      }
   }
}
