package me.bteuk.progressmapper;

import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.projection.GeographicProjection;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraminusminus.util.geo.LatLng;
import net.kyori.adventure.text.Component;
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
        item = new ItemStack(material, amount);

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

    //
    public static ItemStack insertItemIntoInventoryViaComponents(Inventory inv, Material material, int amount, int invSlot, Component displayName, List<Component> loreLines) {

        ItemStack item;

        //Creates a new item for the specified material
        item = new ItemStack(material, amount);

        //Creates the meta for the item
        ItemMeta meta = item.getItemMeta();

        //Sets the display name
        meta.displayName(displayName);

        //Sets the lore
        meta.lore(loreLines);

        //Adds the meta to the item
        item.setItemMeta(meta);

        //Sets the item in the inventory
        inv.setItem(invSlot - 1,  item);

        return item;
    }

    //Long lat form
    public static float getGeometricDistance(double[] dCoordinates1, double[] dCoordinates2)
    {
        //Tpll accuracy checker
        double dLatitude1 = dCoordinates1[1];
        double dLatitude2 = dCoordinates2[1];

        double dLongitude1 = dCoordinates1[0];
        double dLongitude2 = dCoordinates2[0];

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

    public static double[] convertFromBukkitLocationToGeometricCoordinates(Location location)
    {
        double[] longLat = null;

        final GeographicProjection projection = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS).projection();

        try
        {
            longLat = projection.toGeo(location.getX(), location.getZ());
        }
        catch (OutOfProjectionBoundsException e)
        {
            //Player has selected an area outside of the projection
        }

        return longLat;
    }

    public static double[] convertFromMcCoordinatesToGeometricCoordinates(long x, long z)
    {
        double[] longLat = null;

        final GeographicProjection projection = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS).projection();

        try
        {
            longLat = projection.toGeo(x, z);
        }
        catch (OutOfProjectionBoundsException e)
        {
            //Player has selected an area outside of the projection
        }

        return longLat;
    }

    public static double[] convertToMCCoordinates(double dLatitude, double dLongitude)
    {
        double[] xz = null;

        final GeographicProjection projection = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS).projection();

        try
        {
            xz = projection.fromGeo(dLongitude, dLatitude);
        }
        catch (OutOfProjectionBoundsException e)
        {
            //Player has selected an area outside of the projection
        }
        return xz;
    }

    public static ArrayList<Location> LineCalculator2D(int[] iSelectedBlockCoordinates1, int[] iSelectedBlockCoordinates2, World bukkitWorld)
    {
        //In this method, see if you can utilise the world edit api
        ArrayList<Location> line = null;
        line = new ArrayList<>();
        int dx = Math.abs(iSelectedBlockCoordinates1[0]-iSelectedBlockCoordinates2[0]);
        int dz = Math.abs(iSelectedBlockCoordinates1[1]-iSelectedBlockCoordinates2[1]);

        double dMax = Math.max(dx, dz);

        int x1 = iSelectedBlockCoordinates1[0];
        int x2 = iSelectedBlockCoordinates2[0];
        int z1 = iSelectedBlockCoordinates1[1];
        int z2 = iSelectedBlockCoordinates2[1];

        if (dx + dz == 0)
        {
            line.add(new Location(bukkitWorld, x1, bukkitWorld.getHighestBlockYAt(x1, z1), z1));
        }
        else
        {
            int tipx;
            int tipz;
            int domstep;
            if (dMax == dx)
            {
                for(domstep = 0; domstep <= dx; domstep++)
                {
                    tipx = x1 + domstep * (x2 - x1 > 0 ? 1 : -1);
                    tipz = (int)Math.round((double)z1 + (double)domstep * (double)dz / (double)dx * (double)(z2 - z1 > 0 ? 1 : -1));
                    line.add(new Location(bukkitWorld, tipx, bukkitWorld.getHighestBlockYAt(tipx, tipz) + 1, tipz));
                }
            }
            else
            {
                for(domstep = 0; domstep <= dz; ++domstep)
                {
                    tipz = z1 + domstep * (z2 - z1 > 0 ? 1 : -1);
                    tipx = (int)Math.round((double)x1 + (double)domstep * (double)dx / (double)dz * (double)(x2 - x1 > 0 ? 1 : -1));
                    line.add(new Location(bukkitWorld, tipx, bukkitWorld.getHighestBlockYAt(tipx, tipz) + 1, tipz));
                }
            }
        }
        return line;
    }
}