package com.dutchjelly.craftenhance.crafthandling;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.api.CraftEnhanceAPI;
import com.dutchjelly.craftenhance.api.event.crafting.BeforeCraftOutputEvent;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.FurnaceRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.RecipeType;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.crafthandling.util.IMatcher;
import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.crafthandling.util.ServerRecipeTranslator;
import com.dutchjelly.craftenhance.crafthandling.util.WBRecipeComparer;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.util.FurnaceDefultValues;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.permissions.Permissible;

public class RecipeInjector implements Listener {
   private final CraftEnhance plugin;
   private RecipeLoader loader;
   private final boolean disableDefaultModeldataCrafts;
   private final boolean makeItemsadderCompatible;
   private final Map<Furnace, LocalDateTime> pausedFurnaces = new HashMap();
   private final Map<Location, UUID> containerOwners = new HashMap();
   private final Set<Location> notCustomItem = new HashSet();

   public RecipeInjector(CraftEnhance plugin) {
      this.plugin = plugin;
      this.disableDefaultModeldataCrafts = plugin.getConfig().getBoolean("disable-default-custom-model-data-crafts");
      this.makeItemsadderCompatible = plugin.getConfig().getBoolean("make-itemsadder-compatible");
   }

   public void setLoader(RecipeLoader loader) {
      this.loader = loader;
   }

   public void registerContainerOwners(Map<Location, UUID> containerOwners) {
      containerOwners.forEach((key, value) -> {
         if (key != null && key.getWorld() != null) {
            this.containerOwners.put(key, value);
         }

      });
   }

   private boolean containsModeldata(CraftingInventory inv) {
      return Arrays.stream(inv.getMatrix()).anyMatch((x) -> {
         return x != null && x.hasItemMeta() && x.getItemMeta().hasCustomModelData();
      });
   }

