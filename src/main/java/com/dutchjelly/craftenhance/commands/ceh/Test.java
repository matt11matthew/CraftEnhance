package com.dutchjelly.craftenhance.commands.ceh;

import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.crafthandling.RecipeGroup;
import com.dutchjelly.craftenhance.crafthandling.RecipeLoader;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.RecipeType;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.crafthandling.util.ServerRecipeTranslator;
import com.dutchjelly.craftenhance.crafthandling.util.WBRecipeComparer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

@CommandRoute(
   cmdPath = {"ceh.test"},
   perms = "ceh.debug"
)
public class Test implements ICommand {
   public String getDescription() {
      return "Unit tests for the plugin.";
   }

   public void handlePlayerCommand(Player p, String[] args) {
      if (args.length != 1) {
         p.sendMessage("Please specify a unit test index.");
      }

      this.unitTests(Integer.parseInt(args[0]), p);
   }

   public void handleConsoleCommand(CommandSender sender, String[] args) {
      if (args.length != 1) {
         sender.sendMessage("Please specify a unit test index.");
      }

   }

   private void unitTests(int index, CommandSender p) {
      switch(index) {
      case 1:
         this.testShapedComparer(p);
         break;
      case 2:
         this.testServerRecipeTranslator(p);
         break;
      case 3:
         this.testLoader(p);
         break;
      case 4:
         this.testShapeless(p);
         break;
      default:
         p.sendMessage("no test specified");
      }

   }

   private ItemStack[] buildRecipe(String string) {
      ItemStack it = new ItemStack(Material.DIAMOND);
      ItemStack[] built = new ItemStack[9];

      for(int i = 0; i < string.length(); ++i) {
         built[i] = string.charAt(i) == '-' ? null : it;
      }

      return built;
   }

   private String shiftToLeftTop(String s) {
      if (s.trim() == "") {
         return s;
      } else if (s.length() != 9) {
         return s;
      } else {
         while(s.startsWith("---")) {
            s = s.substring(3) + "---";
         }

         while(s.length() > 0 && s.charAt(0) == '-' && s.charAt(3) == '-' && s.charAt(6) == '-') {
            s = s.substring(1) + '-';
         }

         return s;
      }
   }

   private void testShapedComparer(CommandSender p) {
      new ItemStack(Material.DIAMOND);
      p.sendMessage("testing some cornercases");
      ItemStack[] a = this.buildRecipe("--i--i--i");
      ItemStack[] b = this.buildRecipe("i--i--i--");
      ItemStack[] c = this.buildRecipe("-i--i--i-");
      this.Assert(WBRecipeComparer.shapeMatches(a, b, ItemMatchers::matchType));
      this.Assert(WBRecipeComparer.shapeMatches(a, c, ItemMatchers::matchType));
      this.Assert(WBRecipeComparer.shapeMatches(b, c, ItemMatchers::matchType));
      a = this.buildRecipe("----i-i--");
      b = this.buildRecipe("--i-i----");
      c = this.buildRecipe("-i-i-----");
      this.Assert(WBRecipeComparer.shapeMatches(a, b, ItemMatchers::matchType));
      this.Assert(WBRecipeComparer.shapeMatches(a, c, ItemMatchers::matchType));
      this.Assert(WBRecipeComparer.shapeMatches(b, c, ItemMatchers::matchType));
      a = this.buildRecipe("--ii-----");
      b = this.buildRecipe("-----ii--");
      c = this.buildRecipe("---i-i---");
      this.Assert(WBRecipeComparer.shapeMatches(a, b, ItemMatchers::matchType));
      this.Assert(!WBRecipeComparer.shapeMatches(a, c, ItemMatchers::matchType));
      this.Assert(!WBRecipeComparer.shapeMatches(b, c, ItemMatchers::matchType));
      p.sendMessage("testing randomly generated recipes");
//      int randomtestcount = true;
      Random r = new Random();

      for(int t = 0; t < 50; ++t) {
         String shape = "";
         int counter = 0;

         for(int j = 0; j < 9; ++j) {
            shape = shape + (r.nextBoolean() ? '-' : 'i');
            if (shape.endsWith("i")) {
               ++counter;
            }
         }

         if (counter != 0) {
            a = this.buildRecipe(shape);
            String othershape = this.shiftToLeftTop(shape);
            b = this.buildRecipe(othershape);
            Bukkit.getLogger().log(Level.INFO, shape + "\n testing " + othershape);
            this.Assert(WBRecipeComparer.shapeMatches(a, b, ItemMatchers::matchType));

            for(int matchtest = 0; matchtest < 200; ++matchtest) {
               List<String> shuffeledShape = Arrays.asList(shape.split(""));
               Collections.shuffle(shuffeledShape, r);
               String s = String.join("", shuffeledShape);
               boolean matches = this.shiftToLeftTop(s).equals(this.shiftToLeftTop(shape));
               c = this.buildRecipe(s);
               boolean success = matches && WBRecipeComparer.shapeIterationMatches(a, c, ItemMatchers::matchType, 3) || !matches && !WBRecipeComparer.shapeIterationMatches(a, c, ItemMatchers::matchType, 3);
               if (!success) {
                  Bukkit.getLogger().log(Level.INFO, s + "\n" + (matches ? "matches" : "!matches"));
               }

               this.Assert(success);
            }
         }
      }

      p.sendMessage("All tests executed!");
   }

