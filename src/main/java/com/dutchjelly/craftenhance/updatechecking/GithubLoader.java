package com.dutchjelly.craftenhance.updatechecking;

import com.dutchjelly.craftenhance.messaging.Messenger;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class GithubLoader {
   private final String rawUrl = "https://raw.githubusercontent.com/DutchJelly/CraftEnhance/master/recent_version.txt";
   private URLConnection connection;
   private VersionChecker checker;
   private String version;

   public static GithubLoader init(VersionChecker checker) {
      GithubLoader loader = new GithubLoader();
      loader.checker = checker;
      return !loader.openConnection() ? null : loader;
   }

   public String getVersion() {
      return this.version;
   }

   public void readVersion() {
      try {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         InputStream inputStream = this.connection.getInputStream();
         byte[] buffer = new byte[128];

         int i;
         while((i = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, i);
         }

         this.version = new String(outputStream.toByteArray(), "UTF-8");
      } catch (Exception var5) {
         Messenger.Message("(fatal) The update checker could not extract the version from the url connection.");
      }

   }

   private boolean openConnection() {
      try {
         this.connection = (new URL("https://raw.githubusercontent.com/DutchJelly/CraftEnhance/master/recent_version.txt")).openConnection();
         return true;
      } catch (Exception var2) {
         Messenger.Message("(fatal) The update checker could not open URL connection.");
         return false;
      }
   }
}
