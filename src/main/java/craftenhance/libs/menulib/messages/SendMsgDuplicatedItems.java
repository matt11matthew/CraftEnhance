package craftenhance.libs.menulib.messages;

import craftenhance.libs.menulib.dependencies.rbglib.TextTranslator;
import org.bukkit.entity.Player;

public class SendMsgDuplicatedItems {
   private static String blacklistMessage;
   private static String dublicatedMessage;

   public static void setBlacklistMessage(String blacklistMessage) {
      SendMsgDuplicatedItems.blacklistMessage = blacklistMessage;
   }

   public static void setDublicatedMessage(String dublicatedMessage) {
      SendMsgDuplicatedItems.dublicatedMessage = dublicatedMessage;
   }

   public static void sendMessage(Player player, String msg) {
      player.sendMessage(msg);
   }

   public static void sendBlacklistMessage(Player player, Object... placeholders) {
      String message;
      if (blacklistMessage == null) {
         message = "&fthis item&6 {0}&f are blacklisted";
      } else {
         message = blacklistMessage;
      }

      player.sendMessage(TextTranslator.toSpigotFormat(translatePlaceholders(message, placeholders)));
   }

   public static void sendDublicatedMessage(Player player, Object... placeholders) {
      String message;
      if (dublicatedMessage == null) {
         message = "&fYou can't add more than one &6 {0} &ftype, You have added &4{1}&f extra itemstack. You get back &6 {2}&f items.";
      } else {
         message = dublicatedMessage;
      }

      player.sendMessage(TextTranslator.toSpigotFormat(translatePlaceholders(message, placeholders)));
   }

   public static String translatePlaceholders(String rawText, Object... placeholders) {
      for(int i = 0; i < placeholders.length; ++i) {
         rawText = rawText.replace("{" + i + "}", placeholders[i] != null ? placeholders[i].toString() : "");
      }

      return rawText;
   }
}
