package craftenhance.libs.menulib.utility.Item;

import craftenhance.libs.RegisterNbtAPI;
import craftenhance.libs.menulib.RegisterMenuAPI;
import craftenhance.libs.menulib.dependencies.rbglib.TextTranslator;
import craftenhance.libs.menulib.utility.ServerVersion;
import craftenhance.libs.menulib.utility.Validate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
;

public class CreateItemStack {
   private final ItemStack itemStack;
   private final Material matrial;
   private final String stringItem;
   private String rgb;
   private final Iterable<?> itemArray;
   private final String displayName;
   private final List<String> lore;
   private final Map<Enchantment, Pair<Integer, Boolean>> enchantments;
   private final List<ItemFlag> visibleItemFlags;
   private final List<ItemFlag> flagsToHide;
   private final List<Pattern> pattern;
   private final List<PotionEffect> portionEffects;
   private final List<FireworkEffect> fireworkEffects;
   private MetaDataWraper metadata;
   private int amoutOfItems;
   private int red;
   private int green;
   private int blue;
   private short data;
   private int customModeldata;
   private boolean glow;
   private boolean showEnchantments;
   private boolean waterBottle;
   private boolean unbreakable;
   private boolean keepAmount;
   private boolean keepOldMeta;
   private boolean copyOfItem;
   private static ConvertToItemStack convertItems;

   private CreateItemStack(CreateItemStack.Bulider bulider) {
      this.enchantments = new HashMap();
      this.visibleItemFlags = new ArrayList();
      this.flagsToHide = new ArrayList();
      this.pattern = new ArrayList();
      this.portionEffects = new ArrayList();
      this.fireworkEffects = new ArrayList();
      this.red = -1;
      this.green = -1;
      this.blue = -1;
      this.data = -1;
      this.customModeldata = -1;
      this.keepOldMeta = true;
      if (convertItems == null) {
         convertItems = new ConvertToItemStack();
      }

      this.itemStack = bulider.itemStack;
      this.matrial = bulider.matrial;
      this.stringItem = bulider.stringItem;
      this.itemArray = bulider.itemArray;
      this.displayName = bulider.displayName;
      this.lore = bulider.lore;
   }

   public static CreateItemStack of(Object item) {
      return of((Object)item, (String)null, (List)((List)null));
   }

   public static CreateItemStack of(Object item, String itemMetaKey, String itemMetaValue) {
      return of(item).setItemMetaData(itemMetaKey, itemMetaValue);
   }

   public static CreateItemStack of(Object item, String color) {
      return of(item, color, (String)null, (List)((List)null));
   }

   public static CreateItemStack of(Object item, String color, String itemMetaKey, String itemMetaValue) {
      return of(item, color).setItemMetaData(itemMetaKey, itemMetaValue);
   }

   public static CreateItemStack of(Object item, String displayName, String... lore) {
      return of(item, displayName, Arrays.asList(lore));
   }

   public static CreateItemStack of(Object item, String displayName, List<String> lore) {
      return (new CreateItemStack.Bulider(getConvertItems().checkItem(item), displayName, lore)).build();
   }

   public static CreateItemStack of(Object item, String color, String displayName, List<String> lore) {
      return (new CreateItemStack.Bulider(getConvertItems().checkItem(item, color), displayName, lore)).build();
   }

   public static <T> CreateItemStack of(Iterable<T> itemArray, String displayName, String... lore) {
      return of(itemArray, displayName, Arrays.asList(lore));
   }

   public static <T> CreateItemStack of(Iterable<T> itemArray, String displayName, List<String> lore) {
      return (new CreateItemStack.Bulider(itemArray, displayName, lore)).build();
   }

   public CreateItemStack setAmoutOfItems(int amoutOfItems) {
      this.amoutOfItems = amoutOfItems;
      return this;
   }

   public boolean isGlow() {
      return this.glow;
   }

   public CreateItemStack setGlow(boolean glow) {
      this.glow = glow;
      return this;
   }

   public List<Pattern> getPattern() {
      return this.pattern;
   }

   public CreateItemStack addPattern(Pattern... patterns) {
      if (patterns != null && patterns.length >= 1) {
         this.pattern.addAll(Arrays.asList(patterns));
         return this;
      } else {
         return this;
      }
   }

