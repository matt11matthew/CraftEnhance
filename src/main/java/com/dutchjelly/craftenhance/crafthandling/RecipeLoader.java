package com.dutchjelly.craftenhance.crafthandling;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.util.ServerRecipeTranslator;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RecipeLoader implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(CraftEnhance.self().getConfig().getBoolean("learn-recipes"))
            Adapter.DiscoverRecipes(e.getPlayer(), getLoadedServerRecipes());
    }

    //Ensure one instance
    private static RecipeLoader instance = null;
    public static RecipeLoader getInstance(){
        return instance == null ? instance = new RecipeLoader(Bukkit.getServer()) : instance;
    }

    @Getter
    private List<Recipe> serverRecipes = new ArrayList<>();

    @Getter
    private List<Recipe> disabledServerRecipes = new ArrayList<>();

    //A map of the server recipes that represent the loaded custom recipes (mapped to their keys).
    private Map<String, Recipe> loaded = new HashMap<>();
    private Server server;


    //Recipes are grouped in groups of 'similar' recipes. A server can contain multiple recipes with the same
    //recipe with different results. Think of a diamond chestplate recipe vs a custom diamond chestplate recipe
    //with custom diamonds. Or think of a shapeless recipe of a block of diamond vs a shaped recipe of the block of
    //diamond.
    @NonNull @Getter
    private List<RecipeGroup> groupedRecipes = new ArrayList<>();

    private RecipeLoader(Server server){
        this.server = server;
        server.recipeIterator().forEachRemaining(serverRecipes::add);
    }



    //Adds or merges group with existing group.
    private RecipeGroup addGroup(RecipeGroup newGroup){

        if(newGroup == null) return null;

//        if(!newGroup.getServerRecipes().isEmpty()){
//            //look for merge
//            for(RecipeGroup group : groupedRecipes){
//                Debug.Send("Looking if two enhanced recipes are similar for merge.");
//                if(newGroup.getEnhancedRecipes().stream().anyMatch(x -> group.getEnhancedRecipes().stream().anyMatch(x::isSimilar))){
//                    return group.mergeWith(newGroup);
//                }
//            }
//        }

        for(RecipeGroup group : groupedRecipes){
//            Debug.Send("Looking if two enhanced recipes are similar for merge.");
            if(newGroup.getEnhancedRecipes().stream().anyMatch(x -> group.getEnhancedRecipes().stream().anyMatch(x::isSimilar))){
                return group.mergeWith(newGroup);
            }
        }

        groupedRecipes.add(newGroup);
        return newGroup;
    }

    public RecipeGroup findGroup(EnhancedRecipe recipe){
        return groupedRecipes.stream().filter(x -> x.getEnhancedRecipes().contains(recipe)).findFirst().orElse(null);
    }

    //Find groups that contain at least one recipe that maps to result.
    public List<RecipeGroup> findGroupsByResult(ItemStack result){
        List<RecipeGroup> originGroups = new ArrayList<>();
        for(RecipeGroup group : groupedRecipes){
            if(group.getEnhancedRecipes().stream().anyMatch(x -> result.equals(x.getResult())))
                originGroups.add(group);
            else if(group.getServerRecipes().stream().anyMatch(x -> result.equals(x.getResult())))
                originGroups.add(group);
        }
        return originGroups;
    }

    public <T extends EnhancedRecipe> RecipeGroup findGroupBySource(ItemStack source){
        for(RecipeGroup group : groupedRecipes){
            try{
                if(group.getEnhancedRecipes().stream().anyMatch(x -> (T)x != null && Arrays.stream(x.getContent()).anyMatch(y -> source.equals(y))))
                    return group;
            } catch(ClassCastException e){}
        }
        return null;
    }

    public <T extends EnhancedRecipe> List<RecipeGroup> findGroupsBySource(ItemStack source){
        List<RecipeGroup> originGroups = new ArrayList<>();
        for(RecipeGroup group : groupedRecipes){
            try{
                if(group.getEnhancedRecipes().stream().anyMatch(x -> (T)x != null && Arrays.stream(x.getContent()).anyMatch(y -> source.equals(y))))
                    originGroups.add(group);
            } catch(ClassCastException e){}

        }
        return originGroups;
    }

    public boolean isLoadedAsServerRecipe(EnhancedRecipe recipe){
        return loaded.containsKey(recipe.getKey());
    }

    private void unloadCehRecipes() {
        Iterator<Recipe> it = server.recipeIterator();
        while(it.hasNext()) {
            Recipe r = it.next();
            if(Adapter.ContainsSubKey(r, ServerRecipeTranslator.KeyPrefix)) {
                it.remove();
            }
        }
    }

    private void unloadRecipe(Recipe r) {
        Iterator<Recipe> it = server.recipeIterator();
        while(it.hasNext()) {
            Recipe currentRecipe = it.next();
            if(currentRecipe.equals(r)) {
                it.remove();
                return;
            }
        }
    }

    public void unloadAll(){
        disabledServerRecipes.forEach(x ->
            enableServerRecipe(x)
        );
        groupedRecipes.clear();
        serverRecipes.clear();
        loaded.clear();
        unloadCehRecipes();
    }

    public void unloadRecipe(EnhancedRecipe recipe){
        RecipeGroup group = findGroup(recipe);
        if(group == null) {
            printGroupsDebugInfo();
            Bukkit.getLogger().log(Level.SEVERE, "Could not unload recipe from groups because it doesn't exist.");
            return;
        }
        Recipe serverRecipe = loaded.get(recipe.getKey());

        //Only unload from server if there are no similar server recipes.
        if(serverRecipe != null){
            loaded.remove(recipe.getKey());
            unloadRecipe(serverRecipe);
        }

        //TODO update grouping. This is not a priority because the injector compares the recipes either way.
        //Remove entire recipe group if it's the last enhanced recipe, or remove a single recipe from the group.
        if(group.getEnhancedRecipes().size() == 1)
            groupedRecipes.removeIf(x -> x.getEnhancedRecipes().contains(recipe));
        else group.getEnhancedRecipes().remove(recipe);
        Debug.Send("unloaded a recipe");
        printGroupsDebugInfo();
    }

    public void loadRecipe(@NonNull EnhancedRecipe recipe){

        if(recipe.validate() != null) {
            Messenger.Error("There's an issue with recipe " + recipe.getKey() + ": " + recipe.validate());
            return;
        }

        if(loaded.containsKey(recipe.getKey()))
            unloadRecipe(recipe);

        List<Recipe> similarServerRecipes = new ArrayList<>();
        for(Recipe r : serverRecipes){
            if(recipe.isSimilar(r)){
                similarServerRecipes.add(r);
            }
        }
        Recipe alwaysSimilar = null;
        for(Recipe r : similarServerRecipes){
            if(recipe.isAlwaysSimilar(r)){
                alwaysSimilar = r;
                break;
            }
        }
        //Only load the recipe if there is not a server recipe that's always similar.
        if(alwaysSimilar == null){
            Recipe serverRecipe = recipe.getServerRecipe();
            server.addRecipe(serverRecipe);
            Debug.Send("Added server recipe for " + serverRecipe.getResult().toString());
            loaded.put(recipe.getKey(), serverRecipe);
            if(CraftEnhance.self().getConfig().getBoolean("learn-recipes"))
                Bukkit.getServer().getOnlinePlayers().forEach(x -> Adapter.DiscoverRecipes(x, Arrays.asList(serverRecipe)));
        }else{
            Debug.Send("Didn't add server recipe for " + recipe.getKey() + " because a similar one was already loaded: " + alwaysSimilar.toString() + " with the result " + alwaysSimilar.getResult().toString());
        }

        RecipeGroup group = new RecipeGroup();
        group.setEnhancedRecipes(Arrays.asList(recipe));
        group.setServerRecipes(similarServerRecipes);
        addGroup(group);
    }

    public List<EnhancedRecipe> getLoadedRecipes(){
        return groupedRecipes.stream().flatMap(x -> x.getEnhancedRecipes().stream()).distinct().collect(Collectors.toList());
    }

    public List<Recipe> getLoadedServerRecipes(){
        return new ArrayList<>(loaded.values());
    }

    public void printGroupsDebugInfo(){
        for(RecipeGroup group : groupedRecipes){
            Debug.Send("group of grouped recipes:");
            Debug.Send("   enhanced recipes: " + group.getEnhancedRecipes().stream().filter(x -> x != null).map(x -> x.getResult().toString()).collect(Collectors.joining(", ")));
            Debug.Send("   server recipes: " + group.getServerRecipes().stream().filter(x -> x != null).map(x -> x.getResult().toString()).collect(Collectors.joining(", ")));
        }
    }

    public boolean disableServerRecipe(Recipe r){
        if(serverRecipes.contains(r)) {
            Debug.Send("[Recipe Loader] disabling server recipe for " + r.getResult().getType().name());

            serverRecipes.remove(r);
            disabledServerRecipes.add(r);

            groupedRecipes.forEach(x -> {
                if(x.getServerRecipes().contains(r))
                    x.getServerRecipes().remove(r);
            });
            unloadRecipe(r);
            return true;
        }
        return false;
    }

    public boolean enableServerRecipe(Recipe r){
        if(!serverRecipes.contains(r)) {
            Debug.Send("[Recipe Loader] enabling server recipe for " + r.getResult().getType().name());
            serverRecipes.add(r);
            disabledServerRecipes.remove(r);
            server.addRecipe(r);
            return true;
        }
        return false;
    }

    public void disableServerRecipes(List<Recipe> disabledServerRecipes){
        //No need to be efficient here, this'll only run once.
        disabledServerRecipes.forEach(x -> disableServerRecipe(x));
    }
}
