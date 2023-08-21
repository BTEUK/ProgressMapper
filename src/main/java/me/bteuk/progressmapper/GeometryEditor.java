package me.bteuk.progressmapper;

import me.bteuk.progressmapperbackend.maphubapi.maphubobjects.Feature;
import me.bteuk.progressmapperbackend.maphubapi.maphubobjects.GeometryType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

public class GeometryEditor
{
    private Feature feature;
    private Player player;

    private static final int iRows = 3;

    //A list of minecraft coordinates making up the geometry, tracked by this class
    private ArrayList<BlockCoordinates> blockCoordinatesList;

    //A list of locations of perimeter blocks, used for
    private ArrayList<Location> perimeterBlocksList;

    public GeometryEditor(Feature feature, Player player)
    {
        this.feature = feature;
        this.player = player;

        this.blockCoordinatesList = new ArrayList<>();
        convertFeatureGeometryIntoBlockCoordinates();
        updatePerimeterBlocksList();
    }

    public ArrayList<BlockCoordinates> getBlockCoordinatesList()
    {
        return this.blockCoordinatesList;
    }

    public Inventory getGUI()
    {
        //3 is cancel
        //7 is confirm
        Component component = Component.text("Confirm area", Style.style(TextColor.color(Color.AQUA.asRGB()), TextDecoration.BOLD));
        Inventory inventory = Bukkit.createInventory(null, iRows * 9, component);

        Utils.insertItemIntoInventory(inventory, Material.BARRIER, 1, 3,(ChatColor.AQUA +"Cancel edit"), ChatColor.DARK_AQUA +"Restore to original area");
        Utils.insertItemIntoInventory(inventory, Material.EMERALD, 1, 7,(ChatColor.AQUA +"Confirm edit"), ChatColor.DARK_AQUA +"Save the area. Click the emerald on the Feature Menu to upload these changes.");

        return inventory;
    }

    /**
     * Creates a list of minecraft block coordinates from the geometry of the feature and saves this in the list
     */
    public void convertFeatureGeometryIntoBlockCoordinates()
    {
        ArrayList<BlockCoordinates> blockCoordinatesList = new ArrayList<>();
        double[][] dCoordinatesOfGeometry = feature.getGeometry().coordinates;
        int iNumCoordinates = dCoordinatesOfGeometry.length;
        int i;

        double dLatitude, dLongitude;
        for (i = 0 ; i < iNumCoordinates ; i++)
        {
            //Feature coordinates are in Long, lat form
            dLatitude = dCoordinatesOfGeometry[i][0];
            dLongitude = dCoordinatesOfGeometry[i][1];

            BlockCoordinates blockCoordinates = new BlockCoordinates(dLatitude, dLongitude);
            blockCoordinatesList.add(blockCoordinates);
        }
    }

    /**
     * Creates a list of geometric coordinates from the list of block coordinates that have been stored
     */
    private double[][] convertFeatureGeometryIntoGeometric()
    {
        int i;

        //Creates the geometric coordinates array
        int iNumCoordinates = this.blockCoordinatesList.size();

        double[][] dCoordinatesOfGeometry;

        if (feature.getGeometry().getType().equals(GeometryType.Polygon))
            dCoordinatesOfGeometry = new double[iNumCoordinates+1][2];
        else
            dCoordinatesOfGeometry = new double[iNumCoordinates][2];

        //Converts each of the block coordinates into geometric coordinates and stores the geometric coordinates in the array
        double[] longLat;
        for (i = 0 ; i < iNumCoordinates ; i++)
        {
            longLat = blockCoordinatesList.get(i).convertToGeometricCoordinates();
            dCoordinatesOfGeometry[i] = longLat;
        }

        //Sets the start as the end
        if (feature.getGeometry().getType().equals(GeometryType.Polygon))
            dCoordinatesOfGeometry[iNumCoordinates] = dCoordinatesOfGeometry[0];

        return dCoordinatesOfGeometry;
    }

    /**
     * Saves the geometry being edited to the owning feature's object
     */
    public void confirmGeometry()
    {
        feature.getGeometry().coordinates = convertFeatureGeometryIntoGeometric();
    }

    public void leftClick(long x, long z)
    {
        //Resets the list
        ArrayList<BlockCoordinates> blockCoordinatesList = new ArrayList<>();

        //Adds the first point
        blockCoordinatesList.add(new BlockCoordinates(x, z));

        //Updates the perimeter blocks list, which also updates the players view
        updatePerimeterBlocksList();
    }

    public void rightClick(long x, long z)
    {
        //Adds a point
        blockCoordinatesList.add(new BlockCoordinates(x, z));

        //Updates the perimeter blocks list, which also updates the players view
        updatePerimeterBlocksList();
    }

    private void updatePerimeterBlocksList()
    {
        int i, j;
        int iNumPoints;
        int iNumLocations;
        long[] xzCoordinates;
        int[] iPoint1;
        int[] iPoint2;
        ArrayList<Location> locations;

        //Resets the perimeterBlocksList
        perimeterBlocksList = new ArrayList<>();

        iNumPoints = blockCoordinatesList.size();

        //Goes through all points on the geometry perimeter
        for (i = 0 ; i < iNumPoints - 1 ; i++)
        {
            //Calculates the line between two points on the perimeter
            xzCoordinates = blockCoordinatesList.get(i).xzCoordinates;
            iPoint1 = new int[]{(int) xzCoordinates[0], (int) xzCoordinates[1]};

            xzCoordinates = blockCoordinatesList.get(i+1).xzCoordinates;
            iPoint2 = new int[]{(int) xzCoordinates[0], (int) xzCoordinates[1]};

            locations = Utils.LineCalculator2D(iPoint1, iPoint2, this.player.getWorld());
            iNumLocations = locations.size();

            //Adds all of the points on this line to the perimeter blocks list
            for (j = 0 ; j < iNumLocations ; j++)
            {
                perimeterBlocksList.add(locations.get(j));
            }
        }

        if (iNumPoints > 1)
        {
            //Calculates the line between the first and last points on the perimeter
            xzCoordinates = blockCoordinatesList.get(iNumPoints - 1).xzCoordinates;
            iPoint1 = new int[]{(int) xzCoordinates[0], (int) xzCoordinates[1]};

            xzCoordinates = blockCoordinatesList.get(0).xzCoordinates;
            iPoint2 = new int[]{(int) xzCoordinates[0], (int) xzCoordinates[1]};

            locations = Utils.LineCalculator2D(iPoint1, iPoint2, this.player.getWorld());
            iNumLocations = locations.size();

            //Adds all of the points on this line to the perimeter blocks list
            for (j = 0 ; j < iNumLocations ; j++)
            {
                perimeterBlocksList.add(locations.get(j));
            }
        }
        updateView();
    }

    public void updateView()
    {
        int i;
        int iNumLocations = this.perimeterBlocksList.size();

        //Displays the particles along this line
        for (i = 0 ; i < iNumLocations ; i++)
        {
            this.player.spawnParticle(Particle.REDSTONE, this.perimeterBlocksList.get(i), 10, new Particle.DustOptions(Color.GREEN, 3));
        }
    }
}
