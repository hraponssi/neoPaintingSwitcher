package plugin.arcwolf.neopaintingswitch;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class neoPaintingSwitch extends JavaPlugin {

    public static final Logger LOGGER = Logger.getLogger("Minecraft.neoPaintingSwitch");

    public WorldGuardPlugin wgp;

    public boolean free4All = false;
    public boolean worldguard = false;
    
    public boolean plotsquared = false;

    private boolean debug = false;
    private Server server;
    private PluginDescriptionFile pdfFile;
    private PluginManager pm;
    private String pluginName;

    @Override
    public void onEnable() {
        server = this.getServer();
        pdfFile = getDescription();
        pluginName = pdfFile.getName();
        pm = server.getPluginManager();

        PluginDescriptionFile pdfFile = getDescription();
        setupConfig();
        wgp = getWorldGuard();
        worldguard = wgp != null;
        LOGGER.info("Worldguard detected and integrated: " + worldguard);
        
        plotsquared = getPlotSquared();
        LOGGER.info("PlotSquared detected and integrated: " + plotsquared);

        pm.registerEvents(new npPlayerEvent(this), this);
        pm.registerEvents(new npPaintingBreakEvent(), this);

        LOGGER.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
    }

    public void setupConfig() {
        File configFile = new File(this.getDataFolder() + "/config.yml");
        FileConfiguration config = this.getConfig();
        if (!configFile.exists()) {
            config.set("free4All", Boolean.valueOf(false));
            config.set("debug", Boolean.valueOf(debug));
            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        free4All = config.getBoolean("free4All", false);
        debug = config.getBoolean("debug", false);
    }

    @Override
    public void onDisable() {
        PluginDescriptionFile pdfFile = getDescription();
        LOGGER.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled!");
    }

    // get worldguard plugin
    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) { return null; }

        return (WorldGuardPlugin) plugin;
    }
    
    // get plotsquared plugin
    private boolean getPlotSquared() {
        Plugin plugin = getServer().getPluginManager().getPlugin("PlotSquared");

        // PlotSquared may not be loaded
        if (plugin == null) { return false; }

        return true;
    }

    public boolean hasPermission(Player player, String permission) {
        return player.isOp() || player.hasPermission(permission);
    }

}
