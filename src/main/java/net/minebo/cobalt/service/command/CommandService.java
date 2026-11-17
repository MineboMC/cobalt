package net.minebo.cobalt.service.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.contexts.ContextResolver;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.service.command.defaults.command.ChannelCommand;
import net.minebo.cobalt.service.command.defaults.command.NpcCommand;
import net.minebo.cobalt.service.command.defaults.command.ProjectileCommand;
import net.minebo.cobalt.service.command.defaults.completion.DyeColorCompletionHandler;
import net.minebo.cobalt.service.command.defaults.completion.MaterialCompletionHandler;
import net.minebo.cobalt.service.command.defaults.context.ChatColorContext;
import net.minebo.cobalt.service.command.defaults.context.CommandFlagsContext;
import net.minebo.cobalt.service.command.defaults.context.EnchantmentContext;
import net.minebo.cobalt.service.command.defaults.context.HologramContext;
import net.minebo.cobalt.service.command.defaults.context.NpcContext;
import net.minebo.cobalt.service.command.defaults.context.OnlinePlayerContext;
import net.minebo.cobalt.service.command.defaults.context.UUIDContext;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class CommandService extends CService {

    public PaperCommandManager commandManager;

    private final Map<Plugin, PaperCommandManager> managers = new HashMap<>();

    @Override
    public String getName() {
        return "Commands";
    }

    @Override
    public void onEnable() {
        this.commandManager = getManager(Cobalt.getInstance());
        registerAll(Cobalt.getInstance());
    }

    public PaperCommandManager getManager(Plugin plugin) {
        return managers.computeIfAbsent(plugin, p -> {
            PaperCommandManager manager = new PaperCommandManager(p); // Commands register under p, so each plugin gets its own manager.
            manager.enableUnstableAPI("help");
            manager.setHelpFormatter(new CobaltHelpFormatter(manager));
            registerDefaults(manager);
            return manager;
        });
    }

    private void registerDefaults(PaperCommandManager manager) {
        new ChatColorContext().register(manager);
        new OnlinePlayerContext().register(manager);
        new UUIDContext().register(manager);
        new EnchantmentContext().register(manager);
        new CommandFlagsContext().register(manager);
        new HologramContext().register(manager);
        new NpcContext().register(manager);

        // ACF already resolves enums, so these only need completions.
        CommandCompletions<?> completions = manager.getCommandCompletions();
        completions.registerCompletion("materials", new MaterialCompletionHandler());
        completions.registerCompletion("dyecolors", new DyeColorCompletionHandler());

        NpcCommand.registerCompletions(manager);
        ProjectileCommand.registerCompletions(manager);
        ChannelCommand.registerCompletions(manager);

    }

    public void unregisterAll(Plugin plugin) {
        PaperCommandManager manager = managers.remove(plugin);
        if (manager != null) {
            manager.unregisterCommands();
        }
    }

    public void registerCompletion(Plugin plugin, String id, CommandCompletions.CommandCompletionHandler handler) {
        getManager(plugin).getCommandCompletions().registerCompletion(id, handler);
    }

    public void registerContext(Plugin plugin, CommonCommandContext<?> context) {
        context.register(getManager(plugin));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void registerContext(Plugin plugin, Class clazz, ContextResolver resolver) {
        getManager(plugin).getCommandContexts().registerContext(clazz, resolver);
    }

    public void registerCommand(Plugin plugin, BaseCommand command) {
        getManager(plugin).registerCommand(command);
    }

    /** Scans the plugin's own package (and subpackages) for BaseCommand subclasses. */
    public void registerAll(Plugin plugin) {
        registerCommandsInPackage(plugin, plugin.getClass().getPackage().getName());
    }

    public void registerCommandsInPackage(Plugin plugin, String packageName) {
        ClassLoader loader = plugin.getClass().getClassLoader();
        PaperCommandManager manager = getManager(plugin);

        Reflections reflections;
        try {
            reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage(packageName, loader))
                    .setClassLoaders(new ClassLoader[]{loader})
                    .setScanners(Scanners.SubTypes)
                    .filterInputsBy(new FilterBuilder().includePackage(packageName)));
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to scan " + packageName + " for commands");
            e.printStackTrace();
            return;
        }

        for (Class<? extends BaseCommand> commandClass : reflections.getSubTypesOf(BaseCommand.class)) {
            if (commandClass.isInterface() || Modifier.isAbstract(commandClass.getModifiers())) {
                continue;
            }

            // Per-command try/catch so one bad command doesn't stop the rest.
            try {
                Constructor<? extends BaseCommand> constructor = commandClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                manager.registerCommand(constructor.newInstance());

            } catch (Exception e) {
                plugin.getLogger().severe("Failed to register " + commandClass.getName());
                e.printStackTrace();
            }
        }
    }
}