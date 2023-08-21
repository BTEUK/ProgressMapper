package me.bteuk.progressmapper;

import me.bteuk.progressmapperbackend.maphubapi.maphubobjects.Feature;
import me.bteuk.progressmapperbackend.maphubapi.maphubobjects.GeometryType;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class GeometryEditor
{
    private Feature feature;
    private Player player;

    //A list of minecraft coordinates making up the geometry, tracked by this class
    private ArrayList<BlockCoordinates> blockCoordinatesList;

    public GeometryEditor(Feature feature, Player player)
    {
        this.feature = feature;
        this.player = player;
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
    }

    public void rightClick(long x, long z)
    {
        //Adds a point
        blockCoordinatesList.add(new BlockCoordinates(x, z));
    }
}