   public CreateItemStack addPattern(List<Pattern> pattern) {
      this.pattern.addAll(pattern);
      return this;
   }

   public Map<Enchantment, Pair<Integer, Boolean>> getEnchantments() {
      return this.enchantments;
   }

   public boolean isWaterBottle() {
      return this.waterBottle;
   }

   public CreateItemStack setWaterBottle(boolean waterBottle) {
      this.waterBottle = waterBottle;
      return this;
   }

   public boolean isKeepAmount() {
      return this.keepAmount;
   }

   public CreateItemStack setKeepAmount(boolean keepAmount) {
      this.keepAmount = keepAmount;
      return this;
   }

   public boolean isKeepOldMeta() {
      return this.keepOldMeta;
   }

   public CreateItemStack setKeepOldMeta(boolean keepOldMeta) {
      this.keepOldMeta = keepOldMeta;
      return this;
   }

   public List<FireworkEffect> getFireworkEffects() {
      return this.fireworkEffects;
   }

   public void setFireworkEffects(List<FireworkEffect> fireworkEffects) {
      fireworkEffects.addAll(fireworkEffects);
   }

   public CreateItemStack addEnchantments(String... enchantments) {
      String[] var2 = enchantments;
      int var3 = enchantments.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String enchant = var2[var4];
         int middle = enchant.indexOf(";");
         int last = enchant.lastIndexOf(";");
         this.addEnchantments(enchant.substring(0, middle), last > 0 && Boolean.getBoolean(enchant.substring(last + 1)), Integer.parseInt(enchant.substring(middle + 1, Math.max(last, enchant.length()))));
      }

