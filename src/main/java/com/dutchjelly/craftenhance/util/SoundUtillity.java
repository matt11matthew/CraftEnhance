package com.dutchjelly.craftenhance.util;

import org.bukkit.Sound;

public class SoundUtillity {
   public static Sound getSound(String sound) {
      if (sound == null) {
         return null;
      } else {
         Sound[] sounds = Sound.values();
         sound = sound.toUpperCase();
         Sound[] var2 = sounds;
         int var3 = sounds.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Sound sound1 = var2[var4];
            if (sound1.name().equals(sound)) {
               return sound1;
            }
         }

         return null;
      }
   }
}
