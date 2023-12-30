package craftenhance.libs.prompt.prompt.library.utility;

import javax.annotation.Nonnull;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.InactivityConversationCanceller;
import org.bukkit.plugin.Plugin;

public final class CustomCanceller extends InactivityConversationCanceller {
   public CustomCanceller(@Nonnull Plugin plugin, int timeoutSeconds) {
      super(plugin, timeoutSeconds);
   }

   protected void cancelling(Conversation conversation) {
      conversation.getContext().setSessionData("FLP#TIMEOUT", true);
   }
}
