package com.dutchjelly.craftenhance.files.util;

import com.google.gson.Gson;
import craftenhance.libs.menulib.utility.ServerVersion;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class SerializeData {
   public static Object serialize(Object obj) {
      if (obj == null) {
         return null;
      } else if (obj instanceof ChatColor) {
         return ((ChatColor)obj).name();
      } else if (obj instanceof net.md_5.bungee.api.ChatColor) {
         net.md_5.bungee.api.ChatColor color = (net.md_5.bungee.api.ChatColor)obj;
         return ServerVersion.atLeast(ServerVersion.v1_16) ? color.toString() : color.name();
      } else if (obj instanceof Location) {
         return SerializeingLocation.serializeLoc((Location)obj);
      } else if (obj instanceof UUID) {
         return obj.toString();
      } else if (obj instanceof Enum) {
         return obj.toString();
      } else if (obj instanceof CommandSender) {
         return ((CommandSender)obj).getName();
      } else if (obj instanceof World) {
         return ((World)obj).getName();
      } else if (obj instanceof PotionEffect) {
         return ((PotionEffect)obj).serialize();
      } else if (obj instanceof Color) {
         return "#" + ((Color)obj).getRGB();
      } else if (obj instanceof BaseComponent) {
         return toJson((BaseComponent)obj);
      } else if (obj instanceof BaseComponent[]) {
         return toJson((BaseComponent[])((BaseComponent[])obj));
      } else {
         HashMap serialize;
         if (obj instanceof HoverEvent) {
            HoverEvent event = (HoverEvent)obj;
            serialize = new HashMap();
            serialize.put("Action", event.getAction().name());
            serialize.put("Value", Arrays.stream(event.getValue().clone()).map(BaseComponent::toString).collect(Collectors.toList()));
            return serialize;
         } else if (obj instanceof ClickEvent) {
            ClickEvent event = (ClickEvent)obj;
            serialize = new HashMap();
            serialize.put("Action", event.getAction().name());
            serialize.put("Value", event.getValue());
            return serialize;
         } else if (!(obj instanceof Iterable) && !obj.getClass().isArray()) {
            if (!(obj instanceof Map)) {
               if (!(obj instanceof Integer) && !(obj instanceof Double) && !(obj instanceof Float) && !(obj instanceof Long) && !(obj instanceof Short) && !(obj instanceof String) && !(obj instanceof Boolean) && !(obj instanceof ItemStack) && !(obj instanceof MemorySection) && !(obj instanceof Pattern)) {
                  if (obj instanceof ConfigurationSerializable) {
                     return ((ConfigurationSerializable)obj).serialize();
                  } else {
                     throw new SerializeData.SerializeFailedException("Does not know how to serialize " + obj.getClass().getSimpleName() + "! Does it extends ConfigSerializable? Data: " + obj);
                  }
               } else {
                  return obj;
               }
            } else {
               Map<?, ?> oldMap = (Map)obj;
               Map<Object, Object> newMap = new LinkedHashMap();
               Iterator var14 = oldMap.entrySet().iterator();

               while(var14.hasNext()) {
                  Entry<?, ?> entry = (Entry)var14.next();
                  newMap.put(serialize(entry.getKey()), serialize(entry.getValue()));
               }

               return newMap;
            }
         } else {
            List<Object> serialized = new ArrayList();
            if (obj instanceof Iterable) {
               Iterator var2 = ((Iterable)obj).iterator();

               while(var2.hasNext()) {
                  Object element = var2.next();
                  serialized.add(serialize(element));
               }
            } else {
               Object[] var10 = (Object[])((Object[])obj);
               int var13 = var10.length;

               for(int var4 = 0; var4 < var13; ++var4) {
                  Object element = var10[var4];
                  serialized.add(serialize(element));
               }
            }

            return serialized;
         }
      }
   }

   public static String toJson(BaseComponent... comps) {
      if (ServerVersion.olderThan(ServerVersion.v1_7)) {
         return "{}";
      } else {
         String json;
         try {
            json = ComponentSerializer.toString(comps);
         } catch (Throwable var3) {
            json = (new Gson()).toJson((new TextComponent(comps)).toLegacyText());
         }

         return json;
      }
   }

   public static class SerializeFailedException extends RuntimeException {
      private static final long serialVersionUID = 2L;

      public SerializeFailedException(String reason) {
         super(reason);
      }
   }
}
