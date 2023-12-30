package com.dutchjelly.craftenhance.exceptions;

import com.dutchjelly.craftenhance.messaging.Messenger;

public class ConfigError extends RuntimeException {
   public ConfigError(String message) {
      super(message);
   }

   public void printStackTrace() {
      Messenger.Error("(Configuration error) " + this.getMessage());
   }
}
