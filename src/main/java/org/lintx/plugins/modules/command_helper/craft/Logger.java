package org.lintx.plugins.modules.command_helper.craft;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class Logger extends PluginLogger {

    public Logger(Plugin plugin) {
        super(plugin);
        debugLog(false);
    }

    @Override
    public void log(LogRecord logRecord) {
        String prefix = "[CommandHelper] ";
        logRecord.setMessage(prefix + logRecord.getMessage());
        super.log(logRecord);
    }

    void debugLog(boolean debug){
        if (debug){
            setLevel(Level.WARNING);
        }else {
            setLevel(Level.ALL);
        }
    }
}
