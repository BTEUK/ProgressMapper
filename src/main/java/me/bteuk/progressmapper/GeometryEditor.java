package me.bteuk.progressmapper;

import me.bteuk.progressmapperbackend.maphubapi.maphubobjects.Feature;
import me.bteuk.progressmapperbackend.maphubapi.maphubobjects.GeometryType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class GeometryEditor
{
    private Feature feature;
    private Player player;

    //A list of minecraft coordinates making up the geometry, tracked by this class
    private ArrayList<BlockCoordinates> blockCoordinatesList;

    //A list of locations of perimeter blocks, used for
    private ArrayList<Location> perimeterBlocksList;

    public GeometryEditor(Feature feature, Player player)
    {
        this.feature = feature;
        this.player = player;

        convertFeatureGeometryIntoBlockCoordinates();
        updatePerimeterBlocksList();
    }

    public ArrayList<BlockCoordinates> getBlockCoordinatesList()
    {
        return this.blockCoordinatesList;
    }

    /**
     * Creates a list of minecraft block coordinates from the geometry of the feature and saves this in the list
     */
    private void convertFeatureGeometryIntoBlockCoordinates()
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
        int iNumPoints = blockCoordinatesList.size();
        int iNumLocations;
        long[] xzCoordinates;
        int[] iPoint1;
        int[] iPoint2;
        ArrayList<Location> locations;

        //Resets the perimeterBlocksList
        perimeterBlocksList = new ArrayList<>();

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
                perimeterBlocksList.add(locations.get(i));
            }
        }
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
