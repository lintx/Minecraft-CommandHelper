package org.lintx.plugins.modules.command_helper.craft;

import org.bukkit.command.*;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;
import org.lintx.plugins.modules.command_helper.command.CTree;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@SuppressWarnings("unused")
public class CommandHelper implements TabExecutor {
    private final JavaPlugin plugin;
    private final List<Class<?>> pluginClass = new ArrayList<>();
    private final Map<Class<?>,Object> findClass = new HashMap<>();
    private final CTree root;
    private final Logger logger;

    public CommandHelper(JavaPlugin plugin){
        this.plugin = plugin;
        logger = new Logger(plugin);
        root = CTree.newRoot(logger);
    }

    public CommandHelper debugLog(boolean debug){
        logger.debugLog(debug);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public CommandHelper add(){
        add(plugin.getClass().getPackage());
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public CommandHelper add(Class<?> clz){
        if (!findClass.containsKey(clz) && hasAnnotation(clz)){
            //检查是否有反射
            logger.fine("find class:" + clz.getName());
            findClass.put(clz,null);
        }
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public CommandHelper add(Object obj){
        add(obj.getClass());
        Class<?> clz = obj.getClass();
        if (hasAnnotation(clz)){
            if (!findClass.containsKey(clz) || findClass.get(clz)==null){
                logger.fine("find class:" + clz.getName());
                findClass.put(clz,obj);
            }
        }
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public CommandHelper add(Package pkg){
        if (pluginClass.size()==0){
            loadClasses();
        }
        String pkgStr = pkg.getName();
        for (Class<?> clz : pluginClass) {
            if (clz.getPackage().getName().startsWith(pkgStr)) add(clz);
        }
        return this;
    }

    private boolean hasAnnotation(Class<?> clz){
        return clz.isAnnotationPresent(org.lintx.plugins.modules.command_helper.annotation.CommandHelper.class);
    }

    private void loadClasses() {
        logger.fine("find all class with plugin.");
        ClassLoader loader = plugin.getClass().getClassLoader();
        if (!(loader instanceof URLClassLoader)) return;
        URLClassLoader classLoader = (URLClassLoader) loader;
        for (URL url : classLoader.getURLs()) {
            try(JarFile jar = new JarFile(url.getFile())) {
                Enumeration<JarEntry> entries = jar.entries();
                while(entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String file = entry.getName();
                    if (file.endsWith(".class")) {
                        String clzStr = file.replace(".class", "").replace("/", ".");
                        if (clzStr.contains("$")) {
                            clzStr = clzStr.split("\\$")[0];
                        }

                        Class<?> clz = Class.forName(clzStr);
                        logger.fine("find class :" + clz.getName() + " with the plugin.");
                        pluginClass.add(clz);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unused")
    public void register(){
        if (plugin.getServer() instanceof CraftServer){
            fillObject();
            Map<String, Map<String, Object>> map = root.getCommandMap();

            CraftServer server = (CraftServer) plugin.getServer();

            List<Command> commands = new ArrayList<>();

            plugin.getDescription().getCommands().putAll(map);
            List<Command> pluginCommands = PluginCommandYamlParser.parse(plugin);

            for (Command command : pluginCommands) {
                if (command instanceof PluginCommand){
                    PluginCommand c = (PluginCommand) command;
                    if (map.containsKey(c.getName())){
                        logger.fine("registered command:" + c.getName());
                        c.setExecutor(this);
                        c.setTabCompleter(this);
                        commands.add(c);
                    }
                }
            }

            server.getCommandMap().registerAll(plugin.getDescription().getName(),commands);
            try {
                //同步客户端的命令列表以在动态加载插件时也能在自动提示中显示新注册的插件命令
                Class<CraftServer> clz = CraftServer.class;
                Method method = clz.getDeclaredMethod("syncCommands");
                method.setAccessible(true);
                method.invoke(server);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            logger.warning("not supported this Server!");
        }
    }

    private void fillObject(){
        Class<?> pluginClz = plugin.getClass();
        Set<Class<?>> keys = findClass.keySet();
        for (Class<?> clz : keys) {
            Object obj = findClass.get(clz);
            if (obj==null){
                try {
                    Constructor<?> constructor = clz.getConstructor(pluginClz);
                    obj = constructor.newInstance(plugin);
                    findClass.put(clz,obj);
                }catch (Exception e){
                    try {
                        obj = clz.newInstance();
                        findClass.put(clz,obj);
                    }catch (Exception exception){
                        e.printStackTrace();
                        exception.printStackTrace();
                    }
                }
            }
            if (obj!=null){
                root.loadTree(clz,obj);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        logger.fine(commandSender.getName() + " executor command:/" + command.getName() + " " + String.join(" ",strings));
        CommandContext context = new CommandContext(plugin,commandSender,command,s,strings);
        return root.onCommand(context);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        CommandContext context = new CommandContext(plugin,commandSender,command,s,strings);
        return root.onTabComplete(context);
    }
}
