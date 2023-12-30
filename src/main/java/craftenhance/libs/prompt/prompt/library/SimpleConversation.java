package craftenhance.libs.prompt.prompt.library;

import craftenhance.libs.prompt.prompt.library.utility.CustomCanceller;
import craftenhance.libs.prompt.prompt.library.utility.CustomConversation;
import craftenhance.libs.prompt.prompt.library.utility.SimpleCanceller;
import craftenhance.libs.prompt.prompt.library.utility.SimplePrefix;
import craftenhance.libs.prompt.prompt.library.utility.Validate;
import javax.annotation.Nonnull;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public abstract class SimpleConversation implements ConversationAbandonedListener {
   private String prefix;
   private int timeout = 60;
   private final Plugin plugin;

   public SimpleConversation(Plugin plugin) {
      this.plugin = plugin;
   }

   public abstract Prompt getFirstPrompt();

   public final void start(@Nonnull Player player) {
      Validate.checkBoolean(player.isConversing(), "Player " + player.getName() + " is already conversing!");
      player.closeInventory();
      CustomConversation conversation = new CustomConversation(this.plugin, this, player);
      CustomCanceller canceller = new CustomCanceller(this.plugin, this.timeout);
      canceller.setConversation(conversation);
      conversation.getCancellers().add(canceller);
      conversation.getCancellers().add(this.getCanceller());
      conversation.addConversationAbandonedListener(this);
      conversation.begin();
   }

   protected ConversationCanceller getCanceller() {
      return new SimpleCanceller(new String[]{"quit", "cancel", "exit"});
   }

   public void conversationAbandoned(@Nonnull ConversationAbandonedEvent event) {
      ConversationContext context = event.getContext();
      Conversable conversing = context.getForWhom();
      Object source = event.getSource();
      boolean timeout = (Boolean)context.getAllSessionData().getOrDefault("FLP#TIMEOUT", false);
      context.getAllSessionData().remove("FLP#TIMEOUT");
      if (source instanceof CustomConversation) {
         SimplePrompt lastPrompt = ((CustomConversation)source).getLastSimplePrompt();
         if (lastPrompt != null) {
            lastPrompt.onConversationEnd(this, event);
         }
      }

      this.onConversationEnd(event, timeout);
   }

   protected void onConversationEnd(ConversationAbandonedEvent event, boolean canceledFromInactivity) {
      this.onConversationEnd(event);
   }

   protected void onConversationEnd(SimpleConversation conversation, ConversationAbandonedEvent event) {
   }

   protected void onConversationEnd(ConversationAbandonedEvent event) {
   }

   public ConversationPrefix getPrefix() {
      return new SimplePrefix(this.prefix != null ? this.prefix : this.plugin.getName());
   }

   protected void setPrefix(String prefix) {
      this.prefix = prefix;
   }

   public boolean insertPrefix() {
      return this.prefix != null && !this.prefix.isEmpty();
   }

   public int getTimeout() {
      return this.timeout;
   }

   protected void setTimeout(int timeout) {
      this.timeout = timeout;
   }
}
