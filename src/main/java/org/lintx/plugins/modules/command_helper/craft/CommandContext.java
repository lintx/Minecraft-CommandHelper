package org.lintx.plugins.modules.command_helper.craft;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.lintx.plugins.modules.command_helper.annotation.CommandMapping;
import org.lintx.plugins.modules.command_helper.annotation.CommandVariableType;

import java.lang.reflect.Type;
import java.util.*;

public class CommandContext extends org.lintx.plugins.modules.command_helper.command.CommandContext {
    CommandSender sender;
    Command command;
    JavaPlugin plugin;
    String label;
    String[] args;

    CommandContext(JavaPlugin plugin, CommandSender sender, Command command, String label, String[] args){
        this.plugin = plugin;
        this.sender = sender;
        this.command = command;
        this.label = label;
        this.args = args;
        path.add(command.getName());
        path.addAll(Arrays.asList(args));
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            players.add(player.getName().toLowerCase(Locale.ROOT));
        }
    }

    @Override
    protected boolean senderHasPermission(CommandMapping mapping) {
        return sender.hasPermission(mapping.permission());
    }

    @Override
    protected boolean senderIsConsole() {
        return sender instanceof ConsoleCommandSender;
    }

    @Override
    protected boolean senderIsPlayer() {
        return sender instanceof Player;
    }

    @Override
    protected void sendMessageToSender(String[] message) {
        for (String str:message){
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',str));
        }
    }

    @Override
    protected Object structureArgs(CommandVariableType commandVariableType, CommandMapping mapping, Class<?> cla, Type type, String name) {
        switch (commandVariableType){
            case SENDER:
                return cla==CommandSender.class ? sender : defaultData(cla);
            case COMMAND:
                return  cla==Command.class ? command : defaultData(cla);
            case LABEL:
                return cla==String.class ? label : defaultData(cla);
            case PLUGIN:
                return (cla==JavaPlugin.class || JavaPlugin.class.isAssignableFrom(cla)) ? plugin : defaultData(cla);
            case HAS_PERMISSION:
                return (cla==boolean.class || cla==Boolean.class) ? sender.hasPermission(mapping.permission()) : defaultData(cla);
            case DEFAULT:
                if (cla==CommandSender.class){
                    return sender;
                }else if (cla==Command.class){
                    return command;
                }else if (cla==JavaPlugin.class || JavaPlugin.class.isAssignableFrom(cla)){
                    return plugin;
                }else if (cla==String.class && name.equals("label") && !params.containsKey("label")){
                    return label;
                }else if (cla==String[].class && name.equals("args") && !params.containsKey("args")){
                    return args;
                }else if (cla==String.class && name.equals("permission") && !params.containsKey("permission")){
                    return mapping.permission();
                }else if ((cla==boolean.class || cla==Boolean.class) && name.equals("hasPermission") && !params.containsKey("hasPermission")){
                    return sender.hasPermission(mapping.permission());
                }else {
                    return parseData(cla,type,params.get(name));
                }
            default:
                return defaultData(cla);
        }
    }

    protected Object parseData(Class<?> cla, Type type, String value){
        Object result = super.parseData(cla,type,value);
        if (result==null && cla== Player.class){
            return plugin.getServer().getPlayer(value.toLowerCase(Locale.ROOT));
        }
        return result;
    }
}
