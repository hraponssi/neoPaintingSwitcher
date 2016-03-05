package plugin.arcwolf.neopaintingswitch;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.Art;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.BlockIterator;

public class npPlayerEvent implements Listener {

    private neoPaintingSwitch plugin;

    public npPlayerEvent(neoPaintingSwitch plugin) {
        this.plugin = plugin;
    }

    // Updated WG support
    // Code pull from BangL (https://github.com/BangL)
    // https://github.com/arcwolf/neoPaintingSwitch/pull/1
    //
    // Updated for worldguard 6
    //
    private boolean canModifyPainting(Player player, Entity e) {
        // First check for op ...
        if (!player.isOp()
                // ... if not, check if WorldGuardPlugin existent ...
                && plugin.worldguard
                // ... if yes, then check if player can build in any region anyways.
                && !plugin.hasPermission(player, "worldguard.region.bypass." + player.getWorld().getName().toLowerCase())) {
            return plugin.wgp.canBuild(player, e.getLocation());
        }
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (event.isCancelled())
            return;
        if (plugin.hasPermission(event.getPlayer(), "neopaintingswitch.use") || plugin.free4All) {
            Player player = event.getPlayer();
            npSettings settings = npSettings.getSettings(player);
            if (settings.previousPainting != null && event.getEntity() instanceof Painting) {
                Painting painting = (Painting) event.getEntity();
                if (!painting.setArt(settings.previousPainting.getArt())) {
                    Art[] art = Art.values();
                    int count = new Random().nextInt(Art.values().length - 1);
                    int tempCount = count;
                    count--;
                    if (count == -1) count = 0;
                    while (!painting.setArt(art[count])) {
                        if (count == 0)
                            count = art.length - 1;
                        else
                            count--;
                        if (count == tempCount) break;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.isCancelled())
            return;
        Entity entity = event.getRightClicked();
        if (event.getHand() == EquipmentSlot.HAND && entity instanceof Painting && (plugin.hasPermission(event.getPlayer(), "neopaintingswitch.use") || plugin.free4All)) {
            Player player = event.getPlayer();
            if (canModifyPainting(player, entity)) {
                Set<Entry<String, npSettings>> keys = npSettings.playerSettings.entrySet();
                for(Entry<String, npSettings> set : keys) {
                    String playerName = set.getKey();
                    if (npSettings.playerSettings.get(playerName).painting != null && npSettings.playerSettings.get(playerName).painting.getEntityId() == entity.getEntityId() && !playerName.equals(player.getName())) {
                        player.sendMessage(playerName + ChatColor.RED + " is already editing this painting.");
                        return;
                    }
                }
                npSettings settings = npSettings.getSettings(player);
                //settings.block = player.getTargetBlock(null, 20); //TODO
                settings.block = getTargetBlock(player, null, 20);
                settings.painting = (Painting) entity;
                settings.location = player.getLocation();
                if (settings.clicked) {
                    player.sendMessage(ChatColor.RED + "Painting locked");
                    npSettings.clear(player);
                }
                else {
                    player.sendMessage(ChatColor.GREEN + "Scroll to change painting");
                    settings.clicked = true;
                }
            }
            else {
                player.sendMessage(ChatColor.RED + "This Painting is locked by worldguard.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getPlayer();
        npSettings settings = npSettings.getSettings(player);
        try {
            if (settings.block != null && settings.location != null && settings.clicked && hasPlayerMovedSignificantly(event)) {
                player.sendMessage(ChatColor.RED + "Painting locked");
                npSettings.clear(player);
            }
        } catch (Exception e) {
            // Do Nothing
        }
    }

    private boolean hasPlayerMovedSignificantly(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        npSettings settings = npSettings.getSettings(player);
        int oldPlayerPosX = Math.abs(settings.location.getBlockX() + 100);
        int oldPlayerPosY = Math.abs(settings.location.getBlockY() + 100);
        int oldPlayerPosZ = Math.abs(settings.location.getBlockZ() + 100);
        int newPlayerPosX = Math.abs(event.getTo().getBlockX() + 100);
        int newPlayerPosY = Math.abs(event.getTo().getBlockY() + 100);
        int newPlayerPosZ = Math.abs(event.getTo().getBlockZ() + 100);
        if (oldPlayerPosX < newPlayerPosX) {
            int temp = oldPlayerPosX;
            oldPlayerPosX = newPlayerPosX;
            newPlayerPosX = temp;
        }
        if (oldPlayerPosY < newPlayerPosY) {
            int temp = oldPlayerPosY;
            oldPlayerPosY = newPlayerPosY;
            newPlayerPosY = temp;
        }
        if (oldPlayerPosZ < newPlayerPosZ) {
            int temp = oldPlayerPosZ;
            oldPlayerPosZ = newPlayerPosZ;
            newPlayerPosZ = temp;
        }
        int oldPlayerYaw = (int) Math.abs(settings.location.getYaw());
        int newPlayerYaw = (int) Math.abs(player.getLocation().getYaw());
        int oldPlayerPitch = (int) settings.location.getPitch();
        int newPlayerPitch = (int) player.getLocation().getPitch();
        if (hasYawChangedSignificantly(oldPlayerYaw, newPlayerYaw) || hasPitchChangedSignificantly(oldPlayerPitch, newPlayerPitch)) {
            //if (!settings.block.equals(player.getTargetBlock(null, 15))) { return true; } //TODO
            if (!settings.block.equals(getTargetBlock(player, null, 15))) { return true; }
        }
        if (((newPlayerYaw <= 315 && newPlayerYaw >= 225) || (newPlayerYaw <= 135 && newPlayerYaw >= 45)) &&
                ((oldPlayerPosX % newPlayerPosX > 7) || (oldPlayerPosY % newPlayerPosY > 2) || (oldPlayerPosZ % newPlayerPosZ > 2))) { // -X or +X direction
            //if (!settings.block.equals(player.getTargetBlock(null, 15))) { return true; } //TODO
            if (!settings.block.equals(getTargetBlock(player, null, 15))) { return true; }
        }
        if (((newPlayerYaw < 45 || newPlayerYaw > 315) || (newPlayerYaw < 225 && newPlayerYaw > 135)) &&
                ((oldPlayerPosX % newPlayerPosX > 2) || (oldPlayerPosY % newPlayerPosY > 2) || (oldPlayerPosZ % newPlayerPosZ > 7))) { // -Z or +Z direction
            //if (!settings.block.equals(player.getTargetBlock(null, 15))) { return true; } //TODO
            if (!settings.block.equals(getTargetBlock(player, null, 15))) { return true; }
        }
        return false;
    }

    /**
     * Gets the block that the living entity has targeted.
     * 
     * @param LivingEntity
     *            this is the entity to get target block
     * @param transparent
     *            HashSet containing all transparent block Materials (set to
     *            null for only air)
     * @param maxDistance
     *            this is the maximum distance to scan (may be limited by server
     *            by at least 100 blocks, no less)
     * @return block that the living entity has targeted
     */
    private Block getTargetBlock(LivingEntity entity, HashSet<Material> transparent, int maxDistance) {
        Block target = entity.getEyeLocation().getBlock();
        Location eyeLoc = entity.getEyeLocation();
        if(transparent == null){
            transparent = new HashSet<Material>();
            transparent.add(Material.AIR);
        }
        try {
            BlockIterator lineOfSight = new BlockIterator(entity.getWorld(), eyeLoc.toVector(), entity.getLocation().getDirection(), 0, maxDistance);
            while (lineOfSight.hasNext()) {
                Block toTest = lineOfSight.next();
                if (!transparent.contains(toTest.getType()))
                    return target;
                else
                    target = toTest;
            }
        } catch (Exception e) {
            //Do nothing
        }
        return target;
    }

    private boolean hasPitchChangedSignificantly(int oldPlayerPitch, int newPlayerPitch) {
        if (oldPlayerPitch < newPlayerPitch) {
            int temp = oldPlayerPitch;
            oldPlayerPitch = newPlayerPitch;
            newPlayerPitch = temp;
        }
        return (oldPlayerPitch - newPlayerPitch) > 30;
    }

    private boolean hasYawChangedSignificantly(int oldYaw, int newYaw) {
        oldYaw = Math.abs(oldYaw) + 360;
        newYaw = Math.abs(newYaw) + 360;
        if (oldYaw < newYaw) {
            int temp = oldYaw;
            oldYaw = newYaw;
            newYaw = temp;
        }
        return (oldYaw % newYaw) > 30;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        npSettings settings = npSettings.getSettings(player);
        int previousSlot = event.getPreviousSlot();
        int newSlot = event.getNewSlot();
        boolean reverse = (previousSlot - newSlot) > 0;
        if (((previousSlot == 0) && (newSlot == 8)) || ((previousSlot == 8) && (newSlot == 0))) {
            reverse = !reverse;
        }
        if (settings.clicked && settings.painting != null && settings.block != null && !reverse) {
            Painting painting = settings.painting;
            Art[] art = Art.values();
            int currentID = painting.getArt().ordinal();
            if (currentID == art.length - 1) {
                int count = 0;
                while (!painting.setArt(art[count])) {
                    if (count == art.length - 1) break;
                    count++;
                }
            }
            else {
                int count = painting.getArt().ordinal();
                int tempCount = count;
                count++;
                while (!painting.setArt(art[count])) {
                    if (count == art.length - 1)
                        count = 0;
                    else
                        count++;
                    if (count == tempCount) break;
                }
            }
            settings.previousPainting = painting;
        }
        else if (settings.clicked && settings.painting != null && settings.block != null && reverse) {
            Painting painting = settings.painting;
            Art[] art = Art.values();
            int currentID = painting.getArt().ordinal();
            if (currentID == 0) {
                int count = art.length - 1;
                while (!painting.setArt(art[count])) {
                    count--;
                    if (count == 0) break;
                }
            }
            else {
                int count = painting.getArt().ordinal();
                int tempCount = count;
                count--;
                while (!painting.setArt(art[count])) {
                    if (count == 0)
                        count = art.length - 1;
                    else
                        count--;
                    if (count == tempCount) break;
                }
            }
            settings.previousPainting = painting;
        }
    }
}