   private IMatcher<ItemStack> getTypeMatcher() {
      return Adapter.canUseModeldata() && this.disableDefaultModeldataCrafts ? ItemMatchers.constructIMatcher(ItemMatchers::matchType, ItemMatchers::matchModelData) : ItemMatchers::matchType;
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent e) {
      if (CraftEnhance.self().getConfig().getBoolean("learn-recipes")) {
         try {
            Iterator var2 = e.getPlayer().getDiscoveredRecipes().iterator();

            while(var2.hasNext()) {
               NamespacedKey namespacedKey = (NamespacedKey)var2.next();
               if (namespacedKey.getNamespace().contains("craftenhance")) {
                  e.getPlayer().undiscoverRecipe(namespacedKey);
               }
            }
         } catch (Exception var4) {
         }

         Adapter.DiscoverRecipes(e.getPlayer(), RecipeLoader.getInstance().getLoadedServerRecipes());
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void handleCrafting(PrepareItemCraftEvent e) {
      if (e.getRecipe() != null && e.getRecipe().getResult() != null && this.plugin.getConfig().getBoolean("enable-recipes")) {
         if (e.getInventory() instanceof CraftingInventory) {
            CraftingInventory inv = e.getInventory();
            Recipe serverRecipe = e.getRecipe();
            Debug.Send((Object)("The server wants to inject " + serverRecipe.getResult().toString() + " ceh will check or modify this."));
            List<RecipeGroup> possibleRecipeGroups = this.loader.findGroupsByResult(serverRecipe.getResult(), RecipeType.WORKBENCH);
            List<Recipe> disabledServerRecipes = RecipeLoader.getInstance().getDisabledServerRecipes();
            if (possibleRecipeGroups != null && possibleRecipeGroups.size() != 0) {
               Iterator var6 = possibleRecipeGroups.iterator();

               label94:
               while(var6.hasNext()) {
                  RecipeGroup group = (RecipeGroup)var6.next();
                  Iterator var8 = group.getEnhancedRecipes().iterator();

                  while(true) {
                     while(true) {
                        EnhancedRecipe eRecipe;
                        do {
                           if (!var8.hasNext()) {
                              var8 = group.getServerRecipes().iterator();

                              while(true) {
                                 if (!var8.hasNext()) {
                                    continue label94;
                                 }

                                 Recipe sRecipe = (Recipe)var8.next();
                                 ItemStack[] ingredients;
                                 if (sRecipe instanceof ShapedRecipe) {
                                    ingredients = ServerRecipeTranslator.translateShapedRecipe((ShapedRecipe)sRecipe);
                                    if (WBRecipeComparer.shapeMatches(ingredients, inv.getMatrix(), this.getTypeMatcher())) {
                                       inv.setResult(sRecipe.getResult());
                                       return;
                                    }
                                 } else if (sRecipe instanceof ShapelessRecipe) {
                                    ingredients = ServerRecipeTranslator.translateShapelessRecipe((ShapelessRecipe)sRecipe);
                                    if (WBRecipeComparer.ingredientsMatch(ingredients, inv.getMatrix(), this.getTypeMatcher())) {
                                       inv.setResult(sRecipe.getResult());
                                       return;
                                    }
                                 }
                              }
                           }

                           eRecipe = (EnhancedRecipe)var8.next();
                        } while(!(eRecipe instanceof WBRecipe));

                        WBRecipe wbRecipe = (WBRecipe)eRecipe;
                        if (this.checkForDisabledRecipe(disabledServerRecipes, wbRecipe, serverRecipe.getResult())) {
                           inv.setResult((ItemStack)null);
                        } else {
                           Debug.Send((Object)("Checking if enhanced recipe for " + wbRecipe.getResult().toString() + " matches."));
                           if (wbRecipe.matches(inv.getMatrix()) && e.getViewers().stream().allMatch((x) -> {
                              return this.entityCanCraft(x, wbRecipe);
                           }) && !CraftEnhanceAPI.fireEvent(wbRecipe, e.getViewers().size() > 0 ? (Player)e.getViewers().get(0) : null, inv, group)) {
                              Debug.Send((Object)("Recipe matches, injecting " + wbRecipe.getResult().toString()));
                              if (this.makeItemsadderCompatible && this.containsModeldata(inv)) {
                                 EnhancedRecipe finalERecipe = eRecipe;
                                 Bukkit.getScheduler().runTask(CraftEnhance.self(), () -> {
                                    if (wbRecipe.matches(inv.getMatrix())) {
                                       BeforeCraftOutputEvent beforeCraftOutputEvent = new BeforeCraftOutputEvent(finalERecipe, wbRecipe, wbRecipe.getResult().clone());
                                       if (beforeCraftOutputEvent.isCancelled()) {
                                          return;
                                       }

                                       inv.setResult(beforeCraftOutputEvent.getResultItem());
                                    }

                                 });
                                 return;
                              }

                              BeforeCraftOutputEvent beforeCraftOutputEvent = new BeforeCraftOutputEvent(eRecipe, wbRecipe, wbRecipe.getResult().clone());
                              if (!beforeCraftOutputEvent.isCancelled()) {
                                 inv.setResult(beforeCraftOutputEvent.getResultItem());
                                 return;
                              }
                           } else {
                              Debug.Send((Object)"Recipe doesn't match.");
                           }
                        }
                     }
                  }
               }

               inv.setResult((ItemStack)null);
            } else {
               if (this.disableDefaultModeldataCrafts && Adapter.canUseModeldata() && this.containsModeldata(inv)) {
                  inv.setResult((ItemStack)null);
               }

               if (this.checkForDisabledRecipe(disabledServerRecipes, serverRecipe, serverRecipe.getResult())) {
                  inv.setResult((ItemStack)null);
               }

               Debug.Send((Object)"no matching groups");
            }
         }
      }
   }

   public boolean checkForDisabledRecipe(List<Recipe> disabledServerRecipes, @NonNull Recipe recipe, @NonNull ItemStack result) {
      if (recipe == null) {
         throw new NullPointerException("recipe is marked non-null but is null");
      } else if (result == null) {
         throw new NullPointerException("result is marked non-null but is null");
      } else {
         if (disabledServerRecipes != null && !disabledServerRecipes.isEmpty()) {
            Iterator var4 = disabledServerRecipes.iterator();

            while(var4.hasNext()) {
               Recipe disabledRecipe = (Recipe)var4.next();
               if (disabledRecipe.getResult().isSimilar(result)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public boolean checkForDisabledRecipe(List<Recipe> disabledServerRecipes, @NonNull WBRecipe wbRecipe, @NonNull ItemStack result) {
      if (wbRecipe == null) {
         throw new NullPointerException("wbRecipe is marked non-null but is null");
      } else if (result == null) {
         throw new NullPointerException("result is marked non-null but is null");
      } else {
         if (disabledServerRecipes != null && !disabledServerRecipes.isEmpty()) {
            Iterator var4 = disabledServerRecipes.iterator();

            while(var4.hasNext()) {
               Recipe disabledRecipe = (Recipe)var4.next();
               if (disabledRecipe.getResult().isSimilar(result) && wbRecipe.isSimilar(disabledRecipe)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public RecipeGroup getMatchingRecipeGroup(ItemStack source) {
      ItemStack[] srcMatrix = new ItemStack[]{source};
      FurnaceRecipe recipe = new FurnaceRecipe((String)null, (ItemStack)null, srcMatrix);
      return RecipeLoader.getInstance().findSimilarGroup(recipe);
   }

   public Optional<ItemStack> getFurnaceResult(RecipeGroup group, ItemStack source, Furnace furnace) {
      ItemStack[] srcMatrix = new ItemStack[]{source};
      if (group == null) {
         Debug.Send((Object)"furnace recipe does not match any group, so not changing the outcome");
         return null;
      } else {
         UUID playerId = (UUID)this.containerOwners.get(furnace.getLocation());
         Player p = playerId == null ? null : this.plugin.getServer().getPlayer(playerId);
         Debug.Send((Object)("Furnace belongs to player: " + p + " the id " + playerId));
         Debug.Send((Object)("Furnace source item: " + source));
         Iterator var7 = group.getEnhancedRecipes().iterator();

         while(var7.hasNext()) {
            EnhancedRecipe eRecipe = (EnhancedRecipe)var7.next();
            FurnaceRecipe fRecipe = (FurnaceRecipe)eRecipe;
            Debug.Send((Object)("Checking if enhanced recipe for " + fRecipe.getResult().toString() + " matches."));
            if (fRecipe.matches(srcMatrix)) {
               if (this.entityCanCraft(p, fRecipe)) {
                  Debug.Send((Object)("Found enhanced recipe " + fRecipe.getResult().toString() + " for furnace"));
                  Debug.Send((Object)("Matching ingridens are " + source + " ."));
                  return Optional.of(fRecipe.getResult());
               }

               Debug.Send((Object)("found this recipe " + fRecipe.getResult().toString() + " match but, player has not this permission " + fRecipe.getPermissions()));
               break;
            }
         }

         var7 = group.getServerRecipes().iterator();

         org.bukkit.inventory.FurnaceRecipe fRecipe;
         do {
            if (!var7.hasNext()) {
               return Optional.empty();
            }

            Recipe sRecipe = (Recipe)var7.next();
            fRecipe = (org.bukkit.inventory.FurnaceRecipe)sRecipe;
         } while(!this.getTypeMatcher().match(fRecipe.getInput(), source));

         Debug.Send((Object)"found similar server recipe for furnace");
         Debug.Send((Object)("Source " + source));
         Debug.Send((Object)("Input: " + fRecipe.getInput()));
         return null;
      }
   }

   @EventHandler
   public void exstract(FurnaceExtractEvent e) {
      if (!this.notCustomItem.isEmpty() && this.notCustomItem.contains(e.getBlock().getLocation())) {
         e.setExpToDrop(FurnaceDefultValues.getExp(e.getItemType()));
         this.notCustomItem.remove(e.getBlock().getLocation());
      }

   }

   @EventHandler
   public void smelt(FurnaceSmeltEvent e) {
      Debug.Send((Object)"furnace smelt");
      RecipeGroup group = this.getMatchingRecipeGroup(e.getSource());
      Optional<ItemStack> result = this.getFurnaceResult(group, e.getSource(), (Furnace)e.getBlock().getState());
      if (result != null) {
         if (result.isPresent()) {
            e.setResult((ItemStack)result.get());
         } else {
            ItemStack itemStack = (ItemStack)RecipeLoader.getInstance().getSimilarVanillaRecipe().get(new ItemStack(e.getSource().getType()));
            if (itemStack != null) {
               e.setResult(itemStack);
               Iterator var5 = group.getEnhancedRecipes().iterator();

               while(var5.hasNext()) {
                  EnhancedRecipe eRecipe = (EnhancedRecipe)var5.next();
                  FurnaceRecipe fRecipe = (FurnaceRecipe)eRecipe;
                  if (fRecipe.matcheType(new ItemStack[]{e.getSource()})) {
                     this.notCustomItem.add(e.getBlock().getLocation());
                     break;
                  }
               }
            } else {
               e.setCancelled(true);
            }
         }

      }
   }

   @EventHandler(
      ignoreCancelled = false
   )
   public void burn(FurnaceBurnEvent e) {
      Debug.Send((Object)"furnace burn");
      if (!e.isCancelled()) {
         Furnace f = (Furnace)e.getBlock().getState();
         if (((LocalDateTime)this.pausedFurnaces.getOrDefault(f, LocalDateTime.now())).isAfter(LocalDateTime.now())) {
            e.setCancelled(true);
         } else {
            RecipeGroup recipe = this.getMatchingRecipeGroup(f.getInventory().getSmelting());
            Optional<ItemStack> result = this.getFurnaceResult(recipe, f.getInventory().getSmelting(), (Furnace)e.getBlock().getState());
            if (result != null && !result.isPresent()) {
               if (f.getInventory().getSmelting() != null && RecipeLoader.getInstance().getSimilarVanillaRecipe().get(new ItemStack(f.getInventory().getSmelting().getType())) != null) {
                  return;
               }

               e.setCancelled(true);
               this.pausedFurnaces.put(f, LocalDateTime.now().plusSeconds(10L));
            }

         }
      }
   }

   @EventHandler
   public void furnaceClick(InventoryClickEvent e) {
      if (!e.isCancelled()) {
         if (e.getView().getTopInventory() instanceof FurnaceInventory) {
            Furnace f = (Furnace)e.getView().getTopInventory().getHolder();
            this.pausedFurnaces.remove(f);
         }

      }
   }

   @EventHandler
   public void furnacePlace(BlockPlaceEvent e) {
      if (!e.isCancelled()) {
         if (e.getBlock().getType().equals(Material.FURNACE)) {
            this.containerOwners.put(e.getBlock().getLocation(), e.getPlayer().getUniqueId());
         }

      }
   }

   @EventHandler
   public void furnaceBreak(BlockBreakEvent e) {
      if (!e.isCancelled()) {
         if (e.getBlock().getType().equals(Material.FURNACE)) {
            this.containerOwners.remove(e.getBlock().getLocation());
            this.pausedFurnaces.remove((Furnace)e.getBlock().getState());
         }

      }
   }

   private boolean entityCanCraft(Permissible entity, EnhancedRecipe group) {
      return group.getPermissions() == null || group.getPermissions().equals("") || entity != null && entity.hasPermission(group.getPermissions());
   }

   public Map<Location, UUID> getContainerOwners() {
      return this.containerOwners;
   }
}
