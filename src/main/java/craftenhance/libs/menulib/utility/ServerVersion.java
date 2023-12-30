package craftenhance.libs.menulib.utility;

import org.bukkit.plugin.Plugin;

public enum ServerVersion {
   v1_20(20.0F),
   v1_19_4(19.4F),
   v1_19(19.0F),
   v1_18_2(18.2F),
   v1_18_1(18.1F),
   v1_18_0(18.0F),
   v1_17(17.0F),
   v1_16(16.0F),
   v1_15(15.0F),
   v1_14(14.0F),
   v1_13(13.0F),
   v1_12(12.0F),
   v1_11(11.0F),
   v1_10(10.0F),
   v1_9(9.0F),
   v1_8(8.0F),
   v1_7(7.0F),
   v1_6(6.0F),
   v1_5(5.0F),
   v1_4(4.0F),
   v1_3_AND_BELOW(3.0F);

   private final float version;
   private static float currentServerVersion;

   public static boolean equals(ServerVersion version) {
      return serverVersion(version) == 0.0D;
   }

   public static boolean atLeast(ServerVersion version) {
      return equals(version) || newerThan(version);
   }

   public static boolean newerThan(ServerVersion version) {
      return serverVersion(version) > 0.0D;
   }

   public static boolean olderThan(ServerVersion version) {
      return serverVersion(version) < 0.0D;
   }

   public static double serverVersion(ServerVersion version) {
      return (double)(currentServerVersion - version.getVersion());
   }

   public static void setServerVersion(Plugin plugin) {
      String[] strings = plugin.getServer().getBukkitVersion().split("\\.");
      String firstString = strings[1];
      String firstNumber;
      String secondNumber;
      if (firstString.contains("-")) {
         firstNumber = firstString.substring(0, firstString.lastIndexOf("-"));
         secondNumber = firstString.substring(firstString.lastIndexOf("-") + 1);
         int index = secondNumber.toUpperCase().indexOf("R");
         if (index >= 0) {
            secondNumber = secondNumber.substring(index + 1);
         }
      } else {
         String secondString = strings[2];
         firstNumber = firstString;
         secondNumber = secondString.substring(0, secondString.lastIndexOf("-"));
      }

      float version = Float.parseFloat(firstNumber + "." + secondNumber);
      if (version < 18.0F) {
         currentServerVersion = (float)Math.floor((double)version);
      } else {
         currentServerVersion = version;
      }

   }

   public float getVersion() {
      return this.version;
   }

   public static float getCurrentServerVersion() {
      return currentServerVersion;
   }

   private ServerVersion(float version) {
      this.version = version;
   }
}
