package com.dutchjelly.craftenhance.messaging;

import com.dutchjelly.craftenhance.CraftEnhance;
import java.util.logging.Logger;

public class Debug {
   private static Logger logger;
   private static boolean enable;
   private static String prefix;

   public static void init(CraftEnhance main) {
      enable = main.getConfig().getBoolean("enable-debug");
      prefix = main.getConfig().getString("debug-prefix");
      logger = main.getLogger();
   }

   public static void Send(Object obj) {
      if (enable) {
         System.out.println(prefix + (obj != null ? obj.toString() : "null"));
      }
   }

   public static void Send(Object sender, Object obj) {
      if (enable) {
         logger.info(prefix + "<" + sender.getClass().getName() + "> " + obj != null ? obj.toString() : "null");
      }
   }

   public static void Send(Object[] arr) {
      if (arr != null) {
         logger.info(prefix + " ");

         for(int i = 0; i < arr.length; ++i) {
            if (arr[i] != null) {
               logger.info(arr[i].toString());
            }
         }

         logger.info("");
      }
   }
}