      return this;
   }

   public CreateItemStack addEnchantments(Enchantment... enchantments) {
      Enchantment[] var2 = enchantments;
      int var3 = enchantments.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Enchantment enchant = var2[var4];
         this.addEnchantments(enchant, true, 1);
      }

      return this;
   }

   public CreateItemStack addEnchantments(Map<Enchantment, Pair<Integer, Boolean>> enchantmentMap, boolean override) {
      Validate.checkNotNull(enchantmentMap, "this map is null");
      if (enchantmentMap.isEmpty()) {
         RegisterMenuAPI.getLogger(Level.INFO, "This map is empty so no enchantments vill be added");
      }

      enchantmentMap.forEach((key, value) -> {
         if (!override) {
            this.enchantments.putIfAbsent(key, value);
         } else {
            this.enchantments.put(key, value);
         }

      });
      return this;
   }

   public CreateItemStack addEnchantments(Object enchant, boolean levelRestriction, int enchantmentLevel) {
      Enchantment enchantment = null;
      if (enchant instanceof String) {
         enchantment = Enchantment.getByKey(NamespacedKey.minecraft((String)enchant));
      } else if (enchant instanceof Enchantment) {
         enchantment = (Enchantment)enchant;
      }

      if (enchantment != null) {
         this.enchantments.put(enchantment, new Pair(enchantmentLevel, levelRestriction));
      } else {
         RegisterMenuAPI.getLogger(Level.INFO, "your enchantment: " + enchant + " ,are not valid.");
      }

      return this;
   }

   public CreateItemStack setShowEnchantments(boolean showEnchantments) {
      this.showEnchantments = showEnchantments;
      return this;
   }

   public CreateItemStack setItemMetaData(String itemMetaKey, Object itemMetaValue) {
      return this.setItemMetaData(itemMetaKey, itemMetaValue, false);
   }

   public CreateItemStack setItemMetaData(String itemMetaKey, Object itemMetaValue, boolean keepclazz) {
      this.metadata = MetaDataWraper.of().add(itemMetaKey, itemMetaValue, keepclazz);
      return this;
   }

   public CreateItemStack setItemMetaDataList(MetaDataWraper wraper) {
      this.metadata = wraper;
      return this;
   }

   public CreateItemStack setItemMetaDataList(Map<String, Object> itemMetaMap) {
      if (itemMetaMap != null && !itemMetaMap.isEmpty()) {
         MetaDataWraper wraper = MetaDataWraper.of();
         Iterator var3 = itemMetaMap.entrySet().iterator();

         while(var3.hasNext()) {
            Entry<String, Object> itemdata = (Entry)var3.next();
            wraper.add((String)itemdata.getKey(), itemdata.getValue());
         }

         this.metadata = wraper;
      }

      return this;
   }

   public boolean isUnbreakable() {
      return this.unbreakable;
   }

   public CreateItemStack setUnbreakable(boolean unbreakable) {
      this.unbreakable = unbreakable;
      return this;
   }

   public short getData() {
      return this.data;
   }

   public CreateItemStack setData(short data) {
      this.data = data;
      return this;
   }

   public int getCustomModeldata() {
      return this.customModeldata;
   }

   public CreateItemStack setCustomModeldata(int customModeldata) {
      this.customModeldata = customModeldata;
      return this;
   }

   public List<PotionEffect> getPortionEffects() {
      return this.portionEffects;
   }

   public CreateItemStack addPortionEffects(PotionEffect... potionEffects) {
      if (potionEffects.length == 0) {
         return this;
      } else {
         this.portionEffects.addAll(Arrays.asList(potionEffects));
         return this;
      }
   }

   public CreateItemStack addPortionEffects(List<PotionEffect> potionEffects) {
      if (potionEffects.isEmpty()) {
         RegisterMenuAPI.getLogger(Level.INFO, "This list of portion effects is empty so no values vill be added");
         return this;
      } else {
         this.portionEffects.addAll(potionEffects);
         return this;
      }
   }

   public CreateItemStack setPortionEffects(List<PotionEffect> potionEffects) {
      if (potionEffects.isEmpty()) {
         RegisterMenuAPI.getLogger(Level.INFO, "This list of portion effects is empty so no values vill be added");
         return this;
      } else {
         this.portionEffects.clear();
         this.portionEffects.addAll(potionEffects);
         return this;
      }
   }

   public String getRgb() {
      return this.rgb;
   }

   public CreateItemStack setRgb(String rgb) {
      this.rgb = rgb;
      String[] colors = this.getRgb().split(",");
      Validate.checkBoolean(colors.length < 4, "rgb is not format correcly. Should be formated like this 'r,b,g'. Example '20,15,47'.");

      try {
         this.red = Integer.parseInt(colors[0]);
         this.green = Integer.parseInt(colors[2]);
         this.blue = Integer.parseInt(colors[1]);
      } catch (NumberFormatException var4) {
         RegisterMenuAPI.getLogger(Level.WARNING, "you don´t use numbers inside this " + rgb);
         var4.printStackTrace();
      }

      return this;
   }

   public int getRed() {
      return this.red;
   }

   public int getGreen() {
      return this.green;
   }

   public int getBlue() {
      return this.blue;
   }

   public CreateItemStack setItemFlags(ItemFlag... itemFlags) {
      this.setItemFlags(Arrays.asList(itemFlags));
      return this;
   }

   public CreateItemStack setItemFlags(List<ItemFlag> itemFlags) {
      Validate.checkNotNull(itemFlags, "flags list is null");
      this.visibleItemFlags.addAll(itemFlags);
      return this;
   }

   public CreateItemStack setFlagsToHide(List<ItemFlag> itemFlags) {
      Validate.checkNotNull(itemFlags, "flags list is null");
      this.flagsToHide.addAll(itemFlags);
      return this;
   }

   public List<ItemFlag> getFlagsToHide() {
      return this.flagsToHide;
   }

   public boolean isCopyOfItem() {
      return this.copyOfItem;
   }

   public CreateItemStack setCopyOfItem(boolean copyItem) {
      this.copyOfItem = copyItem;
      return this;
   }

   public ItemStack makeItemStack() {
      ItemStack itemstack = this.checkTypeOfItem();
      return this.createItem(itemstack);
   }

   public ItemStack[] makeItemStackArray() {
      ItemStack itemstack = null;
      List<ItemStack> list = new ArrayList();
      if (this.itemArray != null) {
         Iterator var3 = this.itemArray.iterator();

         while(var3.hasNext()) {
            Object itemStringName = var3.next();
            itemstack = this.checkTypeOfItem(itemStringName);
            if (itemstack != null) {
               list.add(this.createItem(itemstack));
            }
         }
      }

      return itemstack != null ? (ItemStack[])list.toArray(new ItemStack[0]) : new ItemStack[]{new ItemStack(Material.AIR)};
   }

   
   private ItemStack createItem(ItemStack itemstack) {
      if (itemstack == null) {
         return new ItemStack(Material.AIR);
      } else {
         ItemStack itemstackNew = itemstack;
         RegisterNbtAPI nbtApi = RegisterMenuAPI.getNbtApi();
         if (!this.keepOldMeta) {
            itemstackNew = new ItemStack(itemstack.getType());
            if (this.keepAmount) {
               itemstackNew.setAmount(itemstack.getAmount());
            }
         }

         if (this.isCopyOfItem() && this.keepOldMeta) {
            itemstackNew = new ItemStack(itemstackNew);
         }

         if (!this.isAir(itemstackNew.getType())) {
            if (nbtApi != null) {
               Map<String, Object> metadataMap = this.getMetadataMap();
               Entry entitys;
               if (metadataMap != null) {
                  for(Iterator var5 = metadataMap.entrySet().iterator(); var5.hasNext(); itemstackNew = nbtApi.getCompMetadata().setMetadata(itemstackNew, (String)entitys.getKey(), entitys.getValue())) {
                     entitys = (Entry)var5.next();
                  }
               }
            }

            ItemMeta itemMeta = itemstackNew.getItemMeta();
            if (itemMeta != null) {
               if (this.displayName != null) {
                  itemMeta.setDisplayName(this.translateColors(this.displayName));
               }

               if (this.lore != null && !this.lore.isEmpty()) {
                  itemMeta.setLore(this.translateColors(this.lore));
               }

               this.addItemMeta(itemMeta);
            }

            itemstackNew.setItemMeta(itemMeta);
            if (!this.keepAmount) {
               itemstackNew.setAmount(this.amoutOfItems <= 0 ? 1 : this.amoutOfItems);
            }
         }

         return itemstackNew;
      }
   }

   public boolean isAir(Material material) {
      switch(material) {
      case AIR:
      case CAVE_AIR:
      case VOID_AIR:
      case LEGACY_AIR:
         return true;
      default:
         return false;
      }
   }

   private ItemStack checkTypeOfItem() {
      if (this.itemStack != null) {
         return this.itemStack;
      } else if (this.matrial != null) {
         return new ItemStack(this.matrial);
      } else {
         return this.stringItem != null ? this.checkTypeOfItem(this.stringItem) : null;
      }
   }

   private ItemStack checkTypeOfItem(Object object) {
      return getConvertItems().checkItem(object);
   }

   private void addItemMeta(ItemMeta itemMeta) {
      this.addBannerPatterns(itemMeta);
      this.addLeatherArmorColors(itemMeta);
      this.addFireworkEffect(itemMeta);
      this.addEnchantments(itemMeta);
      this.addBottleEffects(itemMeta);
      if (ServerVersion.newerThan(ServerVersion.v1_10)) {
         this.addUnbreakableMeta(itemMeta);
      }

      this.addCustomModelData(itemMeta);
      if (this.isShowEnchantments() || !this.getFlagsToHide().isEmpty() || this.isGlow()) {
         this.hideEnchantments(itemMeta);
      }

   }

   private void hideEnchantments(ItemMeta itemMeta) {
      if (!this.getFlagsToHide().isEmpty()) {
         itemMeta.addItemFlags((ItemFlag[])this.getFlagsToHide().toArray(new ItemFlag[0]));
      } else {
         itemMeta.addItemFlags((ItemFlag[])Arrays.stream(ItemFlag.values()).filter((itemFlag) -> {
            return !this.visibleItemFlags.contains(itemFlag);
         }).toArray((x$0) -> {
            return new ItemFlag[x$0];
         }));
      }

   }

   public boolean addEnchantments(ItemMeta itemMeta) {
      if (this.getEnchantments().isEmpty()) {
         return this.isGlow() ? itemMeta.addEnchant(Enchantment.SILK_TOUCH, 1, false) : false;
      } else {
         boolean haveEnchant = false;
         Iterator var3 = this.getEnchantments().entrySet().iterator();

         while(var3.hasNext()) {
            Entry<Enchantment, Pair<Integer, Boolean>> enchant = (Entry)var3.next();
            if (enchant == null) {
               RegisterMenuAPI.getLogger(Level.INFO, "Your enchantment are null.");
            } else {
               Pair<Integer, Boolean> level = (Pair)enchant.getValue();
               haveEnchant = itemMeta.addEnchant((Enchantment)enchant.getKey(), (Integer)level.getFirst() <= 0 ? 1 : (Integer)level.getFirst(), (Boolean)level.getSecond());
            }
         }

         if (this.isShowEnchantments() || !this.getFlagsToHide().isEmpty()) {
            this.hideEnchantments(itemMeta);
         }

         return haveEnchant;
      }
   }

   private void addBannerPatterns(ItemMeta itemMeta) {
      if (this.getPattern() != null && !this.getPattern().isEmpty()) {
         if (itemMeta instanceof BannerMeta) {
            BannerMeta bannerMeta = (BannerMeta)itemMeta;
            bannerMeta.setPatterns(this.getPattern());
         }

      }
   }

   private void addLeatherArmorColors(ItemMeta itemMeta) {
      if (this.getRgb() != null && this.getRed() >= 0) {
         if (itemMeta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta)itemMeta;
            leatherArmorMeta.setColor(Color.fromBGR(this.getBlue(), this.getGreen(), this.getRed()));
         }

      }
   }

   private void addBottleEffects(ItemMeta itemMeta) {
      if (itemMeta instanceof PotionMeta) {
         PotionMeta potionMeta = (PotionMeta)itemMeta;
         if (this.isWaterBottle()) {
            PotionData potionData = new PotionData(PotionType.WATER);
            potionMeta.setBasePotionData(potionData);
            return;
         }

         if (this.getPortionEffects() == null || this.getPortionEffects().isEmpty()) {
            return;
         }

         if (this.getRgb() == null || this.getRed() < 0 && this.getGreen() < 0 && this.getBlue() < 0) {
            RegisterMenuAPI.getLogger(Level.WARNING, "You have not set colors correctly, you have set this: " + this.getRgb() + " should be in this format Rgb: #,#,#");
            return;
         }

         potionMeta.setColor(Color.fromBGR(this.getBlue(), this.getGreen(), this.getRed()));
         this.getPortionEffects().forEach((portionEffect) -> {
            potionMeta.addCustomEffect(portionEffect, true);
         });
      }

   }

   private void addFireworkEffect(ItemMeta itemMeta) {
      if (this.getRgb() != null && (this.getRed() >= 0 || this.getGreen() >= 0 || this.getBlue() >= 0)) {
         if (itemMeta instanceof FireworkEffectMeta) {
            if (this.getRgb() == null || this.getRed() < 0 && this.getGreen() < 0 && this.getBlue() < 0) {
               RegisterMenuAPI.getLogger(Level.WARNING, "You have not set colors correctly, you have set this: " + this.getRgb() + " should be in this format Rgb: #,#,#");
               return;
            }

            FireworkEffectMeta fireworkEffectMeta = (FireworkEffectMeta)itemMeta;
            Builder builder = FireworkEffect.builder();
            builder.withColor(Color.fromBGR(this.getBlue(), this.getGreen(), this.getRed()));
            if (this.getFireworkEffects() != null && !this.getFireworkEffects().isEmpty()) {
               ((FireworkEffect)this.getFireworkEffects().get(0)).getFadeColors();
               builder.flicker(false).with(Type.BURST).trail(true).withFade(new Color[0]);
            }

            fireworkEffectMeta.setEffect(builder.build());
         }

      }
   }

   private void addUnbreakableMeta(ItemMeta itemMeta) {
      itemMeta.setUnbreakable(this.isUnbreakable());
   }

   private void addCustomModelData(ItemMeta itemMeta) {
      if (this.getCustomModeldata() > 0) {
         itemMeta.setCustomModelData(this.getCustomModeldata());
      }

   }

   private boolean isShowEnchantments() {
      return this.showEnchantments;
   }

   private List<String> translateColors(List<String> rawLore) {
      List<String> lores = new ArrayList();
      Iterator var3 = rawLore.iterator();

      while(var3.hasNext()) {
         String lore = (String)var3.next();
         if (lore != null) {
            lores.add(TextTranslator.toSpigotFormat(lore));
         }
      }

      return lores;
   }

   private String translateColors(String rawSingelLine) {
      return TextTranslator.toSpigotFormat(rawSingelLine);
   }

   public static ItemStack createItemStackAsOne(Material material) {
      ItemStack itemstack = null;
      if (material != null) {
         itemstack = new ItemStack(material);
      }

      return createItemStackAsOne(itemstack != null ? itemstack : new ItemStack(Material.AIR));
   }

   public static ItemStack createItemStackAsOne(ItemStack itemstacks) {
      ItemStack itemstack = null;
      if (itemstacks != null && !itemstacks.getType().equals(Material.AIR)) {
         itemstack = itemstacks.clone();
         ItemMeta meta = itemstack.getItemMeta();
         itemstack.setItemMeta(meta);
         itemstack.setAmount(1);
      }

      return itemstack != null ? itemstack : new ItemStack(Material.AIR);
   }

   public static ItemStack[] createItemStackAsOne(ItemStack[] itemstacks) {
      ItemStack itemstack = null;
      if (itemstacks != null) {
         ItemStack[] var2 = itemstacks;
         int var3 = itemstacks.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            ItemStack item = var2[var4];
            if (item.getType() != Material.AIR) {
               itemstack = item.clone();
               ItemMeta meta = itemstack.getItemMeta();
               itemstack.setItemMeta(meta);
               itemstack.setAmount(1);
               return new ItemStack[]{itemstack};
            }
         }
      }

      return new ItemStack[]{new ItemStack(Material.AIR)};
   }

   public static ItemStack createItemStackWhitAmount(Material matrial, int amount) {
      ItemStack itemstack = null;
      if (matrial != null) {
         itemstack = new ItemStack(matrial);
         itemstack.setAmount(amount);
      }

      return itemstack != null ? itemstack : new ItemStack(Material.AIR);
   }

   protected static ConvertToItemStack getConvertItems() {
      if (convertItems == null) {
         convertItems = new ConvertToItemStack();
      }

      return convertItems;
   }

   public static List<String> formatColors(List<String> rawLore) {
      List<String> lores = new ArrayList();
      Iterator var2 = rawLore.iterator();

      while(var2.hasNext()) {
         String lore = (String)var2.next();
         lores.add(translateHexCodes(lore));
      }

      return lores;
   }

   public static String formatColors(String rawSingelLine) {
      return translateHexCodes(rawSingelLine);
   }

   private static String translateHexCodes(String textTranslate) {
      return TextTranslator.toSpigotFormat(textTranslate);
   }

   private MetaDataWraper getMetadata() {
      return this.metadata;
   }

   private Map<String, Object> getMetadataMap() {
      return this.metadata != null ? this.metadata.getMetaDataMap() : null;
   }

   // $FF: synthetic method
   CreateItemStack(CreateItemStack.Bulider x0, Object x1) {
      this(x0);
   }

   private static class Bulider {
      private ItemStack itemStack;
      private Material matrial;
      private String stringItem;
      private Iterable<?> itemArray;
      private final String displayName;
      private final List<String> lore;

      private Bulider(ItemStack itemStack, String displayName, List<String> lore) {
         this.itemStack = itemStack;
         this.displayName = displayName;
         this.lore = lore;
      }

      private Bulider(Material matrial, String displayName, List<String> lore) {
         this.matrial = matrial;
         this.displayName = displayName;
         this.lore = lore;
      }

      private Bulider(String stringItem, String displayName, List<String> lore) {
         this.stringItem = stringItem;
         this.displayName = displayName;
         this.lore = lore;
      }

      private <T> Bulider(Iterable<T> itemArray, String displayName, List<String> lore) {
         this.itemArray = itemArray;
         this.displayName = displayName;
         this.lore = lore;
      }

      public CreateItemStack build() {
         return new CreateItemStack(this);
      }

      // $FF: synthetic method
      Bulider(ItemStack x0, String x1, List x2, Object x3) {
         this(x0, x1, x2);
      }

      // $FF: synthetic method
      Bulider(Iterable x0, String x1, List x2, Object x3) {
         this(x0, x1, x2);
      }
   }
}
