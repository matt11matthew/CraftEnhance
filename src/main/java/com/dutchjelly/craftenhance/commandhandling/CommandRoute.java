package com.dutchjelly.craftenhance.commandhandling;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandRoute {
   String[] cmdPath();

   String perms();
}
