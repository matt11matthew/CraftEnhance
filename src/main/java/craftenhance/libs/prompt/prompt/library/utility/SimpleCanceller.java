package craftenhance.libs.prompt.prompt.library.utility;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;

public final class SimpleCanceller implements ConversationCanceller {
   private final List<String> cancelPhrases;

   public SimpleCanceller(String... cancelPhrases) {
      this(Arrays.asList(cancelPhrases));
   }

   public SimpleCanceller(List<String> cancelPhrases) {
      Validate.checkBoolean(cancelPhrases.isEmpty(), "Cancel phrases are empty for conversation cancel listener!");
      this.cancelPhrases = cancelPhrases;
   }

   public void setConversation(@Nonnull Conversation conversation) {
   }

   public boolean cancelBasedOnInput(@Nonnull ConversationContext context, @Nonnull String input) {
      Iterator var3 = this.cancelPhrases.iterator();

      String phrase;
      do {
         if (!var3.hasNext()) {
            return false;
         }

         phrase = (String)var3.next();
      } while(!input.equalsIgnoreCase(phrase));

      return true;
   }

   @Nonnull
   public ConversationCanceller clone() {
      return new SimpleCanceller(this.cancelPhrases);
   }
}
