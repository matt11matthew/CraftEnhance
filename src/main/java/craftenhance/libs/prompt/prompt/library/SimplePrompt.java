package craftenhance.libs.prompt.prompt.library;

import craftenhance.libs.prompt.prompt.library.utility.Validate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public abstract class SimplePrompt extends ValidatingPrompt implements Cloneable {
   private Player player;
   private final Plugin plugin;

   protected abstract String getPrompt(ConversationContext var1);

   public SimplePrompt() {
      this((Plugin)null);
   }

   public SimplePrompt(@Nullable Plugin plugin) {
      this.player = null;
      this.plugin = plugin;
   }

   @Nonnull
   public final String getPromptText(@Nonnull ConversationContext context) {
      return this.getPrompt(context);
   }

   protected boolean isInputValid(@Nonnull ConversationContext context, @Nullable String input) {
      return true;
   }

   public final SimpleConversation start(@Nonnull Player player) {
      this.player = player;
      Validate.checkNotNull(this.plugin, "Please provide a plugin instance before using this method.");
      SimpleConversation conversation = new SimpleConversation(this.plugin) {
         public Prompt getFirstPrompt() {
            return SimplePrompt.this;
         }
      };
      conversation.start(player);
      return conversation;
   }

   public void onConversationEnd(SimpleConversation conversation, ConversationAbandonedEvent event) {
   }

   protected final Player getPlayer(@Nonnull ConversationContext ctx) {
      Validate.checkBoolean(!(ctx.getForWhom() instanceof Player), "Conversable is not a player but: " + ctx.getForWhom());
      return (Player)ctx.getForWhom();
   }

   @Nullable
   protected final Player getPlayer() {
      return this.player;
   }

   @Nullable
   public final Prompt acceptInput(@Nonnull ConversationContext context, String input) {
      if (this.isInputValid(context, input)) {
         return this.acceptValidatedInput(context, input);
      } else {
         this.getFailedValidationText(context, input);
         return this;
      }
   }

   public SimplePrompt clone() {
      try {
         return (SimplePrompt)super.clone();
      } catch (CloneNotSupportedException var2) {
         throw new AssertionError();
      }
   }
}