   private void testServerRecipeTranslator(CommandSender p) {
//      int testAmount = true;
      ItemStack[] items = new ItemStack[]{new ItemStack(Material.DIAMOND), new ItemStack(Material.LADDER), new ItemStack(Material.STICK)};
      Random r = new Random();
      p.sendMessage("testing randomly generated shaped recipes translations");

      for(int i = 0; i < 10; ++i) {
         ItemStack[] recipe = new ItemStack[9];

         for(int j = 0; j < 9; ++j) {
            if (r.nextBoolean()) {
               recipe[j] = items[r.nextInt(3)];
            } else {
               recipe[j] = null;
            }
         }

         ShapedRecipe sr = ServerRecipeTranslator.translateShapedEnhancedRecipe(recipe, new ItemStack(Material.DIAMOND), "test");
         if (sr != null) {
            ItemStack[] original = ServerRecipeTranslator.translateShapedRecipe(sr);
            p.sendMessage((String)Arrays.stream(original).filter((x) -> {
               return x != null && x.getData() != null;
            }).map((x) -> {
               return x.getData().toString();
            }).collect(Collectors.joining(", ")));
            p.sendMessage((String)Arrays.stream(recipe).filter((x) -> {
               return x != null && x.getData() != null;
            }).map((x) -> {
               return x.toString().toString();
            }).collect(Collectors.joining(", ")));
            this.Assert(WBRecipeComparer.shapeMatches(original, recipe, ItemMatchers::matchType));
         }
      }

      p.sendMessage("All successful!");
   }

   private void testLoader(CommandSender p) {
      ItemStack supahDiamondChestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
      ItemStack supahDiamond = new ItemStack(Material.DIAMOND);
      ItemMeta supahDiamondMeta = supahDiamond.getItemMeta();
      supahDiamondMeta.addEnchant(Enchantment.DURABILITY, 10, true);
      supahDiamond.setItemMeta(supahDiamondMeta);
      ItemStack[] customContent = new ItemStack[]{supahDiamond, null, supahDiamond, supahDiamond, supahDiamond, supahDiamond, supahDiamond, supahDiamond, supahDiamond};
      WBRecipe recipe = new WBRecipe();
      recipe.setKey("testing123");
      recipe.setPermissions("");
      recipe.setContent(customContent);
      recipe.setResult(supahDiamondChestplate);
      RecipeLoader.getInstance().loadRecipe(recipe);
      this.Assert(!RecipeLoader.getInstance().isLoadedAsServerRecipe(recipe));
      this.Assert(RecipeLoader.getInstance().findGroup(recipe) != null);
      RecipeGroup group = RecipeLoader.getInstance().findGroup(recipe);
      this.Assert(group.getServerRecipes().size() == 1);
      this.Assert(RecipeLoader.getInstance().findGroupsByResult(supahDiamondChestplate, RecipeType.WORKBENCH).contains(group));
      this.Assert(RecipeLoader.getInstance().findGroupsByResult(new ItemStack(Material.DIAMOND_CHESTPLATE), RecipeType.WORKBENCH).contains(group));
      p.sendMessage("showing loaded groups...");
      this.showLoadedRecipeGroups(p);
      this.Assert(group.getServerRecipes().stream().anyMatch((x) -> {
         return x.getResult().getType().equals(Material.DIAMOND_CHESTPLATE);
      }));
      RecipeLoader.getInstance().unloadRecipe((EnhancedRecipe)recipe);
      this.Assert(!RecipeLoader.getInstance().isLoadedAsServerRecipe(recipe));
      this.Assert(RecipeLoader.getInstance().findGroup(recipe) == null);
      customContent = new ItemStack[]{null, null, null, supahDiamond, null, supahDiamond, supahDiamond, null, supahDiamond};
      recipe.setContent(customContent);
      RecipeLoader.getInstance().loadRecipe(recipe);
      this.Assert(!RecipeLoader.getInstance().isLoadedAsServerRecipe(recipe));
      this.Assert(RecipeLoader.getInstance().findGroup(recipe) != null);
      group = RecipeLoader.getInstance().findGroup(recipe);
      this.Assert(group.getServerRecipes().size() == 1);
      this.Assert(RecipeLoader.getInstance().findGroupsByResult(supahDiamond, RecipeType.WORKBENCH).contains(group));
      this.Assert(RecipeLoader.getInstance().findGroupsByResult(new ItemStack(Material.DIAMOND_BOOTS), RecipeType.WORKBENCH).contains(group));
      this.Assert(group.getServerRecipes().stream().anyMatch((x) -> {
         return x.getResult().getType().equals(Material.DIAMOND_BOOTS);
      }));
      WBRecipe recipe2 = new WBRecipe();
      recipe2.setKey("testing1234");
      recipe2.setPermissions("");
      recipe2.setContent(customContent);
      recipe2.setResult(new ItemStack(Material.EMERALD));
      RecipeLoader.getInstance().loadRecipe(recipe2);
      this.Assert(RecipeLoader.getInstance().findGroup(recipe2).getEnhancedRecipes().size() == 2);
      this.Assert(RecipeLoader.getInstance().findGroupsByResult(new ItemStack(Material.EMERALD), RecipeType.WORKBENCH).size() == 1);
      this.Assert(RecipeLoader.getInstance().findGroup(recipe2).getServerRecipes().size() == 1);
      RecipeLoader.getInstance().unloadRecipe((EnhancedRecipe)recipe2);
      RecipeLoader.getInstance().unloadRecipe((EnhancedRecipe)recipe);
      this.Assert(!RecipeLoader.getInstance().isLoadedAsServerRecipe(recipe));
      this.Assert(RecipeLoader.getInstance().findGroup(recipe) == null);
      this.Assert(!RecipeLoader.getInstance().isLoadedAsServerRecipe(recipe2));
      this.Assert(RecipeLoader.getInstance().findGroup(recipe2) == null);
      customContent = new ItemStack[]{null, supahDiamond, null, null, null, null, null, null, null};
      recipe.setContent(customContent);
      RecipeLoader.getInstance().loadRecipe(recipe);
      this.Assert(RecipeLoader.getInstance().isLoadedAsServerRecipe(recipe));
      this.Assert(RecipeLoader.getInstance().findGroup(recipe) != null);
      group = RecipeLoader.getInstance().findGroup(recipe);
      this.Assert(group.getServerRecipes().size() == 0);
      this.Assert(RecipeLoader.getInstance().findGroupsByResult(supahDiamond, RecipeType.WORKBENCH).contains(group));
      this.Assert(RecipeLoader.getInstance().findGroupsByResult(new ItemStack(supahDiamond), RecipeType.WORKBENCH).contains(group));
      this.Assert(!group.getServerRecipes().stream().anyMatch((x) -> {
         return x.getResult().getType().equals(Material.DIAMOND);
      }));
      RecipeLoader.getInstance().unloadRecipe((EnhancedRecipe)recipe);
      this.Assert(!RecipeLoader.getInstance().isLoadedAsServerRecipe(recipe));
      this.Assert(RecipeLoader.getInstance().findGroup(recipe) == null);
      p.sendMessage("test successfully completed!");
   }

