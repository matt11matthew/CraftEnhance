package com.dutchjelly.craftenhance;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.api.CraftEnhanceAPI;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.commands.ceh.ChangeKeyCmd;
import com.dutchjelly.craftenhance.commands.ceh.CleanItemFileCmd;
import com.dutchjelly.craftenhance.commands.ceh.CreateRecipeCmd;
import com.dutchjelly.craftenhance.commands.ceh.Disabler;
import com.dutchjelly.craftenhance.commands.ceh.RecipesCmd;
import com.dutchjelly.craftenhance.commands.ceh.ReloadCmd;
import com.dutchjelly.craftenhance.commands.ceh.SetPermissionCmd;
import com.dutchjelly.craftenhance.commands.ceh.SpecsCommand;
import com.dutchjelly.craftenhance.commands.edititem.DisplayNameCmd;
import com.dutchjelly.craftenhance.commands.edititem.DurabilityCmd;
import com.dutchjelly.craftenhance.commands.edititem.EnchantCmd;
import com.dutchjelly.craftenhance.commands.edititem.ItemFlagCmd;
import com.dutchjelly.craftenhance.commands.edititem.LocalizedNameCmd;
import com.dutchjelly.craftenhance.commands.edititem.LoreCmd;
import com.dutchjelly.craftenhance.crafthandling.RecipeInjector;
import com.dutchjelly.craftenhance.crafthandling.RecipeLoader;
import com.dutchjelly.craftenhance.crafthandling.customcraftevents.ExecuteCommand;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.FurnaceRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.files.CategoryDataCache;
import com.dutchjelly.craftenhance.files.ConfigFormatter;
import com.dutchjelly.craftenhance.files.FileManager;
import com.dutchjelly.craftenhance.files.GuiTemplatesFile;
import com.dutchjelly.craftenhance.files.MenuSettingsCache;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.customcrafting.CustomCraftingTable;
import com.dutchjelly.craftenhance.gui.guis.GUIElement;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import com.dutchjelly.craftenhance.updatechecking.VersionChecker;
import com.dutchjelly.craftenhance.util.Metrics;
import craftenhance.libs.menulib.RegisterMenuAPI;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class CraftEnhance extends JavaPlugin {
   private static CraftEnhance plugin;
   private Metrics metrics;
   private FileManager fm;
   private GuiManager guiManager;
   private GuiTemplatesFile guiTemplatesFile;
   private RegisterMenuAPI registerMenuAPI;
   private CustomCmdHandler commandHandler;
   private RecipeInjector injector;
   VersionChecker versionChecker;
   private boolean usingItemsAdder;
   private boolean isReloding;
   private MenuSettingsCache menuSettingsCache;
   private CategoryDataCache categoryDataCache;

   public static CraftEnhance self() {
      return plugin;
   }

   public void onEnable() {
      plugin = this;
      this.registerSerialization();
      this.versionChecker = VersionChecker.init(this);
      if (this.registerMenuAPI == null) {
         this.registerMenuAPI = new RegisterMenuAPI(this);
      }

      this.saveDefaultConfig();
      Debug.init(this);
      if (this.isReloding) {
         Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            this.loadPluginData(this.isReloding);
         });
      } else {
         this.loadPluginData(false);
         this.loadRecipes();
      }

      this.guiManager = new GuiManager(this);
      Debug.Send((Object)"Setting up listeners and commands");
      if (!this.isReloding) {
         this.setupListeners();
      }

      this.setupCommands();
      Messenger.Message("CraftEnhance is managed and developed by DutchJelly.");
      Messenger.Message("If you find a bug in the plugin, please report it to https://github.com/DutchJelly/CraftEnhance/issues.");
      if (!this.versionChecker.runVersionCheck()) {
         for(int i = 0; i < 4; ++i) {
            Messenger.Message("WARN: The installed version isn't tested to work with this version of the server.");
         }
      }

      BukkitScheduler var10000 = Bukkit.getScheduler();
      VersionChecker var10002 = this.versionChecker;
      var10000.runTaskAsynchronously(this, var10002::runUpdateCheck);
