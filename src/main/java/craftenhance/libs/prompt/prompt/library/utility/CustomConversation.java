package craftenhance.libs.prompt.prompt.library.utility;

import craftenhance.libs.prompt.prompt.library.SimpleConversation;
import craftenhance.libs.prompt.prompt.library.SimplePrompt;
import javax.annotation.Nonnull;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.plugin.Plugin;

public class CustomConversation extends Conversation {
   private SimplePrompt lastSimplePrompt;
   private final SimpleConversation simpleConversatin;

   public CustomConversation(@Nonnull Plugin plugin, @Nonnull SimpleConversation simpleConversation, @Nonnull Conversable forWhom) {
      super(plugin, forWhom, simpleConversation.getFirstPrompt());
      this.localEchoEnabled = false;
      this.simpleConversatin = simpleConversation;
      if (simpleConversation.insertPrefix() && simpleConversation.getPrefix() != null) {
         this.prefix = simpleConversation.getPrefix();
      }

   }

   public SimplePrompt getLastSimplePrompt() {
      return this.lastSimplePrompt;
   }

   public void outputNextPrompt() {
      if (this.currentPrompt == null) {
         this.abandon(new ConversationAbandonedEvent(this));
      } else {
         String promptClass = this.currentPrompt.getClass().getSimpleName();
         String question = this.currentPrompt.getPromptText(this.context);

         try {
            Object askedQuestions = this.context.getAllSessionData().getOrDefault("Asked_" + promptClass, this.simpleConversatin.getTimeout());
            if (!askedQuestions.equals(question)) {
               this.context.setSessionData("Asked_" + promptClass, askedQuestions);
               this.context.getForWhom().sendRawMessage(this.prefix.getPrefix(this.context) + question);
            }
         } catch (NoSuchMethodError var4) {
         }

         if (this.currentPrompt instanceof SimplePrompt) {
            this.lastSimplePrompt = ((SimplePrompt)this.currentPrompt).clone();
         }

         if (!this.currentPrompt.blocksForInput(this.context)) {
            this.currentPrompt = this.currentPrompt.acceptInput(this.context, (String)null);
            this.outputNextPrompt();
         }
      }

   }
}
