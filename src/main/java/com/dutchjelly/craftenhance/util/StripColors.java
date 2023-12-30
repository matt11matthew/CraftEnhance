package com.dutchjelly.craftenhance.util;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.updatechecking.VersionChecker;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StripColors {
   private static final Pattern COLOR_AND_DECORATION_REGEX = Pattern.compile("(&|§)[0-9a-fk-orA-FK-OR]");
   public static final Pattern RGB_HEX_COLOR_REGEX = Pattern.compile("(?<!\\\\)(&|)#((?:[0-9a-fA-F]{3}){1,2})");
   private static final Pattern RGB_X_COLOR_REGEX = Pattern.compile("(§x)(§[0-9a-fA-F]){6}");

   public static List<String> stripLore(List<String> lores) {
      List<String> clearColors = new ArrayList();
      if (lores != null && !lores.isEmpty()) {
         Iterator var2 = lores.iterator();

         while(var2.hasNext()) {
            String lore = (String)var2.next();
            clearColors.add(stripColors(lore));
         }

         return clearColors;
      } else {
         return clearColors;
      }
   }

   public static String stripColors(String message) {
      if (message != null && !message.isEmpty()) {
         Matcher matcher;
         for(matcher = COLOR_AND_DECORATION_REGEX.matcher(message); matcher.find(); message = matcher.replaceAll("")) {
         }

         if (CraftEnhance.self().getVersionChecker().newerThan(VersionChecker.ServerVersion.v1_15)) {
            for(matcher = RGB_HEX_COLOR_REGEX.matcher(message); matcher.find(); message = matcher.replaceAll("")) {
            }

            for(matcher = RGB_X_COLOR_REGEX.matcher(message); matcher.find(); message = matcher.replaceAll("")) {
            }

            message = message.replace("§x", "");
         }

         return message;
      } else {
         return message;
      }
   }
}