   private void showLoadedRecipeGroups(CommandSender p) {
      p.sendMessage("printed debug info in the console (assuming that your debug mode is on)");
      RecipeLoader.getInstance().printGroupsDebugInfo();
   }

   private void testShapeless(CommandSender p) {
//      int nRandomTests = true;
      ItemStack[] items = (ItemStack[])Arrays.asList(Material.values()).stream().map(ItemStack::new).toArray((x$0) -> {
         return new ItemStack[x$0];
      });
      Random r = new Random();

      for(int i = 0; i < 500; ++i) {
         List<ItemStack> ingredients = new ArrayList();

         int j;
         for(j = 0; j < r.nextInt(8) + 1; ++j) {
            ingredients.add(items[r.nextInt(items.length)]);
         }

         for(j = 0; j < 50; ++j) {
            List<ItemStack> shuffled = new ArrayList(ingredients);
            Collections.shuffle(shuffled, r);
            this.Assert(WBRecipeComparer.ingredientsMatch((ItemStack[])ingredients.stream().toArray((x$0) -> {
               return new ItemStack[x$0];
            }), (ItemStack[])shuffled.stream().toArray((x$0) -> {
               return new ItemStack[x$0];
            }), ItemMatchers::matchMeta));
            List<ItemStack> lessIngredients = new ArrayList(ingredients);
            lessIngredients.stream().filter((x) -> {
               return x != null && r.nextInt(4) == 1;
            }).collect(Collectors.toList());
            lessIngredients.set(r.nextInt(ingredients.size()), null);
            Collections.shuffle(lessIngredients, r);
            this.Assert(!WBRecipeComparer.ingredientsMatch((ItemStack[])ingredients.stream().toArray((x$0) -> {
               return new ItemStack[x$0];
            }), (ItemStack[])lessIngredients.stream().toArray((x$0) -> {
               return new ItemStack[x$0];
            }), ItemMatchers::matchMeta));
            List<ItemStack> moreIngredients = new ArrayList(ingredients);
            Arrays.asList(new int[1 + r.nextInt(5)]).forEach((x) -> {
               moreIngredients.add(items[r.nextInt(items.length)]);
            });
            Collections.shuffle(moreIngredients);
            this.Assert(!WBRecipeComparer.ingredientsMatch((ItemStack[])ingredients.stream().toArray((x$0) -> {
               return new ItemStack[x$0];
            }), (ItemStack[])moreIngredients.stream().toArray((x$0) -> {
               return new ItemStack[x$0];
            }), ItemMatchers::matchMeta));
         }
      }

      p.sendMessage("test successfully completed!");
   }

   private void Assert(boolean x) {
      try {
         if (!x) {
            throw new Exception("assertion failed");
         }
      } catch (Throwable var3) {
         var3.printStackTrace();
//         throw var3;
      }
   }
}
