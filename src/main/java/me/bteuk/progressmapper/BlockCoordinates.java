package me.bteuk.progressmapper;

public class BlockCoordinates
{
    /**
     * Stores minecraft coordinates with index 0 as x and index 1 as z
     */
    long[] xzCoordinates;

    public double[] convertToGeometricCoordinates()
    {
        double[] geometricCoordinates = Utils.convertFromMcCoordinatesToGeometricCoordinates(xzCoordinates[0], xzCoordinates[1]);
        return geometricCoordinates;
    }

    /**
     * Creates a new block coordinates object, converting the parsed geometric coordinates (latitude then longitude) into minecraft coordinates and storing these
     */
    public BlockCoordinates(double dLatitude, double dLongitude)
    {
        //Converts the geometric coordinates to minecraft coordinates
        double[] xzDoubleCoordinates = Utils.convertToMCCoordinates(dLatitude, dLongitude);

        //Makes the minecraft coordinates integers
        xzCoordinates = new long[]{(long) xzDoubleCoordinates[0], (long) xzDoubleCoordinates[1]};
    }

    /**
     * Creates a new block coordinates object out of minecraft coordinates parsed as x and then z
     */
    public BlockCoordinates(long x, long z)
    {
        //Makes the minecraft coordinates integers
        xzCoordinates = new long[]{x, z};
    }
}
