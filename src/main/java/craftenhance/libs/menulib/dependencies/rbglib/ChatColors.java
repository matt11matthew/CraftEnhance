package craftenhance.libs.menulib.dependencies.rbglib;

import com.google.common.base.Preconditions;
import java.awt.Color;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class ChatColors {
   public static final char COLOR_CHAR = '§';
   public static final char COLOR_AMPERSAND = '&';
   public static final String[] ALL_CODES = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "a", "B", "b", "C", "c", "D", "d", "E", "e", "F", "f", "K", "k", "L", "l", "M", "m", "N", "n", "O", "o", "R", "r", "X", "x"};
   public static final char[] ALL_COLOR_CODES = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'a', 'B', 'b', 'C', 'c', 'D', 'd', 'E', 'e', 'F', 'f', 'R', 'r', 'X', 'x'};
   private static final char[] SPECIAL_SIGN = new char[]{'l', 'n', 'o', 'k', 'm', 'r'};
   public static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf('§') + "[0-9A-FK-ORX]");
   private static final Map<Character, ChatColors> BY_CHAR = new HashMap();
   private static final Map<String, ChatColors> BY_NAME = new HashMap();
   public static final ChatColors BLACK = new ChatColors('0', "black", new Color(0));
   public static final ChatColors DARK_BLUE = new ChatColors('1', "dark_blue", new Color(170));
   public static final ChatColors DARK_GREEN = new ChatColors('2', "dark_green", new Color(43520));
   public static final ChatColors DARK_AQUA = new ChatColors('3', "dark_aqua", new Color(43690));
   public static final ChatColors DARK_RED = new ChatColors('4', "dark_red", new Color(11141120));
   public static final ChatColors DARK_PURPLE = new ChatColors('5', "dark_purple", new Color(11141290));
   public static final ChatColors GOLD = new ChatColors('6', "gold", new Color(16755200));
   public static final ChatColors GRAY = new ChatColors('7', "gray", new Color(11184810));
   public static final ChatColors DARK_GRAY = new ChatColors('8', "dark_gray", new Color(5592405));
   public static final ChatColors BLUE = new ChatColors('9', "blue", new Color(5592575));
   public static final ChatColors GREEN = new ChatColors('a', "green", new Color(5635925));
   public static final ChatColors AQUA = new ChatColors('b', "aqua", new Color(5636095));
   public static final ChatColors RED = new ChatColors('c', "red", new Color(16733525));
   public static final ChatColors LIGHT_PURPLE = new ChatColors('d', "light_purple", new Color(16733695));
   public static final ChatColors YELLOW = new ChatColors('e', "yellow", new Color(16777045));
   public static final ChatColors WHITE = new ChatColors('f', "white", new Color(16777215));
   public static final ChatColors MAGIC = new ChatColors('k', "obfuscated");
   public static final ChatColors BOLD = new ChatColors('l', "bold");
   public static final ChatColors STRIKETHROUGH = new ChatColors('m', "strikethrough");
   public static final ChatColors UNDERLINE = new ChatColors('n', "underline");
   public static final ChatColors ITALIC = new ChatColors('o', "italic");
   public static final ChatColors RESET = new ChatColors('r', "reset");
   private static int count = 0;
   private final String toString;
   private final String name;
   private final Color color;
   private final char code;

   public ChatColors(char code, String name) {
      this(code, name, (Color)null);
   }

   public ChatColors(char code, String name, Color color) {
      this.name = name;
      this.toString = new String(new char[]{'§', code});
      this.color = color;
      this.code = code;
      BY_CHAR.put(code, this);
      BY_NAME.put(name.toUpperCase(Locale.ROOT), this);
   }

   private ChatColors(String name, String toString, int rgb) {
      this.name = name;
      this.toString = toString;
      this.color = new Color(rgb);
      this.code = ' ';
   }

   public static ChatColors getByChar(char code) {
      return (ChatColors)BY_CHAR.get(code);
   }

   public char[] getSpecialSign() {
      return SPECIAL_SIGN;
   }

   public static ChatColors of(Color color) {
      return of("#" + String.format("%08x", color.getRGB()).substring(2));
   }

   public static ChatColors of(String string) {
      Preconditions.checkArgument(string != null, "string cannot be null");
      if (string.startsWith("#") && string.length() == 7) {
         int rgb;
         try {
            rgb = Integer.parseInt(string.substring(1), 16);
         } catch (NumberFormatException var7) {
            throw new IllegalArgumentException("Illegal hex string " + string);
         }

         StringBuilder magic = new StringBuilder("§x");
         char[] var3 = string.substring(1).toCharArray();
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            char c = var3[var5];
            magic.append('§').append(c);
         }

         return new ChatColors(string, magic.toString(), rgb);
      } else {
         ChatColors defined = (ChatColors)BY_NAME.get(string.toUpperCase(Locale.ROOT));
         if (defined != null) {
            return defined;
         } else {
            throw new IllegalArgumentException("Could not parse ChatColors " + string);
         }
      }
   }

   public String toString() {
      return this.toString;
   }

   public String getName() {
      return this.name;
   }

   public Color getColor() {
      return this.color;
   }

   public char getCode() {
      return this.code;
   }
}