//      if (this.metrics == null) {
//         int metricsId = true;
//         this.metrics = new Metrics(this, 9023);
//      }

      CraftEnhanceAPI.registerListener(new ExecuteCommand());
   }

   public void reload() {
      this.isReloding = true;
      Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
         this.onDisable();
         Bukkit.getScheduler().runTask(this, () -> {
            RecipeLoader.clearInstance();
            this.reloadConfig();
            this.onEnable();
         });
      });
   }

   public void onDisable() {
      if (!this.isReloding) {
         this.getServer().resetRecipes();
      }

      Debug.Send((Object)"Saving container owners...");
      this.fm.saveContainerOwners(this.injector.getContainerOwners());
      Debug.Send((Object)"Saving disabled recipes...");
      this.fm.saveDisabledServerRecipes((List)RecipeLoader.getInstance().getDisabledServerRecipes().stream().map((x) -> {
         return Adapter.GetRecipeIdentifier(x);
      }).collect(Collectors.toList()));
      this.fm.getRecipes().forEach(EnhancedRecipe::save);
      this.categoryDataCache.save();
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (this.commandHandler == null) {
         Messenger.Message("Could not execute the command.", sender);
         Messenger.Message("Something went wrong with initializing the commandHandler. Please make sure to use Spigot or Bukkit when using this plugin. If you are using Spigot or Bukkit and still experiencing this issue, please send a bug report here: https://dev.bukkit.org/projects/craftenhance.");
         Messenger.Message("Disabling the plugin...");
         this.getPluginLoader().disablePlugin(this);
      }

      this.commandHandler.handleCommand(sender, label, args);
      return true;
   }

   private void registerSerialization() {
      ConfigurationSerialization.registerClass(WBRecipe.class, "EnhancedRecipe");
      ConfigurationSerialization.registerClass(WBRecipe.class, "Recipe");
      ConfigurationSerialization.registerClass(FurnaceRecipe.class, "FurnaceRecipe");
   }

   private void setupCommands() {
      this.commandHandler = new CustomCmdHandler(this);
      this.commandHandler.loadCommandClasses(Arrays.asList(new DisplayNameCmd(this.commandHandler), new DurabilityCmd(this.commandHandler), new EnchantCmd(this.commandHandler), new ItemFlagCmd(this.commandHandler), new LocalizedNameCmd(this.commandHandler), new LoreCmd(this.commandHandler)));
      this.commandHandler.loadCommandClasses(Arrays.asList(new CreateRecipeCmd(this.commandHandler), new RecipesCmd(this.commandHandler), new SpecsCommand(this.commandHandler), new ChangeKeyCmd(this.commandHandler), new CleanItemFileCmd(this.commandHandler), new SetPermissionCmd(this.commandHandler), new ReloadCmd(), new Disabler(this.commandHandler)));
   }

   private void setupListeners() {
      this.guiManager = new GuiManager(this);
      this.getServer().getPluginManager().registerEvents(this.injector, this);
      this.getServer().getPluginManager().registerEvents(this.guiManager, this);
   }

   private void setupFileManager() {
      this.fm = FileManager.init(this);
      this.fm.cacheItems();
      this.fm.cacheRecipes();
   }

   private void loadPluginData(Boolean isReloding) {
      if (this.categoryDataCache == null) {
         this.categoryDataCache = new CategoryDataCache();
      }

      this.categoryDataCache.reload();
      Debug.Send((Object)"Checking for config updates.");
      File configFile = new File(this.getDataFolder(), "config.yml");
      FileManager.EnsureResourceUpdate("config.yml", configFile, YamlConfiguration.loadConfiguration(configFile), this);
      Debug.Send((Object)"Coloring config messages.");
      ConfigFormatter.init(this).formatConfigMessages();
      Messenger.Init(this);
      ItemMatchers.init(this.getConfig().getBoolean("enable-backwards-compatible-item-matching"));
      Debug.Send((Object)"Loading gui templates");
      if (this.menuSettingsCache == null) {
         this.menuSettingsCache = new MenuSettingsCache(this);
      }

      this.menuSettingsCache.reload();
      if (isReloding) {
         Bukkit.getScheduler().runTask(this, this::loadRecipes);
      }

   }

   private void loadRecipes() {
      this.usingItemsAdder = this.getServer().getPluginManager().getPlugin("ItemsAdder") != null;
      Debug.Send((Object)"Setting up the file manager for recipes.");
      this.setupFileManager();
      Debug.Send((Object)"Loading recipes");
      RecipeLoader loader = RecipeLoader.getInstance();
      this.fm.getRecipes().stream().filter((x) -> {
         return x.validate() == null;
      }).forEach(loader::loadRecipe);
      loader.printGroupsDebugInfo();
      loader.disableServerRecipes((List)this.fm.readDisabledServerRecipes().stream().map((x) -> {
         return Adapter.FilterRecipes(loader.getServerRecipes(), x);
      }).collect(Collectors.toList()));
      if (this.injector == null) {
         this.injector = new RecipeInjector(this);
      }

      this.injector.registerContainerOwners(this.fm.getContainerOwners());
      this.injector.setLoader(loader);
      if (this.isReloding && Bukkit.getOnlinePlayers().size() > 0 && self().getConfig().getBoolean("learn-recipes")) {
         Iterator var2 = Bukkit.getOnlinePlayers().iterator();

         while(var2.hasNext()) {
            Player player = (Player)var2.next();
            Adapter.DiscoverRecipes(player, this.getCategoryDataCache().getServerRecipes());
         }
      }

      this.isReloding = false;
   }

   public void openEnhancedCraftingTable(Player p) {
      CustomCraftingTable table = new CustomCraftingTable(this.getGuiManager(), this.getGuiTemplatesFile().getTemplate((Class)null), (GUIElement)null, p);
      this.getGuiManager().openGUI(p, table);
   }

   public FileManager getFm() {
      return this.fm;
   }

   public GuiManager getGuiManager() {
      return this.guiManager;
   }

   public GuiTemplatesFile getGuiTemplatesFile() {
      return this.guiTemplatesFile;
   }

   public VersionChecker getVersionChecker() {
      return this.versionChecker;
   }

   public boolean isUsingItemsAdder() {
      return this.usingItemsAdder;
   }

   public MenuSettingsCache getMenuSettingsCache() {
      return this.menuSettingsCache;
   }

   public CategoryDataCache getCategoryDataCache() {
      return this.categoryDataCache;
   }
}
