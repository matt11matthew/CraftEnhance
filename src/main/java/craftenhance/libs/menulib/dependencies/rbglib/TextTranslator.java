package craftenhance.libs.menulib.dependencies.rbglib;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.awt.Color;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.md_5.bungee.api.ChatColor;

public final class TextTranslator implements Interpolator {
   private static final Pattern HEX_PATTERN = Pattern.compile("(?<!\\\\\\\\)(<#[a-fA-F0-9]{6}>)|(?<!\\\\\\\\)(<#[a-fA-F0-9]{3}>)");
   private static final Pattern GRADIENT_PATTERN = Pattern.compile("(<#[a-fA-F0-9]{6}:#[a-fA-F0-9]{6}>)");
   private static final TextTranslator instance = new TextTranslator();

   public static TextTranslator getInstance() {
      return instance;
   }

   public static String toSpigotFormat(String message) {
      return getInstance().spigotFormat(message);
   }

   public static String toComponent(String message, String defaultColor) {
      return getInstance().componentFormat(message, defaultColor);
   }

   public static String toComponent(String message) {
      return getInstance().componentFormat(message, (String)null);
   }

   private String componentFormat(String message, String defaultColor) {
      JsonArray jsonArray = new JsonArray();
      Component.Builder component = new Component.Builder();
      message = this.checkStringForGradient(message);
      if (defaultColor == null || defaultColor.equals("")) {
         defaultColor = "white";
      }

      StringBuilder builder = new StringBuilder(message.length());
      StringBuilder hex = new StringBuilder();

      for(int i = 0; i < message.length(); ++i) {
         char letter = message.charAt(i);
         boolean checkHex = false;
         boolean checkChar;
         if ((i + 1 >= message.length() || letter != 167) && letter != '&' && letter != '<') {
            checkChar = false;
         } else {
            char msg = message.charAt(i + 1);
            if (checkIfColor(msg)) {
               checkChar = true;
            } else if (msg == '#') {
               hex = new StringBuilder();

               for(int j = 0; j < 7; ++j) {
                  hex.append(message.charAt(i + 1 + j));
               }

               boolean isHexCode = isValidHexCode(hex.toString());
               checkChar = isHexCode;
               checkHex = isHexCode;
            } else {
               checkChar = false;
            }
         }

         if (checkChar) {
            ++i;
            if (i >= message.length()) {
               break;
            }

            letter = message.charAt(i);
            if (letter >= 'A' && letter <= 'Z') {
               letter = (char)(letter + 32);
            }

            String format;
            if (checkHex) {
               format = hex.toString();
               i += 7;
            } else {
               try {
                  format = ChatColors.getByChar(letter).getName();
               } catch (Exception var13) {
                  format = null;
               }
            }

            if (format != null) {
               if (builder.length() > 0) {
                  component.message(builder.toString());
                  builder = new StringBuilder();
                  jsonArray.add(component.build().toJson());
                  component = new Component.Builder();
               }

               if (format.equals(ChatColors.BOLD.getName())) {
                  component.bold(true);
               } else if (format.equals(ChatColors.ITALIC.getName())) {
                  component.italic(true);
               } else if (format.equals(ChatColors.UNDERLINE.getName())) {
                  component.underline(true);
               } else if (format.equals(ChatColors.STRIKETHROUGH.getName())) {
                  component.strikethrough(true);
               } else if (format.equals(ChatColors.MAGIC.getName())) {
                  component.obfuscated(true);
               } else if (format.equals(ChatColors.RESET.getName())) {
                  component.reset(true);
                  component.colorCode(defaultColor);
               } else {
                  component.colorCode(format);
               }
            }
         } else {
            builder.append(letter);
         }
      }

      component.message(builder.toString());
      jsonArray.add(component.build().toJson());
      if (jsonArray.size() > 1) {
         JsonObject jsonObject = new JsonObject();
         jsonObject.add("extra", jsonArray);
         jsonObject.addProperty("text", "");
         return jsonObject.toString();
      } else {
         return component.build() + "";
      }
   }

   private String spigotFormat(String message) {
      String messageCopy = this.checkStringForGradient(message);
      Matcher matcher = HEX_PATTERN.matcher(messageCopy);

      while(matcher.find()) {
         String match = matcher.group(0);
         int firstPos = match.indexOf("#");
         if (match.length() < 9) {
            messageCopy = messageCopy.replace(match, "&x&" + match.charAt(firstPos + 1) + "&" + match.charAt(firstPos + 1) + "&" + match.charAt(firstPos + 2) + "&" + match.charAt(firstPos + 2) + "&" + match.charAt(firstPos + 3) + "&" + match.charAt(firstPos + 3));
         } else {
            messageCopy = messageCopy.replace(match, "&x&" + match.charAt(firstPos + 1) + "&" + match.charAt(firstPos + 2) + "&" + match.charAt(firstPos + 3) + "&" + match.charAt(firstPos + 4) + "&" + match.charAt(firstPos + 5) + "&" + match.charAt(firstPos + 6));
         }
      }

      return ChatColor.translateAlternateColorCodes('&', messageCopy);
   }

