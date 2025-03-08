package net.minebo.cobalt.acf;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.contexts.ContextResolver;
import java.lang.reflect.Modifier;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;

public class ACFCommandController {
   public static PaperCommandManager commandController;

   public static void registerCompletion(String id, CommandCompletions.CommandCompletionHandler handler) {
      commandController.getCommandCompletions().registerCompletion(id, handler);
   }

   public static void registerContext(Class clazz, ContextResolver resolver) {
      commandController.getCommandContexts().registerContext(clazz, resolver);
   }

   public static void registerCommand(BaseCommand command) {
      commandController.registerCommand(command);
   }

   public static void registerAll(Plugin plugin) {
      registerCommandsInPackage(plugin.getClass().getPackage().getName());
   }

   public static void registerCommandsInPackage(String packageName) {
      try {
         Reflections reflections = new Reflections(packageName, new Scanner[0]);

         for(Class commandClass : reflections.getSubTypesOf(BaseCommand.class)) {
            if (!commandClass.isInterface() && !Modifier.isAbstract(commandClass.getModifiers())) {
               BaseCommand commandInstance = (BaseCommand)commandClass.getDeclaredConstructor().newInstance();
               registerCommand(commandInstance);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

   }
}
