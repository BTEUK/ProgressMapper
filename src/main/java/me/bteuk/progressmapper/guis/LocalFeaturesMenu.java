package me.bteuk.progressmapper.guis;

import me.bteuk.progressmapper.Utils;
import me.bteuk.progressmapperbackend.maphubapi.actions.GetMap;
import me.bteuk.progressmapperbackend.maphubapi.maphubobjects.Feature;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

public class LocalFeaturesMenu
{
    //Holds the player
    Player player;

    //Holds the MapID
    int iMapID;

    //Contains the map details
    GetMap map;

    //Holds the player's current coordinates
    double[] dPlayerCoordinates;

    //Contains a list of all local features
    ArrayList<FeatureMenu> localFeatures;

    public LocalFeaturesMenu(int iMapID, String szMapHubAPIKey, Player player)
    {
        this.iMapID = iMapID;
        this.player = player;

        extractCoordinatesFromPlayer();
        if (dPlayerCoordinates != null)
        {
            //Downloads the map
            //Adds a blank feature to the list of features which can be edited and then uploaded to the progress map
            loadFeatures(szMapHubAPIKey);
        }
    }

    //Public because we may want to update the GUI whilst still keeping this same LocalFeaturesMenu object
    private void extractCoordinatesFromPlayer()
    {
        dPlayerCoordinates = Utils.convertFromBukkitLocationToGeometricCoordinates(player.getLocation());
    }

    public double[] getPlayerCoordinates()
    {
        return dPlayerCoordinates;
    }

    //Public because we may want to update the GUI whilst still keeping this same LocalFeaturesMenu object

    /**
     * Downloads the map, locates the features local to the player and creates FeatureMenus for each feature
     * @param szMapHubAPIKey
     */
    public void loadFeatures(String szMapHubAPIKey)
    {
        map = GetMap.getMap(szMapHubAPIKey, iMapID, false);
        locateLocalFeaturesAndCreateAndStoreTheirFeatureMenus();

        //Adds a blank feature to the list of features which can be edited and then uploaded to the progress map
        createBlankFeature();
    }

    /**
     * Locates the features local to the player and creates FeatureMenus for each feature
     */
    private void locateLocalFeaturesAndCreateAndStoreTheirFeatureMenus()
    {
        Feature[] mapFeatures;
        int iTotalFeatures;
        int i, j, iCoordinates;
        double[][] coordinates;

        //Resets the local features list
        localFeatures = new ArrayList<>();

        //Extracts the map features from the geojson information
        mapFeatures = map.getGeojson().getFeatures();
        iTotalFeatures = mapFeatures.length;

        //Iterate through all features
        for (i = 0 ; i < iTotalFeatures ; i++)
        {
            //Get the feature's coordinates
            coordinates = mapFeatures[i].getGeometry().coordinates;
            iCoordinates = coordinates.length;

            //Iterate through the feature's coordinates
            for (j = 0 ; j < iCoordinates ; j++)
            {
                //Checks to see whether any point on the feature is close
                //Feature coordinates are in Long, lat form, so are player coordinates
                if (Utils.getGeometricDistance(dPlayerCoordinates, coordinates[j]) < 500)
                {
                    //If the feature is close, create a FeatureMenu for it and store it in the list
                    FeatureMenu featureMenu = new FeatureMenu(iMapID, mapFeatures[i], player, map);
                    localFeatures.add(featureMenu);
                    break;
                }
            }
        }
    }

    /**
     * Adds a blank feature to the list of features which can be edited and then uploaded to the progress map
     */
    private void createBlankFeature()
    {
        FeatureMenu featureMenu = new FeatureMenu(iMapID, player, map);
        localFeatures.add(featureMenu);
    }

    public int getNumFeatures()
    {
        return localFeatures.size();
    }

    public FeatureMenu getFeatureMenu(int iFeature)
    {
        return localFeatures.get(iFeature);
    }

    //Returns an inventory out of all of the local features
    public Inventory getGUI()
    {
        int i, iFeatures;
        iFeatures = getNumFeatures();

        //Calculate rows
        int iRows = iFeatures/9;
        if (iFeatures%9 != 0)
            iRows++;

        iRows = iRows + 2; //One empty line, then the back button

        //Creates the inventory
        Component component = Component.text("Nearby Map Features", Style.style(TextColor.color(Color.AQUA.asRGB()), TextDecoration.BOLD));
        Inventory inventory = Bukkit.createInventory(null, iRows*9, component);

        //Creates all of the inventory items
        for (i = 0 ; i < iFeatures - 1 ; i++)
        {
            //Add feature
            String szTitle = getFeatureMenu(i).feature.getProperties().title;
            if (szTitle == null)
                Utils.insertItemIntoInventory(inventory, Material.OAK_SIGN, 1, i+1,(ChatColor.AQUA +"Unnamed feature"));
            else
                Utils.insertItemIntoInventory(inventory, Material.OAK_SIGN, 1, i+1,(ChatColor.AQUA +getFeatureMenu(i).feature.getProperties().title));
        }

        //Barrier block
        if (iFeatures == 0)
        {
            Utils.insertItemIntoInventory(inventory, Material.BARRIER, 1, 1,(ChatColor.AQUA +"No map features found nearby"));
        }
        else
        {
            Utils.insertItemIntoInventory(inventory, Material.JUNGLE_SIGN, 1, iFeatures,(ChatColor.AQUA +"Create New Feature"));
        }

        //Back - A button which links to the building menu
        Utils.insertItemIntoInventory(inventory, Material.SPRUCE_DOOR, 1, (iRows*9),(ChatColor.AQUA+"" +ChatColor.BOLD +"Return"), ChatColor.WHITE +"Open the building menu.");

        return inventory;
    }
}
