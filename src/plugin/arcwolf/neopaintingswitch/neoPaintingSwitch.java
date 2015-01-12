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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.bukkit.PermissionsEx;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.CalculableType;
import net.milkbowl.vault.permission.Permission;
import org.anjocaido.groupmanager.GroupManager;

public class neoPaintingSwitch extends JavaPlugin {

    public static final Logger LOGGER = Logger.getLogger("Minecraft.neoPaintingSwitch");

    private GroupManager groupManager;
    private net.milkbowl.vault.permission.Permission vaultPerms;
    private Permissions permissionsPlugin;
    private PermissionsEx permissionsExPlugin;
    private de.bananaco.bpermissions.imp.Permissions bPermissions;
    public WorldGuardPlugin wgp;

    public boolean free4All = false;
    public boolean worldguard = false;

    private boolean permissionsEr = false;
    private boolean permissionsSet = false;
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
        getPermissionsPlugin();
        wgp = getWorldGuard();
        worldguard = wgp != null;

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

    public boolean hasPermission(Player player, String permission) {
        if (debug) { // For use with permissions debugging
            getPermissionsPlugin();
            if (vaultPerms != null) {
                String pName = player.getName();
                String gName = vaultPerms.getPrimaryGroup(player);
                Boolean permissions = vaultPerms.has(player, permission);
                LOGGER.info("Vault permissions, group for '" + pName + "' = " + gName);
                LOGGER.info("Permission for " + permission + " is " + permissions);
            }
            else if (groupManager != null) {
                String pName = player.getName();
                String gName = groupManager.getWorldsHolder().getWorldData(player.getWorld().getName()).getPermissionsHandler().getGroup(player.getName());
                Boolean permissions = groupManager.getWorldsHolder().getWorldPermissions(player).has(player, permission);
                LOGGER.info("group for '" + pName + "' = " + gName);
                LOGGER.info("Permission for " + permission + " is " + permissions);
                LOGGER.info("");
                LOGGER.info("permissions available to '" + pName + "' = " + groupManager.getWorldsHolder().getWorldData(player.getWorld().getName()).getGroup(gName).getPermissionList());
            }
            else if (permissionsPlugin != null) {
                String pName = player.getName();
                String wName = player.getWorld().getName();
                String gName = Permissions.Security.getGroup(wName, pName);
                Boolean permissions = Permissions.Security.permission(player, permission);
                LOGGER.info("Niji permissions, group for '" + pName + "' = " + gName);
                LOGGER.info("Permission for " + permission + " is " + permissions);
            }
            else if (permissionsExPlugin != null) {
                String pName = player.getName();
                String wName = player.getWorld().getName();
                String[] gNameA = PermissionsEx.getUser(player).getGroupsNames(wName);
                StringBuffer gName = new StringBuffer();
                for(String groups : gNameA) {
                    gName.append(groups + " ");
                }
                Boolean permissions = PermissionsEx.getPermissionManager().has(player, permission);
                LOGGER.info("PermissionsEx permissions, group for '" + pName + "' = " + gName.toString());
                LOGGER.info("Permission for " + permission + " is " + permissions);
            }
            else if (bPermissions != null) {
                String pName = player.getName();
                String wName = player.getWorld().getName();
                String[] gNameA = ApiLayer.getGroups(wName, CalculableType.USER, pName);
                StringBuffer gName = new StringBuffer();
                for(String groups : gNameA) {
                    gName.append(groups + " ");
                }
                Boolean permissions = bPermissions.has(player, permission);
                LOGGER.info("bPermissions, group for '" + pName + "' = " + gName);
                LOGGER.info("bPermission for " + permission + " is " + permissions);
            }
            else if (server.getPluginManager().getPlugin("PermissionsBukkit") != null) {
                LOGGER.info("Bukkit Permissions " + permission + " " + player.hasPermission(permission));
            }
            else if (permissionsEr && (player.isOp() || player.hasPermission(permission))) {
                LOGGER.info("Unknown permissions plugin " + permission + " " + player.hasPermission(permission));
            }
            else {
                LOGGER.info("Unknown permissions plugin " + permission + " " + player.hasPermission(permission));
            }
        }// -- End Debug permissions hooks --
        return player.isOp() || player.hasPermission(permission);
    }

    // permissions plugin debug information
    private void getPermissionsPlugin() {
        if (server.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
            if (!permissionsSet) {
                LOGGER.info(pluginName + ": Vault detected, permissions enabled...");
                permissionsSet = true;
            }
            vaultPerms = rsp.getProvider();
        }
        else if (server.getPluginManager().getPlugin("GroupManager") != null) {
            Plugin p = server.getPluginManager().getPlugin("GroupManager");
            if (!permissionsSet) {
                LOGGER.info(pluginName + ": GroupManager detected, permissions enabled...");
                permissionsSet = true;
            }
            groupManager = (GroupManager) p;
        }
        else if (server.getPluginManager().getPlugin("Permissions") != null) {
            Plugin p = server.getPluginManager().getPlugin("Permissions");
            if (!permissionsSet) {
                LOGGER.info(pluginName + ": Permissions detected, permissions enabled...");
                permissionsSet = true;
            }
            permissionsPlugin = (Permissions) p;
        }
        else if (server.getPluginManager().getPlugin("PermissionsBukkit") != null) {
            if (!permissionsSet) {
                LOGGER.info(pluginName + ": Bukkit permissions detected, permissions enabled...");
                permissionsSet = true;
            }
        }
        else if (server.getPluginManager().getPlugin("PermissionsEx") != null) {
            Plugin p = server.getPluginManager().getPlugin("PermissionsEx");
            if (!permissionsSet) {
                LOGGER.info(pluginName + ": PermissionsEx detected, permissions enabled...");
                permissionsSet = true;
            }
            permissionsExPlugin = (PermissionsEx) p;
        }
        else if (server.getPluginManager().getPlugin("bPermissions") != null) {
            Plugin p = server.getPluginManager().getPlugin("bPermissions");
            if (!permissionsSet) {
                LOGGER.info(pluginName + ": bPermissions detected, permissions enabled...");
                permissionsSet = true;
            }
            bPermissions = (de.bananaco.bpermissions.imp.Permissions) p;
        }
        else {
            if (!permissionsEr) {
                LOGGER.info(pluginName + ": Unknown permissions detected, Using Generic Permissions...");
                permissionsEr = true;
            }
        }
    }
}
