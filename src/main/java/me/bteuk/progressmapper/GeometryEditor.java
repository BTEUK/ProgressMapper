package me.bteuk.progressmapper;

import me.bteuk.progressmapper.guis.ColourPicker;
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
    private ColourPicker colourPicker;

    private static final int iRows = 3;

    //A list of minecraft coordinates making up the geometry, tracked by this class
    private ArrayList<BlockCoordinates> blockCoordinatesList;

    //A list of locations of perimeter blocks, used for
    private ArrayList<Location> perimeterBlocksList;

    public GeometryEditor(Feature feature, Player player, ColourPicker colourPicker)
    {
        this.feature = feature;
        this.player = player;
        this.colourPicker = colourPicker;

        this.blockCoordinatesList = new ArrayList<>();
        convertFeatureGeometryIntoBlockCoordinates();
        updatePerimeterBlocksList();
    }

    /**
     * @return The list of coordinates defining the corners of the geometry
     */
    public ArrayList<BlockCoordinates> getBlockCoordinatesList()
    {
        return this.blockCoordinatesList;
    }

    /**
     * Creates a GUI for the GeometryEditor, containing a cancel button as a barrier and a confirm button as an emerald, with suitable texts
     * @return An inventory with two buttons
     */
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
     * Creates a list of minecraft block coordinates from the geometry of the feature and saves this in the Geometry editor list
     */
    public void convertFeatureGeometryIntoBlockCoordinates()
    {
        double[][] dCoordinatesOfGeometry = feature.getGeometry().coordinates;
        int iNumCoordinates = dCoordinatesOfGeometry.length;
        int i;

//        System.out.println("Converting feature's geometry (as stored in the feature object) into block coordinates, There are " +iNumCoordinates +" coordinates in the feature");

        double dLatitude, dLongitude;

        //This if statement ensures that block coordinates always represent the geometry without the closing side connecting
        // the final and first point. This is because when clicking confirm geometry after making no changes it will always add
        // in the closing side. We do not want it to ever change the coordinates on the map when a player clicks confirm
        // after making no changes in game
        if (feature.getGeometry().getType().equals(GeometryType.Polygon))
            iNumCoordinates = iNumCoordinates - 1;

        //Goes through each point on the geometry of a feature and creates a block coordinate based on the latitude and longitude
        for (i = 0 ; i < iNumCoordinates ; i++)
        {
            //Feature coordinates are in Long, lat form
            dLongitude = dCoordinatesOfGeometry[i][0];
            dLatitude = dCoordinatesOfGeometry[i][1];

            BlockCoordinates blockCoordinates = new BlockCoordinates(dLatitude, dLongitude);

            //Only adds points to the list if the coordinates could be produced
            if (blockCoordinates.xzCoordinates != null)
                this.blockCoordinatesList.add(blockCoordinates);
        }
    }

    /**
     * Creates a list of geometric coordinates from the list of block coordinates that have been stored
     */
    private double[][] convertFeatureGeometryFromBlockCoordinatesIntoGeographical()
    {
        int i;

        //Creates the geometric coordinates array
        int iNumCoordinates = this.blockCoordinatesList.size();
//        System.out.println("There are " +iNumCoordinates +" block coordinates");
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
            dCoordinatesOfGeometry[i][0] = longLat[0];
            dCoordinatesOfGeometry[i][1] = longLat[1];
        }

        //Sets the start as the end if a polygon
        if (feature.getGeometry().getType().equals(GeometryType.Polygon))
        {
            dCoordinatesOfGeometry[iNumCoordinates][0] = dCoordinatesOfGeometry[0][0];
            dCoordinatesOfGeometry[iNumCoordinates][1] = dCoordinatesOfGeometry[0][1];

        }
        return dCoordinatesOfGeometry;
    }

    /**
     * Saves the geometry being edited to the owning feature's object
     */
    public void confirmGeometry()
    {
        feature.getGeometry().coordinates = convertFeatureGeometryFromBlockCoordinatesIntoGeographical();
    }

    /**
     * Adds a pair of minecraft coordinates to the end of the list of block coordinates making up the geometry of the feature being edited
     * @param x The minecraft x coordinate of the block that was clicked
     * @param z The minecraft z coordinate of the block that was clicked
     */
    public void leftClick(long x, long z)
    {
        //Resets the list
        this.blockCoordinatesList = new ArrayList<>();

        //Adds the first point
        blockCoordinatesList.add(new BlockCoordinates(x, z));

        //Updates the perimeter blocks list, which also updates the players view
        updatePerimeterBlocksList();
    }

    /**
     * Removes all points from the list of block coordinates making up the geometry of the feature being edited and then starts a new list with the pair of coordinates parsed
     * @param x The minecraft x coordinate of the block that was clicked
     * @param z The minecraft z coordinate of the block that was clicked
     */
    public void rightClick(long x, long z)
    {
        //Adds a point
        blockCoordinatesList.add(new BlockCoordinates(x, z));

        //Updates the perimeter blocks list, which also updates the players view
        updatePerimeterBlocksList();
    }

    /**
     * Updates the list of blocks making up the lines outlining the entire perimeter based on corners specified by the block coordinates list
     */
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

    /**
     * Sends the player particles showing the outline of the feature's geometry
     */
    public void updateView()
    {
        int i, j;
        int iNumLocations = this.perimeterBlocksList.size();

        Location spawnParticleLocation = new Location(player.getWorld(), 0, 0, 0);

        //Displays the particles along the outline of the NEW area
        if (iNumLocations > 0)
            this.player.spawnParticle(Particle.REDSTONE, this.perimeterBlocksList.get(0), 10, new Particle.DustOptions(colourPicker.getBukkitColorObjectFromColour(), 6));
        for (i = 1 ; i < iNumLocations - 1; i++)
        {
            spawnParticleLocation.add(this.perimeterBlocksList.get(i), 0.5, 0, 0.5);
            this.player.spawnParticle(Particle.REDSTONE, spawnParticleLocation, 10, new Particle.DustOptions(colourPicker.getBukkitColorObjectFromColour(), 4));
        }
        if (iNumLocations > 1)
            this.player.spawnParticle(Particle.REDSTONE, this.perimeterBlocksList.get(iNumLocations-1), 10, new Particle.DustOptions(colourPicker.getBukkitColorObjectFromColour(), 6));

        ArrayList<Location> locations;

        double[][] savedCoordinates = this.feature.getGeometry().coordinates;
        iNumLocations = savedCoordinates.length;
        int[] iPoint1;
        int[] iPoint2;
        int iNumBlocksOnLine;

        //Displays the particles along the outline of the OLD area
        for (i = 0 ; i < iNumLocations - 1 ; i++)
        {
            //Each POINT on the OLD outline
            double[] mcCoordsXZ = Utils.convertToMCCoordinates(savedCoordinates[i][1], savedCoordinates[i][0]);
            Location location = new Location(player.getWorld(), mcCoordsXZ[0], player.getWorld().getHighestBlockYAt((int) mcCoordsXZ[0], (int) mcCoordsXZ[1]) + 1, mcCoordsXZ[1]);
            this.player.spawnParticle(Particle.REDSTONE, location, 7, new Particle.DustOptions(Color.PURPLE, 3));

            //Lines connecting points on the old outline
            //Calculates the line between two points on the perimeter
            iPoint1 = new int[]{(int) savedCoordinates[i][0], (int) savedCoordinates[i][1]};
            iPoint2 = new int[]{(int) savedCoordinates[i+1][0], (int) savedCoordinates[i+1][1]};

            locations = Utils.LineCalculator2D(iPoint1, iPoint2, this.player.getWorld());
            iNumBlocksOnLine = locations.size();

            //Displays all of the points on this line apart from the end points
            for (j = 1 ; j < iNumBlocksOnLine - 1 ; j++)
            {
                spawnParticleLocation.add(locations.get(j), 0.5, 0, 0.5);
                this.player.spawnParticle(Particle.REDSTONE, spawnParticleLocation, 7, new Particle.DustOptions(Color.PURPLE, 1));
            }
        }

        //Final connector line
        if (iNumLocations > 1)
        {
            //Final POINT on the OLD outline
            double[] mcCoordsXZ = Utils.convertToMCCoordinates(savedCoordinates[iNumLocations - 2][1], savedCoordinates[iNumLocations - 2][0]);
            Location location = new Location(player.getWorld(), mcCoordsXZ[0], player.getWorld().getHighestBlockYAt((int) mcCoordsXZ[0], (int) mcCoordsXZ[1]) + 1, mcCoordsXZ[1]);
            this.player.spawnParticle(Particle.REDSTONE, location, 7, new Particle.DustOptions(Color.PURPLE, 3));

            //Calculates the line between the first and last points on the perimeter
            iPoint1 = new int[]{(int) savedCoordinates[iNumLocations - 1][0], (int) savedCoordinates[iNumLocations - 1][1]};
            iPoint2 = new int[]{(int) savedCoordinates[0][0], (int) savedCoordinates[0][1]};

            locations = Utils.LineCalculator2D(iPoint1, iPoint2, this.player.getWorld());
            iNumBlocksOnLine = locations.size();

            //Displays all of the points on this line apart from the end points
            for (j = 1 ; j < iNumBlocksOnLine - 1 ; j++)
            {
                spawnParticleLocation.add(locations.get(j), 0.5, 0, 0.5);
                this.player.spawnParticle(Particle.REDSTONE, spawnParticleLocation, 7, new Particle.DustOptions(Color.PURPLE, 1));
            }
        }
    }
}
