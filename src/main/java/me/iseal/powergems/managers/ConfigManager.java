package me.iseal.powergems.managers;

import me.iseal.powergems.Main;
import me.iseal.powergems.misc.AbstractConfigManager;
import me.iseal.powergems.misc.ExceptionHandler;
import me.iseal.powergems.misc.Utils;
import org.bukkit.Bukkit;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigManager {

    private final ArrayList<Class< ? extends AbstractConfigManager>> registeredConfigurations = new ArrayList<>(5);
    private final HashMap<Class<? extends AbstractConfigManager>, Object> registeredConfigInstances = new HashMap<>(5);
    private final Logger l = Bukkit.getLogger();

    public void setUpConfig() {
        Utils.findAllClassesInPackage("me.iseal.powergems.managers.Configuration").forEach(this::addConfigClass);
        Class<? extends AbstractConfigManager> currentToDebug = null;
        try {
            for (Class<? extends AbstractConfigManager> currentClass : registeredConfigurations) {
                currentToDebug = currentClass;
                Object instance = getRegisteredConfigInstance(currentClass);
                Method init = currentClass.getMethod("setUpConfig");
                init.invoke(instance);
            }
        } catch(Exception ex){
            ExceptionHandler.getInstance().dealWithException(ex, Level.SEVERE, "SET_UP_CONFIG", currentToDebug.getName());
        }
    }

    /*
    * Gets the instance of a registered config object
    *
    * @return The instance, registering it if needed or null if it is not a possible config object
     */
    public <T extends AbstractConfigManager> T getRegisteredConfigInstance(Class<T> clazz) {
        if (!isPossibleConfigClass(clazz)) {
            return null;
        }

        if (!registeredConfigurations.contains(clazz)){
            ExceptionHandler.getInstance().dealWithException(new RuntimeException("EARLY_ASK_FOR_CONFIG_INSTANCE"), Level.WARNING, "EARLY_ASK_FOR_CONFIG_INSTANCE", clazz.getName());
            // oh, god (attempt desperate fix(really f-ing bad code))
            Utils.findAllClassesInPackage("me.iseal.powergems.managers.Configuration").forEach(this::addConfigClass);
        }
        if (!registeredConfigInstances.containsKey(clazz)){
            registerConfigInstance(clazz);
        }
        return clazz.cast(registeredConfigInstances.get(clazz));
    }

    private void registerConfigInstance(Class<? extends AbstractConfigManager> clazz) {
        if (!isPossibleConfigClass(clazz)) {
            return;
        }
        System.out.println("asking for instance of " + clazz.getName());
        try {
            if (!registeredConfigInstances.containsKey(clazz)) {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                registeredConfigInstances.put(clazz, instance);
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ExceptionHandler.getInstance().dealWithException(ex, Level.SEVERE, "RESISTER_CONFIG_INSTANCE");
        }
    }

    public static String getConfigFolderPath(){
        return Main.getPlugin().getDataFolder() + File.separator + "config" + File.separator;
    }

    public void resetConfig() {
        for (Class<? extends AbstractConfigManager> currentClass : registeredConfigurations) {
            try {
                Object instance = getRegisteredConfigInstance(currentClass);
                Method init = currentClass.getMethod("resetConfig");
                init.invoke(instance);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                ExceptionHandler.getInstance().dealWithException(ex, Level.SEVERE, "RESET_CONFIG", currentClass);
            }
        }
    }

    public void reloadConfig() {
        for (Class<? extends AbstractConfigManager> currentClass : registeredConfigurations) {
            try {
                Object instance = getRegisteredConfigInstance(currentClass);
                Method init = currentClass.getMethod("reloadConfig");
                init.invoke(instance);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                ExceptionHandler.getInstance().dealWithException(ex, Level.SEVERE, "RELOAD_CONFIG", currentClass);
            }
        }
    }

    private boolean isPossibleConfigClass(Class<?> clazz) {
        return AbstractConfigManager.class.isAssignableFrom(clazz);
    }

    private void addConfigClass(Class<?> clazz) {
        if (isPossibleConfigClass(clazz)) {
            registeredConfigurations.add((Class<? extends AbstractConfigManager>) clazz);
        }
    }

}
