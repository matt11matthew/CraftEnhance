package craftenhance.libs.menulib.NMS;

import craftenhance.libs.menulib.dependencies.rbglib.TextTranslator;
import craftenhance.libs.menulib.utility.MenuLogger;
import craftenhance.libs.menulib.utility.ServerVersion;
import craftenhance.libs.menulib.utility.Validate;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class UpdateTittleContainers {
   private static Class<?> packetclass;
   private static Method handle;
   private static Field playerConnection;
   private static Class<?> packetConnectionClass;
   private static Class<?> chatBaseCompenent;
   private static Class<?> chatCompenentSubClass;
   private static Class<?> containersClass;
   private static Class<?> containerClass;
   private static Constructor<?> packetConstructor;
   private static UpdateTittleContainers.NmsData nmsData;
   private static MenuLogger menuLogger;

   public static void update(Player p, String title) {
      UpdateTittleContainers.NmsData newNmsData = getNmsData();
      if (menuLogger != null) {
         menuLogger.sendLOG(Level.WARNING, "There was an error the last time you tried to update the title. Send the stack trace to the delevoper.");
      } else {
         try {
            if (p != null) {
               Map inventorySizeNames;
               if (ServerVersion.atLeast(ServerVersion.v1_17)) {
                  if (newNmsData == null) {
                     inventorySizeNames = convertFieldNames(new UpdateTittleContainers.FieldName(9, "a"), new UpdateTittleContainers.FieldName(18, "b"), new UpdateTittleContainers.FieldName(27, "c"), new UpdateTittleContainers.FieldName(36, "d"), new UpdateTittleContainers.FieldName(45, "e"), new UpdateTittleContainers.FieldName(54, "f"), new UpdateTittleContainers.FieldName(5, "p"));
                     if (ServerVersion.atLeast(ServerVersion.v1_19)) {
                        if (ServerVersion.atLeast(ServerVersion.v1_19_4)) {
                           if (ServerVersion.atLeast(ServerVersion.v1_20)) {
                              newNmsData = new UpdateTittleContainers.NmsData("bR", "j", "a", "a", inventorySizeNames);
                           } else {
                              newNmsData = new UpdateTittleContainers.NmsData("bP", "j", "a", "a", inventorySizeNames);
                           }
                        } else {
                           newNmsData = new UpdateTittleContainers.NmsData("bU", "j", "a", "a", inventorySizeNames);
                        }
                     } else if (ServerVersion.atLeast(ServerVersion.v1_18_0)) {
                        newNmsData = new UpdateTittleContainers.NmsData(ServerVersion.atLeast(ServerVersion.v1_18_2) ? "bV" : "bW", "j", "a", "a", inventorySizeNames);
                     } else if (ServerVersion.equals(ServerVersion.v1_17)) {
                        newNmsData = new UpdateTittleContainers.NmsData("bV", "j", "sendPacket", "initMenu", inventorySizeNames);
                     }
                  }
               } else if (ServerVersion.olderThan(ServerVersion.v1_17) && newNmsData == null) {
                  inventorySizeNames = convertFieldNames(new UpdateTittleContainers.FieldName(9, "1"), new UpdateTittleContainers.FieldName(18, "2"), new UpdateTittleContainers.FieldName(27, "3"), new UpdateTittleContainers.FieldName(36, "4"), new UpdateTittleContainers.FieldName(45, "5"), new UpdateTittleContainers.FieldName(54, "6"), new UpdateTittleContainers.FieldName(5, "HOPPER"));
                  newNmsData = new UpdateTittleContainers.NmsData("activeContainer", "windowId", "sendPacket", "updateInventory", inventorySizeNames);
               }

               if (nmsData == null) {
                  nmsData = newNmsData;
               }

               if (newNmsData != null) {
                  loadNmsClasses();
                  updateInventory(p, title);
               }
            }
         } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException | NoSuchFieldException var4) {
            menuLogger = new MenuLogger(UpdateTittleContainers.class);
            var4.printStackTrace();
         }

      }
   }

   private static void loadNmsClasses() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {
      if (ServerVersion.newerThan(ServerVersion.v1_16)) {
         loadNmsClasses1_17();
      } else {
         if (packetclass == null) {
            packetclass = Class.forName(versionCheckNms("Packet"));
         }

         if (handle == null) {
            handle = Class.forName(versionCheckBukkit("entity.CraftPlayer")).getMethod("getHandle");
         }

         if (playerConnection == null) {
            playerConnection = Class.forName(versionCheckNms("EntityPlayer")).getField("playerConnection");
         }

         if (packetConnectionClass == null) {
            packetConnectionClass = Class.forName(versionCheckNms("PlayerConnection"));
         }

         if (chatBaseCompenent == null) {
            chatBaseCompenent = Class.forName(versionCheckNms("IChatBaseComponent"));
         }

         if (containersClass == null) {
            if (ServerVersion.newerThan(ServerVersion.v1_13)) {
               containersClass = Class.forName(versionCheckNms("Containers"));
            } else {
               containersClass = String.class;
            }
         }

         if (containerClass == null) {
            containerClass = Class.forName(versionCheckNms("Container"));
         }

         if (chatCompenentSubClass == null) {
            chatCompenentSubClass = Class.forName(versionCheckNms("IChatBaseComponent$ChatSerializer"));
         }

         if (packetConstructor == null) {
            if (ServerVersion.newerThan(ServerVersion.v1_13)) {
               packetConstructor = Class.forName(versionCheckNms("PacketPlayOutOpenWindow")).getConstructor(Integer.TYPE, containersClass, chatBaseCompenent);
            } else {
               packetConstructor = Class.forName(versionCheckNms("PacketPlayOutOpenWindow")).getConstructor(Integer.TYPE, containersClass, chatBaseCompenent, Integer.TYPE);
            }
         }

      }
   }

   private static void loadNmsClasses1_17() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {
      if (packetclass == null) {
         packetclass = Class.forName("net.minecraft.network.protocol.Packet");
      }

      if (handle == null) {
         handle = Class.forName(versionCheckBukkit("entity.CraftPlayer")).getMethod("getHandle");
      }

      if (playerConnection == null) {
         if (ServerVersion.atLeast(ServerVersion.v1_20)) {
            playerConnection = Class.forName("net.minecraft.server.level.EntityPlayer").getField("c");
         } else {
            playerConnection = Class.forName("net.minecraft.server.level.EntityPlayer").getField("b");
         }
      }

      if (packetConnectionClass == null) {
         packetConnectionClass = Class.forName("net.minecraft.server.network.PlayerConnection");
      }

      if (chatBaseCompenent == null) {
         chatBaseCompenent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
      }

      if (containersClass == null) {
         containersClass = Class.forName("net.minecraft.world.inventory.Containers");
      }

      if (containerClass == null) {
         containerClass = Class.forName("net.minecraft.world.inventory.Container");
      }

      if (chatCompenentSubClass == null) {
         chatCompenentSubClass = Class.forName("net.minecraft.network.chat.IChatBaseComponent$ChatSerializer");
      }

      if (packetConstructor == null) {
         packetConstructor = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutOpenWindow").getConstructor(Integer.TYPE, containersClass, chatBaseCompenent);
      }

   }

   private static void loadNmsClasses1_18() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {
      if (packetclass == null) {
         packetclass = Class.forName("net.minecraft.network.protocol.Packet");
      }

      if (handle == null) {
         handle = Class.forName(versionCheckBukkit("entity.CraftPlayer")).getMethod("getHandle");
      }

      if (playerConnection == null) {
         playerConnection = Class.forName("net.minecraft.server.level.EntityPlayer").getField("b");
      }

      if (packetConnectionClass == null) {
         packetConnectionClass = Class.forName("net.minecraft.server.network.PlayerConnection");
      }

      if (chatBaseCompenent == null) {
         chatBaseCompenent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
      }

      if (containersClass == null) {
         containersClass = Class.forName("net.minecraft.world.inventory.Containers");
      }

      if (containerClass == null) {
         containerClass = Class.forName("net.minecraft.world.inventory.Container");
      }

      if (chatCompenentSubClass == null) {
         chatCompenentSubClass = Class.forName("net.minecraft.network.chat.IChatBaseComponent$ChatSerializer");
      }

      if (packetConstructor == null) {
         packetConstructor = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutOpenWindow").getConstructor(Integer.TYPE, containersClass, chatBaseCompenent);
      }

   }

   private static void updateInventory( Player p, String title) throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, InstantiationException {
      Validate.checkNotNull(p, "player should not be null");
      Validate.checkNotNull(title, "title should not be null");
      Inventory inventory = p.getOpenInventory().getTopInventory();
      UpdateTittleContainers.NmsData nmsData = getNmsData();
      int inventorySize = inventory.getSize();
      boolean isOlder = ServerVersion.olderThan(ServerVersion.v1_17);
      Object player = p.getClass().getMethod("getHandle").invoke(p);
      Object activeContainer = player.getClass().getField(nmsData.getContanerField()).get(player);
      Object windowId = activeContainer.getClass().getField(nmsData.getWindowId()).get(activeContainer);
      String fieldName = nmsData.getContainerFieldnames(inventorySize);
      if (fieldName != null && !fieldName.isEmpty()) {
         if (isOlder && inventorySize % 9 == 0) {
            fieldName = "GENERIC_9X" + fieldName;
         }
      } else if (isOlder) {
         fieldName = "GENERIC_9X3";
      } else {
         fieldName = "f";
      }

      Method declaredMethodChat;
      Object inventoryTittle;
      Object methods;
      Object handles;
      if (ServerVersion.newerThan(ServerVersion.v1_13)) {
         declaredMethodChat = chatCompenentSubClass.getMethod("a", String.class);
         inventoryTittle = declaredMethodChat.invoke((Object)null, TextTranslator.toComponent(title));
         handles = containersClass.getField(fieldName).get((Object)null);
         methods = packetConstructor.newInstance(windowId, handles, inventoryTittle);
      } else {
         declaredMethodChat = chatCompenentSubClass.getMethod(ServerVersion.atLeast(ServerVersion.v1_9) ? "b" : "a", String.class);
         inventoryTittle = declaredMethodChat.invoke((Object)null, "'" + TextTranslator.toSpigotFormat(title) + "'");
         methods = packetConstructor.newInstance(windowId, "minecraft:" + inventory.getType().name().toLowerCase(), inventoryTittle, inventorySize);
      }

      handles = handle.invoke(p);
      Object playerconect = playerConnection.get(handles);
      Method packet1 = packetConnectionClass.getMethod(nmsData.getSendPacket(), packetclass);
      packet1.invoke(playerconect, methods);
      player.getClass().getMethod(nmsData.getUpdateInventory(), containerClass).invoke(player, activeContainer);
   }

   private static String versionCheckNms(String clazzName) {
      return "net.minecraft.server." + Bukkit.getServer().getClass().toGenericString().split("\\.")[3] + "." + clazzName;
   }

   private static String versionCheckBukkit(String clazzName) {
      return "org.bukkit.craftbukkit." + Bukkit.getServer().getClass().toGenericString().split("\\.")[3] + "." + clazzName;
   }

   private static Map<Integer, String> convertFieldNames(UpdateTittleContainers.FieldName... fieldNames) {
      Map<Integer, String> inventoryFieldname = new HashMap();
      UpdateTittleContainers.FieldName[] var2 = fieldNames;
      int var3 = fieldNames.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         UpdateTittleContainers.FieldName fieldName = var2[var4];
         inventoryFieldname.put(fieldName.getInventorySize(), fieldName.getFieldName());
      }

      return inventoryFieldname;
   }

   private static UpdateTittleContainers.NmsData getNmsData() {
      return nmsData;
   }

   private static class FieldName {
      private final int inventorySize;
      private final String fieldName;

      public FieldName(int size, String fieldName) {
         this.inventorySize = size;
         this.fieldName = fieldName;
      }

      public int getInventorySize() {
         return this.inventorySize;
      }

      public String getFieldName() {
         return this.fieldName;
      }
   }

   private static class NmsData {
      private final String contanerField;
      private final String windowId;
      private final String sendPacket;
      private final String updateInventory;
      private final Map<Integer, String> containerFieldnames;

      public NmsData(String contanerField, String windowId, String sendPacket, String updateInventory, Map<Integer, String> containerFieldnames) {
         this.contanerField = contanerField;
         this.windowId = windowId;
         this.sendPacket = sendPacket;
         this.updateInventory = updateInventory;
         this.containerFieldnames = containerFieldnames;
      }

      public String getContanerField() {
         return this.contanerField;
      }

      public String getWindowId() {
         return this.windowId;
      }

      public String getSendPacket() {
         return this.sendPacket;
      }

      public String getUpdateInventory() {
         return this.updateInventory;
      }

      public String getContainerFieldnames(int inventorySize) {
         return (String)this.containerFieldnames.get(inventorySize);
      }
   }
}
