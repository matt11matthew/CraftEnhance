package craftenhance.libs.menulib.dependencies.rbglib;

import com.google.gson.JsonObject;

public final class Component {
   private final String colorCode;
   private final String message;
   private final boolean bold;
   private final boolean italic;
   private final boolean underline;
   private final boolean strikethrough;
   private final boolean obfuscated;
   private final boolean reset;

   private Component(Component.Builder builder) {
      this.message = builder.message;
      this.colorCode = builder.colorCode;
      this.bold = builder.bold;
      this.italic = builder.italic;
      this.underline = builder.underline;
      this.strikethrough = builder.strikethrough;
      this.obfuscated = builder.obfuscated;
      this.reset = builder.reset;
   }

   public String getMessage() {
      return this.message;
   }

   public String getColorCode() {
      return this.colorCode;
   }

   public boolean isBold() {
      return this.bold;
   }

   public boolean isItalic() {
      return this.italic;
   }

   public boolean isUnderline() {
      return this.underline;
   }

   public boolean isStrikethrough() {
      return this.strikethrough;
   }

   public boolean isObfuscated() {
      return this.obfuscated;
   }

   public boolean isReset() {
      return this.reset;
   }

   public String toString() {
      JsonObject json = new JsonObject();
      if (this.colorCode != null) {
         json.addProperty("color", this.colorCode);
      }

      if (!this.reset) {
         if (this.bold) {
            json.addProperty("bold", true);
         }

         if (this.strikethrough) {
            json.addProperty("strikethrough", true);
         }

         if (this.underline) {
            json.addProperty("underline", true);
         }

         if (this.italic) {
            json.addProperty("italic", true);
         }

         if (this.obfuscated) {
            json.addProperty("obfuscated", true);
         }
      }

      if (this.message != null) {
         json.addProperty("text", this.message);
      }

      return json + "";
   }

   public JsonObject toJson() {
      JsonObject json = new JsonObject();
      if (this.colorCode != null) {
         json.addProperty("color", this.colorCode);
      }

      if (!this.reset) {
         if (this.bold) {
            json.addProperty("bold", true);
         }

         if (this.strikethrough) {
            json.addProperty("strikethrough", true);
         }

         if (this.underline) {
            json.addProperty("underline", true);
         }

         if (this.italic) {
            json.addProperty("italic", true);
         }

         if (this.obfuscated) {
            json.addProperty("obfuscated", true);
         }
      }

      if (this.message != null) {
         json.addProperty("text", this.message);
      }

      return json;
   }

   // $FF: synthetic method
   Component(Component.Builder x0, Object x1) {
      this(x0);
   }

   public static class Builder {
      private String message;
      private String colorCode;
      private boolean bold;
      private boolean italic;
      private boolean underline;
      private boolean strikethrough;
      private boolean obfuscated;
      private boolean reset;

      public Component.Builder message(String message) {
         this.message = message;
         return this;
      }

      public Component.Builder colorCode(String color) {
         this.colorCode = color;
         return this;
      }

      public Component.Builder bold(boolean b) {
         this.bold = b;
         return this;
      }

      public Component.Builder italic(boolean b) {
         this.italic = b;
         return this;
      }

      public Component.Builder underline(boolean b) {
         this.underline = b;
         return this;
      }

      public Component.Builder strikethrough(boolean b) {
         this.strikethrough = b;
         return this;
      }

      public Component.Builder obfuscated(boolean b) {
         this.obfuscated = b;
         return this;
      }

      public Component.Builder reset(boolean b) {
         this.reset = b;
         return this;
      }

      public Component build() {
         return new Component(this);
      }
   }
}