   private String checkStringForGradient(String message) {
      String messageCopy = message;
      TextTranslator.GradientType type = null;
      if (message.contains(TextTranslator.GradientType.HSV_GRADIENT_PATTERN.getType())) {
         type = TextTranslator.GradientType.HSV_GRADIENT_PATTERN;
      }

      if (message.contains(TextTranslator.GradientType.SIMPLE_GRADIENT_PATTERN.getType())) {
         type = TextTranslator.GradientType.SIMPLE_GRADIENT_PATTERN;
      }

      if (type != null) {
         StringBuilder builder = new StringBuilder();
         String[] var5 = this.splitOnGradient(type, message);
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String messag = var5[var7];
            builder.append(this.convertToMulitGradiens(type, messag));
         }

         messageCopy = builder.toString();
      }

      if (messageCopy == null) {
         messageCopy = message;
      }

      Matcher matcherGradient = GRADIENT_PATTERN.matcher(messageCopy);
      if (matcherGradient.find()) {
         messageCopy = this.convertGradiens(messageCopy, TextTranslator.GradientType.SIMPLE_GRADIENT_PATTERN);
      }

      return messageCopy;
   }

   public String convertToMulitGradiens(TextTranslator.GradientType type, String message) {
      if (type != null) {
         Double[] portionsList = null;
         int startIndex = message.indexOf(type.getType());
         String subcolor = message.substring(startIndex);
         int subIndex = subcolor.indexOf("<");
         int multi_balance = subcolor.indexOf("_portion");
         int endOfColor = subcolor.indexOf(">");
         message = getStringStriped(message, startIndex, endOfColor);
         if (multi_balance > 0) {
            String portion = subcolor.substring(multi_balance);
            int end = portion.indexOf(">");
            portionsList = (Double[])Arrays.stream(getValuesInside(portion, end)).map(Double::parseDouble).toArray((x$0) -> {
               return new Double[x$0];
            });
            message = message.replace(portion.substring(0, end + 1), "");
         }

         Color[] colorList = (Color[])Arrays.stream(getMultiColors(subcolor, subIndex)).map(TextTranslator::hexToRgb).toArray((x$0) -> {
            return new Color[x$0];
         });
         message = message.replace(subcolor.substring(0, message.length()), "");
         StringBuilder builder = new StringBuilder();
         int end = getNextColor(message);
         int nextEnd = getNextColor(message.substring(end + 1));
         if (startIndex > 0) {
            builder.append(message, 0, startIndex);
         }

         builder.append(this.multiRgbGradient(type, message.substring(Math.max(startIndex, 0), end > 0 ? end : message.length()), colorList, this.checkportions(colorList, portionsList)));
         if (end > 0) {
            builder.append(message, Math.max(end, 0), message.length());
         }

         return builder.toString();
      } else {
         return message;
      }
   }

   public String convertGradiens(String message, TextTranslator.GradientType type) {
      StringBuilder gradient = new StringBuilder();
      Matcher gradientsMatcher = GRADIENT_PATTERN.matcher(message);

      String subMessages;
      String hexRaw;
      int splitPos;
      String subMessag;
      for(subMessages = null; gradientsMatcher.find(); gradient.append(this.rgbGradient(subMessag, hexToRgb(getHexFromString(hexRaw, 0, splitPos)), hexToRgb(getHexFromString(hexRaw, splitPos + 1))))) {
         String match = gradientsMatcher.group(0);
         hexRaw = match.substring(1, match.length() - 1);
         splitPos = hexRaw.indexOf(":");
         int nextGrads = this.getLastGradientMatch(message, type);
         int nextGradientMatch = this.getFirstGradientMatch(message.substring(nextGrads + 1), type) + match.length() + 1;
         subMessag = message.substring(gradientsMatcher.start() >= 0 ? gradientsMatcher.start() + match.length() : 0, nextGradientMatch > 0 && gradientsMatcher.start() < nextGradientMatch && nextGrads != nextGradientMatch ? nextGradientMatch : message.length());
         int nextGrad = getNextColor(subMessag);
         if (nextGrad > 0) {
            subMessages = subMessag.substring(nextGrad);
            subMessag = subMessag.substring(0, nextGrad);
         }
      }

      if (subMessages != null) {
         gradient.append(subMessages);
      }

      return gradient.toString();
   }

   private String multiHsvQuadraticGradient(String str, boolean first) {
      StringBuilder builder = new StringBuilder();
      builder.append(this.hsvGradient(str.substring(0, (int)(0.2D * (double)str.length())), Color.RED, Color.GREEN, (from, to, max) -> {
         return this.quadratic(from, to, max, first);
      }));

      for(int i = (int)(0.2D * (double)str.length()); i < (int)(0.8D * (double)str.length()); ++i) {
         builder.append(ChatColors.of(Color.GREEN)).append(str.charAt(i));
      }

      builder.append(this.hsvGradient(str.substring((int)(0.8D * (double)str.length())), Color.GREEN, Color.RED, (from, to, max) -> {
         return this.quadratic(from, to, max, !first);
      }));
      return builder.toString();
   }

   public String hsvGradient(String str, Color from, Color to) {
      return this.hsvGradient(str, from, to, this);
   }

   public String hsvGradient(String str, Color from, Color to, Interpolator interpolator) {
      float[] hsvFrom = Color.RGBtoHSB(from.getRed(), from.getGreen(), from.getBlue(), (float[])null);
      float[] hsvTo = Color.RGBtoHSB(to.getRed(), to.getGreen(), to.getBlue(), (float[])null);
      double[] h = interpolator.interpolate((double)hsvFrom[0], (double)hsvTo[0], str.length());
      double[] s = interpolator.interpolate((double)hsvFrom[1], (double)hsvTo[1], str.length());
      double[] v = interpolator.interpolate((double)hsvFrom[2], (double)hsvTo[2], str.length());
      StringBuilder builder = new StringBuilder();
      char[] letters = str.toCharArray();
      String lastDecoration = "";

      for(int i = 0; i < letters.length; ++i) {
         char letter = letters[i];
         if ((letter == 167 || letter == '&') && i + 1 < letters.length) {
            char decoration = Character.toLowerCase(letters[i + 1]);
            if (decoration == 'k') {
               lastDecoration = "&k";
            } else if (decoration == 'l') {
               lastDecoration = "&l";
            } else if (decoration == 'm') {
               lastDecoration = "&m";
            } else if (decoration == 'n') {
               lastDecoration = "&n";
            } else if (decoration == 'o') {
               lastDecoration = "&o";
            } else if (decoration == 'r') {
               lastDecoration = "";
            }

            ++i;
         } else {
            builder.append("<").append(convertColortoHex(Color.getHSBColor((float)h[i], (float)s[i], (float)v[i]))).append(">").append(lastDecoration).append(letter);
         }
      }

      return builder.toString();
   }

   public String multiRgbGradient(TextTranslator.GradientType type, String str, Color[] colors, @Nullable Double[] portions) {
      return this.multiRgbGradient(type, str, colors, portions, this);
   }

   public String multiRgbGradient(TextTranslator.GradientType type, String str, Color[] colors, @Nullable Double[] portions, Interpolator interpolator) {
      if (colors.length < 2) {
         return colors.length == 1 ? "<" + convertColortoHex(colors[0]) + ">" + str : str;
      } else {
         Double[] p;
         if (portions == null) {
            p = new Double[colors.length - 1];
            Arrays.fill(p, 1.0D / (double)p.length);
         } else {
            p = portions;
         }

         Preconditions.checkArgument(p.length == colors.length - 1);
         StringBuilder builder = new StringBuilder();
         int strIndex = 0;

         for(int i = 0; i < colors.length - 1; ++i) {
            if (type == TextTranslator.GradientType.SIMPLE_GRADIENT_PATTERN) {
               builder.append(this.rgbGradient(str.substring(strIndex, strIndex + (int)(p[i] * (double)str.length())), colors[i], colors[i + 1], interpolator));
            }

            if (type == TextTranslator.GradientType.HSV_GRADIENT_PATTERN) {
               builder.append(this.hsvGradient(str.substring(strIndex, strIndex + (int)(p[i] * (double)str.length())), colors[i], colors[i + 1], interpolator));
            }

            strIndex = (int)((double)strIndex + p[i] * (double)str.length());
         }

         if (strIndex < str.length()) {
            if (type == TextTranslator.GradientType.SIMPLE_GRADIENT_PATTERN) {
               builder.append(this.rgbGradient(str.substring(strIndex), colors[colors.length - 1], colors[colors.length - 1], (from, to, max) -> {
                  return this.quadratic(from, to, str.length(), true);
               }));
            }

            if (type == TextTranslator.GradientType.HSV_GRADIENT_PATTERN) {
               builder.append(this.hsvGradient(str.substring(strIndex), colors[colors.length - 1], colors[colors.length - 1], (from, to, max) -> {
                  return this.quadratic(from, to, str.length(), true);
               }));
            }
         }

         return builder.toString();
      }
   }

   public String rgbGradient(String str, Color from, Color to) {
      return this.rgbGradient(str, from, to, this);
   }

   public String rgbGradient(String str, Color from, Color to, Interpolator interpolator) {
      double[] red = interpolator.interpolate((double)from.getRed(), (double)to.getRed(), str.length());
      double[] green = interpolator.interpolate((double)from.getGreen(), (double)to.getGreen(), str.length());
      double[] blue = interpolator.interpolate((double)from.getBlue(), (double)to.getBlue(), str.length());
      StringBuilder builder = new StringBuilder();
      char[] letters = str.toCharArray();
      String lastDecoration = "";

      for(int i = 0; i < letters.length; ++i) {
         char letter = letters[i];
         if ((letter == 167 || letter == '&') && i + 1 < letters.length) {
            char decoration = Character.toLowerCase(letters[i + 1]);
            if (decoration == 'k') {
               lastDecoration = "&k";
            } else if (decoration == 'l') {
               lastDecoration = "&l";
            } else if (decoration == 'm') {
               lastDecoration = "&m";
            } else if (decoration == 'n') {
               lastDecoration = "&n";
            } else if (decoration == 'o') {
               lastDecoration = "&o";
            } else if (decoration == 'r') {
               lastDecoration = "";
            }

            ++i;
         } else {
            Color stepColor = new Color((int)Math.round(red[i]), (int)Math.round(green[i]), (int)Math.round(blue[i]));
            boolean isEmpty = letter == ' ' && lastDecoration.isEmpty();
            builder.append(isEmpty ? "" : "<").append(isEmpty ? "" : convertColortoHex(stepColor)).append(isEmpty ? "" : ">").append(lastDecoration).append(letter);
         }
      }

      return builder.toString();
   }

   public double[] interpolate(double from, double to, int max) {
      double[] res = new double[max];

      for(int i = 0; i < max; ++i) {
         res[i] = from + (double)i * ((to - from) / (double)(max - 1));
      }

      return res;
   }

   public double[] quadratic(double from, double to, int max, boolean mode) {
      double[] results = new double[max];
      double a;
      if (mode) {
         a = (to - from) / (double)(max * max);

         for(int i = 0; i < results.length; ++i) {
            results[i] = a * (double)i * (double)i + from;
         }
      } else {
         a = (from - to) / (double)(max * max);
         double b = -2.0D * a * (double)max;

         for(int i = 0; i < results.length; ++i) {
            results[i] = a * (double)i * (double)i + b * (double)i + from;
         }
      }

      return results;
   }

   private static String convertRGBtoHex(int R, int G, int B) {
      if (R >= 0 && R <= 255 && G >= 0 && G <= 255 && B >= 0 && B <= 255) {
         Color color = new Color(R, G, B);
         StringBuilder hex = new StringBuilder(Integer.toHexString(color.getRGB() & 16777215));

         while(hex.length() < 6) {
            hex.insert(0, "0");
         }

         hex.insert(0, "#");
         return hex.toString();
      } else {
         return "0";
      }
   }

   private Double[] checkportions(Color[] colorList, Double[] portionsList) {
      if (colorList != null && portionsList != null) {
         if (colorList.length == portionsList.length) {
            return null;
         } else {
            double num = 0.0D;

            for(int i = 0; i < portionsList.length; ++i) {
               Double number = portionsList[i];
               if (number == null) {
                  portionsList[i] = 0.0D;
                  number = 0.0D;
               }

               num += number;
               if (num > 1.0D) {
                  portionsList[i] = (double)Math.round((1.0D - (num - number)) * 100.0D) / 100.0D;
               }
            }

            return portionsList;
         }
      } else {
         return null;
      }
   }

   public String[] splitOnGradient(TextTranslator.GradientType type, String message) {
      boolean firstMatch = false;
      StringBuilder builder = new StringBuilder();

      for(int i = 0; i < message.length(); ++i) {
         char mess = message.charAt(i);
         if (mess == 'g' && i + 8 < message.length()) {
            StringBuilder build = new StringBuilder();
            boolean isgradient = false;

            for(int check = i > 0 ? i - 1 : 0; check < message.length(); ++check) {
               build.append(message.charAt(check));
               isgradient = build.indexOf(type.getType() + "<") >= 0;
               if (isgradient || check > 8 + i) {
                  break;
               }
            }

            if (isgradient) {
               if (!firstMatch) {
                  firstMatch = true;
               } else {
                  builder.append("_,_");
               }
            }
         }

         builder.append(mess);
      }

      return builder.toString().split("_,_");
   }

   private static Color hexToRgb(String colorStr) {
      if (colorStr.length() == 4) {
         String red = colorStr.substring(1, 2);
         String green = colorStr.substring(2, 3);
         String blue = colorStr.substring(3, 4);
         return new Color(Integer.valueOf(red + red, 16), Integer.valueOf(green + green, 16), Integer.valueOf(blue + blue, 16));
      } else if (colorStr.length() < 7) {
         System.out.println("[RBG-Gradients] This hex color is not vaild " + colorStr);
         return new Color(Color.WHITE.getRGB());
      } else {
         return new Color(Integer.valueOf(colorStr.substring(1, 3), 16), Integer.valueOf(colorStr.substring(3, 5), 16), Integer.valueOf(colorStr.substring(5, 7), 16));
      }
   }

   private static String convertColortoHex(Color color) {
      StringBuilder hex = new StringBuilder(String.format("%06X", color.getRGB() & 16777215));
      hex.insert(0, "#");
      return hex.toString();
   }

   private static boolean checkIfColor(char message) {
      String[] var1 = ChatColors.ALL_CODES;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         String color = var1[var3];
         if (color.equals(String.valueOf(message))) {
            return true;
         }
      }

      return false;
   }

   public static boolean isValidHexCode(String str) {
      String regex = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
      Pattern pattern = Pattern.compile(regex);
      if (str == null) {
         return false;
      } else {
         Matcher matcher = pattern.matcher(str);
         return matcher.matches();
      }
   }

   private static int checkIfContainsColor(String message) {
      int index = message.indexOf(167);
      if (index < 0) {
         index = message.indexOf(38);
      }

      if (index < 0) {
         return -1;
      } else if (index + 1 > message.length()) {
         return -1;
      } else {
         char charColor = message.charAt(index + 1);
         char[] var3 = ChatColors.ALL_COLOR_CODES;
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            char color = var3[var5];
            if (color == charColor) {
               return index;
            }
         }

         return -1;
      }
   }

   public static String[] getValuesInside(String string, int end) {
      int start = string.indexOf("<") + 1;
      return end < 0 ? new String[0] : string.substring(start, end).split(":");
   }

   public static String getHexFromString(String hex, int from, int to) {
      return hex.substring(from, to);
   }

   public static String getHexFromString(String hex, int from) {
      return hex.substring(from);
   }

   public static int getNextColor(String subMessage) {
      int nextGrad = subMessage.indexOf("<#");
      int vanillaColor = checkIfContainsColor(subMessage);
      if (nextGrad < 0) {
         return vanillaColor;
      } else {
         return vanillaColor < 0 ? nextGrad : Math.min(nextGrad, vanillaColor);
      }
   }

   public static int getEndOfColor(String subMessage) {
      int nextGrad = subMessage.indexOf(">");
      int vanillaColor = checkIfContainsColor(subMessage);
      return Math.max(nextGrad, vanillaColor);
   }

   public static String getStringStriped(String message, int startIndex, int endIndex) {
      String subcolor = message.substring(startIndex);
      String substring = subcolor.substring(0, endIndex > 0 ? endIndex + 1 : message.length());
      return message.replace(substring, "");
   }

   public static String[] getMultiColors(String message, int startIndex) {
      String subcolor = message.substring(startIndex);
      int endOfColor = subcolor.indexOf(">");
      String substring = subcolor.substring(0, endOfColor > 0 ? endOfColor + 1 : subcolor.length());
      return substring.substring(1, substring.length() - 1).split(":");
   }

   public int getLastGradientMatch(String message, TextTranslator.GradientType type) {
      Matcher gradientsMatcher = GRADIENT_PATTERN.matcher(message);
      return gradientsMatcher.find() ? gradientsMatcher.end() : -1;
   }

   public int getFirstGradientMatch(String message, TextTranslator.GradientType type) {
      Matcher gradientsMatcher = GRADIENT_PATTERN.matcher(message);
      return gradientsMatcher.find() ? gradientsMatcher.start() : -1;
   }

   public static enum GradientType {
      SIMPLE_GRADIENT_PATTERN("gradiens_"),
      HSV_GRADIENT_PATTERN("gradiens_hsv_");

      private final String type;

      private GradientType(String type) {
         this.type = type;
      }

      public String getType() {
         return this.type;
      }
   }
}
