package com.dutchjelly.craftenhance.prompt;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.gui.interfaces.IChatInputHandler;
import com.dutchjelly.craftenhance.messaging.Messenger;
import craftenhance.libs.menulib.MenuHolder;
import craftenhance.libs.prompt.prompt.library.SimpleConversation;
import craftenhance.libs.prompt.prompt.library.SimplePrompt;
import craftenhance.libs.prompt.prompt.library.utility.SimpleCanceller;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

public class HandleChatInput extends SimpleConversation {
   private final MenuHolder menuHolder;
   private final List<String> messages;
   private final IChatInputHandler chatInputHandler;

   public HandleChatInput(MenuHolder menuHolder, IChatInputHandler callback) {
      super(CraftEnhance.self());
      this.menuHolder = menuHolder;
      this.messages = new ArrayList();
      this.chatInputHandler = callback;
   }

   public HandleChatInput setMessages(String... messages) {
      this.messages.addAll(Arrays.asList(messages));
      return this;
   }

   public int getTimeout() {
      return 120;
   }

   protected void onConversationEnd(SimpleConversation conversation, ConversationAbandonedEvent event) {
      Messenger.Message("Quit the prompt ", (CommandSender)event.getContext().getForWhom());
   }

   protected ConversationCanceller getCanceller() {
      return new SimpleCanceller(new String[]{"non"});
   }

   public Prompt getFirstPrompt() {
      return new HandleChatInput.Action(this.menuHolder, this.messages, this.chatInputHandler);
   }

   public static class Action extends SimplePrompt {
      private final MenuHolder menuHolder;
      private final List<String> messages;
      private final IChatInputHandler chatInputHandler;

      public Action(MenuHolder menuHolder, List<String> message, IChatInputHandler callback) {
         this.menuHolder = menuHolder;
         this.messages = message;
         this.chatInputHandler = callback;
      }

      protected String getPrompt(ConversationContext conversationContext) {
         Player player = this.getPlayer();
         if (conversationContext.getForWhom() instanceof Player) {
            player = this.getPlayer(conversationContext);
         }

         String lastMessage = (String)this.messages.get(this.messages.size() - 1);
         Iterator var4 = this.messages.iterator();

         while(var4.hasNext()) {
            String message = (String)var4.next();
            if (message.equals(lastMessage)) {
               break;
            }

            Messenger.Message(message, player);
         }

         return Messenger.getMessage(lastMessage, player);
      }

      protected Prompt acceptValidatedInput( ConversationContext context,  String input) {
         IChatInputHandler callback = this.chatInputHandler;
         if (callback.handle(input)) {
            return new HandleChatInput.Action(this.menuHolder, this.messages, callback);
         } else {
            MenuHolder gui = this.menuHolder;
            return null;
         }
      }
   }
}
