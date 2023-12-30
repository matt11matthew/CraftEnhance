package craftenhance.libs.prompt.prompt.library.utility;

import javax.annotation.Nonnull;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationPrefix;

public final class SimplePrefix implements ConversationPrefix {
   private final String prefix;

   public SimplePrefix(String prefix) {
      this.prefix = prefix;
   }

   @Nonnull
   public String getPrefix(@Nonnull ConversationContext context) {
      return this.prefix;
   }
}
