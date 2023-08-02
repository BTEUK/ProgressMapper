package me.bteuk.progressmapper;

import net.buildtheearth.terraminusminus.util.geo.LatLng;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static String chat (String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static ItemStack insertItemIntoInventory(Inventory inv, Material material, int amount, int invSlot, String displayName, String... loreString) {

        ItemStack item;

        List<String> lore = new ArrayList<String>();

        //Creates a new item for the specified material
        item = new ItemStack(material);

        //Creates the meta for the item
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Utils.chat(displayName));
        for (String s : loreString) {
            lore.add(Utils.chat(s));
        }
        meta.setLore(lore);

        //Adds the meta to the item
        item.setItemMeta(meta);

        //Sets the item in the inventory
        inv.setItem(invSlot - 1,  item);

        return item;
    }

    public static float getGeometricDistance(double[] dCoordinates1, double[] dCoordinates2)
    {
        //Tpll accuracy checker
        double dLatitude1 = dCoordinates1[0];
        double dLatitude2 = dCoordinates2[0];

        double dLongitude1 = dCoordinates1[1];
        double dLongitude2 = dCoordinates2[1];

        int iRadius = 6371000; // metres
        double φ1 = dLatitude1 * Math.PI/180; // φ, λ in radians
        double φ2 = dLatitude2 * Math.PI/180;
        double Δφ = (dLatitude2-dLatitude1) * Math.PI/180;
        double Δλ = (dLongitude2-dLongitude1) * Math.PI/180;

        double a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
                Math.cos(φ1) * Math.cos(φ2) *
                        Math.sin(Δλ/2) * Math.sin(Δλ/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        float fDistance = (float) (iRadius * c); // in metres

        return fDistance;
    }

    public static ItemStack createPlayerSkull(Inventory inv, Player p, int amount, int invSlot, String displayName, String... loreString) {

        ItemStack item;

        List<String> lore = new ArrayList<String>();

        item = new ItemStack(Material.PLAYER_HEAD);

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setDisplayName(Utils.chat(displayName));
        for (String s : loreString) {
            lore.add(Utils.chat(s));
        }
        meta.setLore(lore);
        meta.setOwningPlayer(p);
        item.setItemMeta(meta);

        inv.setItem(invSlot - 1,  item);

        return item;

    }

    public static boolean isPlayerInGroup(Player player, String group) {
        return player.hasPermission("group." + group);
    }

    public static void spawnFireWork(Player p) {

        Firework f = p.getWorld().spawn(p.getLocation(), Firework.class);
        FireworkMeta fm = f.getFireworkMeta();
        fm.addEffect(FireworkEffect.builder().flicker(true).trail(true).with(Type.BALL_LARGE).withColor(Color.RED).withColor(Color.BLUE).withColor(Color.WHITE).build());
        fm.setPower(1);
        f.setFireworkMeta(fm);


    }

    public static int getHighestYAt(World w, int x, int z) {

        for (int i = 255; i >= 0; i--) {
            if (w.getBlockAt(x, i, z).getType() != Material.AIR) {
                return i+1;
            }
        }
        return 0;
    }
}