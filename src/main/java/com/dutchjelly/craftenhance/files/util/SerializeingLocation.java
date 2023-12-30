package com.dutchjelly.craftenhance.files.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SerializeingLocation {
   public static String serializeLoc(Location loc) {
      String name = loc.getWorld() + "";
      if (loc.getWorld() != null) {
         name = loc.getWorld().getName();
      }

      return name + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + (loc.getPitch() == 0.0F && loc.getYaw() == 0.0F ? "" : " " + Math.round(loc.getYaw()) + " " + Math.round(loc.getPitch()));
   }

   public static Location deserializeLoc(Object rawLoc) {
      if (rawLoc == null) {
         return null;
      } else if (rawLoc instanceof Location) {
         return (Location)rawLoc;
      } else if (!rawLoc.toString().contains(" ")) {
         return null;
      } else {
         String[] parts;
         int length = (parts = rawLoc.toString().split(" ")).length;
         if (length == 4) {
            String world = parts[0];
            World bukkitWorld = Bukkit.getWorld(world);
            if (bukkitWorld == null) {
               return null;
            } else if (!parts[1].matches("[-+]?\\d+") && !parts[2].matches("[-+]?\\d+") && !parts[3].matches("[-+]?\\d+")) {
               return null;
            } else {
               int x = Integer.parseInt(parts[1]);
               int y = Integer.parseInt(parts[2]);
               int z = Integer.parseInt(parts[3]);
               return new Location(bukkitWorld, (double)x, (double)y, (double)z);
            }
         } else {
            return null;
         }
      }
   }
}
