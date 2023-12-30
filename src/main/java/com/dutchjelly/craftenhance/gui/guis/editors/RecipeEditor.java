package com.dutchjelly.craftenhance.gui.guis.editors;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.RecipeLoader;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.FurnaceRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.exceptions.ConfigError;
import com.dutchjelly.craftenhance.files.CategoryData;
import com.dutchjelly.craftenhance.files.MenuSettingsCache;
import com.dutchjelly.craftenhance.gui.guis.CategoryList;
import com.dutchjelly.craftenhance.gui.guis.EditorTypeSelector;
import com.dutchjelly.craftenhance.gui.guis.RecipesViewer;
import com.dutchjelly.craftenhance.gui.templates.MenuTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.util.FormatListContents;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.gui.util.InfoItemPlaceHolders;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import com.dutchjelly.craftenhance.prompt.HandleChatInput;
import craftenhance.libs.menulib.CheckItemsInsideInventory;
import craftenhance.libs.menulib.MenuButton;
import craftenhance.libs.menulib.MenuHolder;
import craftenhance.libs.menulib.MenuUtility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RecipeEditor<RecipeT extends EnhancedRecipe> extends MenuHolder {
   private final MenuSettingsCache menuSettingsCache = CraftEnhance.self().getMenuSettingsCache();
   private String permission;
   private final RecipeT recipe;
   private final MenuTemplate menuTemplate;
   private ItemStack result;
   private boolean hidden;
   private ItemMatchers.MatchType matchType;
   private boolean shapeless;
   private final ButtonType editorType;
   private short duration;
   private float exp;
   private final CategoryData categoryData;

   public RecipeEditor(RecipeT recipe, CategoryData categoryData, String permission, ButtonType editorType) {
      super(FormatListContents.formatRecipes(recipe));
      if (permission != null && !permission.equals("")) {
         this.permission = permission;
      } else {
         this.permission = recipe.getPermissions();
      }

      this.editorType = editorType;
      this.recipe = recipe;
      this.categoryData = categoryData;
      if (recipe instanceof FurnaceRecipe) {
         this.duration = (short)((FurnaceRecipe)recipe).getDuration();
         this.exp = ((FurnaceRecipe)recipe).getExp();
      }

      if (recipe instanceof WBRecipe) {
         this.shapeless = ((WBRecipe)this.recipe).isShapeless();
      }

      this.matchType = recipe.getMatchType();
      this.menuTemplate = (MenuTemplate)this.menuSettingsCache.getTemplates().get(editorType.getType());
      this.setMenuSize(27);
      this.setSlotsYouCanAddItems(true);
      if (this.menuTemplate != null) {
         this.setMenuSize(GuiUtil.invSize("RecipeEditor", this.menuTemplate.getAmountOfButtons()));
         this.setTitle(this.menuTemplate.getMenuTitel());
         this.setFillSpace(this.menuTemplate.getFillSlots());
         this.setMenuOpenSound(this.menuTemplate.getSound());
      }

   }

   public MenuButton getFillButtonAt(Object object) {
      return new MenuButton() {
         public void onClickInsideMenu(Player player, Inventory inventory, ClickType clickType, ItemStack itemStack, Object o) {
         }

         public ItemStack getItem(Object object) {
            return object instanceof ItemStack ? (ItemStack)object : null;
         }

         public ItemStack getItem() {
            return null;
         }
      };
   }

   public MenuButton getButtonAt(int slot) {
      if (this.menuTemplate == null) {
         return null;
      } else {
         Iterator var2 = this.menuTemplate.getMenuButtons().entrySet().iterator();

         Entry menuTemplate;
         do {
            if (!var2.hasNext()) {
               return null;
            }

            menuTemplate = (Entry)var2.next();
         } while(!((List)menuTemplate.getKey()).contains(slot));

         return this.registerButtons((com.dutchjelly.craftenhance.gui.templates.MenuButton)menuTemplate.getValue());
      }
   }

   private MenuButton registerButtons(final com.dutchjelly.craftenhance.gui.templates.MenuButton value) {
      return new MenuButton() {
         public void onClickInsideMenu(Player player, Inventory menu, ClickType click, ItemStack clickedItem, Object object) {
            if (RecipeEditor.this.run(value, menu, player, click)) {
               RecipeEditor.super.updateButton(this);
            }

         }

         public ItemStack getItem() {
            Map<String, String> placeHolders = new HashMap<String, String>() {
               {
                  this.put(InfoItemPlaceHolders.Key.getPlaceHolder(), RecipeEditor.this.recipe.getKey() == null ? "null" : RecipeEditor.this.recipe.getKey());
                  if (RecipeEditor.this.recipe instanceof WBRecipe) {
                     this.put(InfoItemPlaceHolders.Shaped.getPlaceHolder(), RecipeEditor.this.shapeless ? "shapeless" : "shaped");
                  }

                  if (RecipeEditor.this.recipe instanceof FurnaceRecipe) {
                     this.put(InfoItemPlaceHolders.Exp.getPlaceHolder(), String.valueOf(RecipeEditor.this.exp));
                     this.put(InfoItemPlaceHolders.Duration.getPlaceHolder(), String.valueOf(RecipeEditor.this.duration));
                  }

                  this.put(InfoItemPlaceHolders.MatchMeta.getPlaceHolder(), RecipeEditor.this.matchType.getDescription());
                  this.put(InfoItemPlaceHolders.MatchType.getPlaceHolder(), RecipeEditor.this.matchType.getDescription());
                  this.put(InfoItemPlaceHolders.Hidden.getPlaceHolder(), RecipeEditor.this.hidden ? "hide recipe in menu" : "show recipe in menu");
                  this.put(InfoItemPlaceHolders.Permission.getPlaceHolder(), RecipeEditor.this.permission != null && !RecipeEditor.this.permission.trim().equals("") ? RecipeEditor.this.permission : "none");
                  this.put(InfoItemPlaceHolders.Slot.getPlaceHolder(), String.valueOf(RecipeEditor.this.recipe.getSlot()));
                  this.put(InfoItemPlaceHolders.Page.getPlaceHolder(), String.valueOf(RecipeEditor.this.recipe.getPage()));
                  if (RecipeEditor.this.categoryData != null) {
                     this.put(InfoItemPlaceHolders.Category.getPlaceHolder(), RecipeEditor.this.categoryData.getRecipeCategory());
                  } else {
                     this.put(InfoItemPlaceHolders.Category.getPlaceHolder(), RecipeEditor.this.recipe.getRecipeCategory() != null ? RecipeEditor.this.recipe.getRecipeCategory() : "default");
                  }

               }
            };
            return value.getItemStack() == null ? null : GuiUtil.ReplaceAllPlaceHolders(value.getItemStack().clone(), placeHolders);
         }
      };
   }

   public boolean run(com.dutchjelly.craftenhance.gui.templates.MenuButton value, Inventory menu, Player player, ClickType click) {
      if (value.getButtonType() == ButtonType.SetPosition) {
         new HandleChatInput(this, this::handlePositionChange);
         return true;
      } else if (value.getButtonType() == ButtonType.SetCookTime) {
         (new HandleChatInput(this, (msg) -> {
            if (!msg.equals("cancel") && !msg.equals("quit") && !msg.equals("exit") && !msg.equals("q")) {
               short parsed;
               try {
                  parsed = Short.parseShort(msg);
               } catch (NumberFormatException var7) {
                  Messenger.Message("Error, you didn't input a number. your input " + msg, this.getViewer());
                  return true;
               }

               if (parsed < 0) {
                  parsed = 0;
               }

               Messenger.Message("Successfully set duration to " + parsed, this.getViewer());
               this.duration = parsed;
               CheckItemsInsideInventory checkItemsInsideInventory = new CheckItemsInsideInventory();
               checkItemsInsideInventory.setSlotsToCheck(this.menuTemplate.getFillSlots());
               Map<Integer, ItemStack> map = checkItemsInsideInventory.getItemsOnSpecifiedSlots(menu, player, false);
               this.getIngredients(map, player);
               this.menuOpen(player);
               return false;
            } else {
               this.menuOpen(player);
               return false;
            }
         })).setMessages("Please input a cook duration.Type q, exit, cancel to turn it off.").start(player);
         return true;
      } else if (value.getButtonType() == ButtonType.SetExp) {
         (new HandleChatInput(this, (msg) -> {
            if (!msg.equals("cancel") && !msg.equals("quit") && !msg.equals("exit")) {
               int parsed;
               try {
                  parsed = Integer.parseInt(msg);
               } catch (NumberFormatException var5) {
                  Messenger.Message("Error, you didn't input a number. your input " + msg, this.getViewer());
                  return true;
               }

               if (parsed < 0) {
                  parsed = 0;
               }

               Messenger.Message("Successfully set exp to " + parsed, this.getViewer());
               this.exp = (float)parsed;
               this.menuOpen(player);
               return false;
            } else {
               this.menuOpen(player);
               return false;
            }
         })).setMessages("Please input an exp amount.Type q, exit, cancel to turn it off.").start(this.getViewer());
         return true;
      } else if (value.getButtonType() == ButtonType.SwitchShaped) {
         this.shapeless = !this.shapeless;
         return true;
      } else if (value.getButtonType() == ButtonType.DeleteRecipe) {
         CraftEnhance.self().getFm().removeRecipe(this.recipe);
         RecipeLoader.getInstance().unloadRecipe(this.recipe);
         if (this.categoryData != null) {
            (new RecipesViewer(this.categoryData, "", player)).menuOpen(player);
         } else {
            (new EditorTypeSelector((String)null, this.permission)).menuOpen(player);
         }

         return true;
      } else if (value.getButtonType() == ButtonType.SwitchHidden) {
         this.hidden = !this.hidden;
         return true;
      } else if (value.getButtonType() == ButtonType.SwitchMatchMeta) {
         this.switchMatchMeta();
         return true;
      } else if (value.getButtonType() == ButtonType.ResetRecipe) {
         this.updateRecipeDisplay(menu);
         return true;
      } else if (value.getButtonType() == ButtonType.SetPermission) {
         (new HandleChatInput(this, (msg) -> {
            if (!this.handlePermissionSetCB(msg)) {
               this.menuOpen(player);
               return false;
            } else {
               return true;
            }
         })).setMessages("Set your own permission on a recipe. Only players some has this permission can craft the item.", " Type q,exit,cancel to turn it off").start(this.getViewer());
         return true;
      } else {
         if (value.getButtonType() == ButtonType.SaveRecipe) {
            CheckItemsInsideInventory checkItemsInsideInventory = new CheckItemsInsideInventory();
            checkItemsInsideInventory.setSlotsToCheck(this.menuTemplate.getFillSlots());
            Map<Integer, ItemStack> map = checkItemsInsideInventory.getItemsOnSpecifiedSlots(menu, player, false);
            this.save(map, player, true);
            (new RecipeEditor(this.recipe, this.categoryData, this.permission, this.editorType)).menuOpen(player);
         }

         if (value.getButtonType() == ButtonType.Back) {
            if (this.categoryData != null) {
               (new RecipesViewer(this.categoryData, "", player)).menuOpen(player);
            } else {
               (new EditorTypeSelector((String)null, this.permission)).menuOpen(player);
            }
         }

         if (value.getButtonType() == ButtonType.ChangeCategoryList) {
            (new CategoryList(this.recipe, this.categoryData, this.permission, this.editorType, "")).menuOpen(player);
         }

         if (value.getButtonType() == ButtonType.ChangeCategory) {
            (new HandleChatInput(this, (msg) -> {
               if (!GuiUtil.changeOrCreateCategory(msg, player)) {
                  new RecipeEditor(this.recipe, this.categoryData, this.permission, this.editorType);
                  return false;
               } else {
                  return true;
               }
            })).setMessages("Change category name and you can also change item (if not set it will use the old one). Like this 'category new_category_name crafting_table' without '. If you want create new category recomend use this format 'category crafting_table' without '", "Type q,exit,cancel to turn it off.").start(this.getViewer());
         }

         return false;
      }
   }

   public void menuClose(InventoryCloseEvent event, MenuUtility menu) {
      if (CraftEnhance.self().getConfig().getBoolean("save_on_close")) {
         CheckItemsInsideInventory checkItemsInsideInventory = new CheckItemsInsideInventory();
         checkItemsInsideInventory.setSlotsToCheck(this.menuTemplate.getFillSlots());
         Map<Integer, ItemStack> map = checkItemsInsideInventory.getItemsOnSpecifiedSlots(event.getInventory(), this.getViewer(), false);
         this.save(map, this.getViewer(), true);
      }

   }

   private void updateRecipeDisplay(Inventory menu) {
      List<Integer> fillSpace = this.menuTemplate.getFillSlots();
      if (fillSpace.size() != this.recipe.getContent().length + 1) {
         throw new ConfigError("fill space of Recipe editor must be " + (this.recipe.getContent().length + 1));
      } else {
         for(int i = 0; i < this.recipe.getContent().length; ++i) {
            if ((Integer)fillSpace.get(i) >= menu.getSize()) {
               throw new ConfigError("fill space spot " + fillSpace.get(i) + " is outside of inventory");
            }

            menu.setItem((Integer)fillSpace.get(i), this.recipe.getContent()[i]);
         }

         if ((Integer)fillSpace.get(this.recipe.getContent().length) >= menu.getSize()) {
            throw new ConfigError("fill space spot " + fillSpace.get(this.recipe.getContent().length) + " is outside of inventory");
         } else {
            menu.setItem((Integer)fillSpace.get(this.recipe.getContent().length), this.recipe.getResult());
            this.matchType = this.recipe.getMatchType();
            this.hidden = this.recipe.isHidden();
         }
      }
   }

   private void save(Map<Integer, ItemStack> map, Player player, boolean loadRecipe) {
      ItemStack[] newContents = this.getIngredients(map, player);
      if (newContents == null) {
         Messenger.Message("The recipe is empty.", player);
      } else {
         ItemStack newResult = this.getResult();
         if (newResult == null) {
            Messenger.Message("The result slot is empty.", player);
         } else {
            this.recipe.setContent(newContents);
            this.recipe.setResult(newResult);
            this.recipe.setMatchType(this.matchType);
            this.recipe.setHidden(this.hidden);
            this.beforeSave();
            this.recipe.setPermissions(this.permission);
            this.recipe.save();
            if (loadRecipe) {
               this.recipe.load();
            } else {
               Messenger.Message("Has not reload this recipe, click on save to reload the recipe or /ceh reload", player);
            }

            Messenger.Message("Successfully saved the recipe.", player);
         }
      }
   }

   private void beforeSave() {
      if (this.recipe instanceof WBRecipe) {
         ((WBRecipe)this.recipe).setShapeless(this.shapeless);
      }

      if (this.recipe instanceof FurnaceRecipe) {
         ((FurnaceRecipe)this.recipe).setDuration(this.duration);
         ((FurnaceRecipe)this.recipe).setExp(this.exp);
      }

   }

   @Nullable
   private ItemStack[] getIngredients(Map<Integer, ItemStack> map, Player player) {
      int resultSlot = (Integer)this.menuTemplate.getFillSlots().get(this.recipe.getContent().length);
      List<ItemStack> arrays = new ArrayList(this.recipe.getContent().length);
      int index = 0;

      for(Iterator var6 = this.menuTemplate.getFillSlots().iterator(); var6.hasNext(); ++index) {
         Integer slot = (Integer)var6.next();
         ItemStack itemStack = (ItemStack)map.get(slot);
         if (itemStack != null && itemStack.getAmount() > 1 && slot != resultSlot) {
            Messenger.Message("Recipes only support amounts of 1 in the content.", player);
            itemStack.setAmount(1);
         }

         if (slot != resultSlot) {
            arrays.add(index, itemStack);
         }

         if (slot == resultSlot) {
            this.recipe.setResultSlot(index);
         }
      }

      this.result = (ItemStack)map.remove(resultSlot);
      if (!arrays.stream().anyMatch((x) -> {
         return x != null;
      })) {
         return null;
      } else if (this.recipe instanceof FurnaceRecipe) {
         return (ItemStack[])arrays.toArray(new ItemStack[1]);
      } else {
         ItemStack[] itemstacks = (ItemStack[])arrays.toArray(new ItemStack[0]);
         return itemstacks;
      }
   }

   private boolean handlePermissionSetCB(String message) {
      if (message != null && !message.trim().equals("")) {
         message = message.trim();
         if (!message.equalsIgnoreCase("q") && !message.equalsIgnoreCase("cancel") && !message.equalsIgnoreCase("quit") && !message.equalsIgnoreCase("exit")) {
            if (message.equals("-")) {
               this.permission = "";
               return false;
            } else if (message.contains(" ")) {
               Messenger.Message("A permission can't contain a space.", this.getViewer());
               return true;
            } else {
               this.permission = message;
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void switchMatchMeta() {
      ItemMatchers.MatchType[] matchTypes = ItemMatchers.MatchType.values();

      int i;
      for(i = 0; i < matchTypes.length && matchTypes[i] != this.matchType; ++i) {
      }

      if (i == matchTypes.length) {
         Debug.Send((Object)"couldn't find match type that's currently selected in the editor");
      } else {
         this.matchType = matchTypes[(i + 1) % matchTypes.length];
      }
   }

   public boolean handlePositionChange(String message) {
      if (message != null && message.trim() != "") {
         if (!message.equals("") && !message.equalsIgnoreCase("q") && !message.equalsIgnoreCase("cancel") && !message.equalsIgnoreCase("quit") && !message.equalsIgnoreCase("exit")) {
            String[] args = message.split(" ");
            if (args.length != 2) {
               Messenger.Message("Please specify a page and slot number separated by a space.", this.getViewer());
               return true;
            } else {
               boolean var4 = false;

               int page;
               try {
                  page = Integer.parseInt(args[0]);
               } catch (NumberFormatException var7) {
                  Messenger.Message("Could not parse the page number.", this.getViewer());
                  return true;
               }

               int slot;
               try {
                  slot = Integer.parseInt(args[1]);
               } catch (NumberFormatException var6) {
                  Messenger.Message("Could not parse the slot number.", this.getViewer());
                  return true;
               }

               this.recipe.setPage(page);
               this.recipe.setSlot(slot);
               Messenger.Message("Set the page to " + page + ", and the slot to " + slot + ". This will get auto-filled if it's not available.", this.getViewer());
               CraftEnhance.self().getFm().saveRecipe(this.recipe);
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public RecipeT getRecipe() {
      return this.recipe;
   }

   public ItemStack getResult() {
      return this.result;
   }
}
