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
        loadFeatures(szMapHubAPIKey);
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
    public void loadFeatures(String szMapHubAPIKey)
    {
        map = GetMap.getMap(szMapHubAPIKey, iMapID);
        locateLocalFeaturesAndCreateAndStoreTheirFeatureMenus();
    }

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
                if (Utils.getGeometricDistance(dPlayerCoordinates, coordinates[j]) < 500)
                {
                    //If the feature is close, create a FeatureMenu for it and store it in the list
                    FeatureMenu featureMenu = new FeatureMenu(iMapID, mapFeatures[i], player);
                    localFeatures.add(featureMenu);
                    break;
                }
            }
        }
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
        Component component = Component.text("Edit Feature", Style.style(TextColor.color(Color.AQUA.asRGB()), TextDecoration.BOLD));
        Inventory inventory = Bukkit.createInventory(null, iRows*9, component);

        //Creates all of the inventory items
        for (i = 0 ; i < iFeatures ; i++)
        {
            //Add feature
            Utils.insertItemIntoInventory(inventory, Material.OAK_SIGN, 1, i,(ChatColor.AQUA +getFeatureMenu(i).feature.getProperties().title));
        }

        //Barrier block
        if (iFeatures == 0)
        {
            Utils.insertItemIntoInventory(inventory, Material.BARRIER, 1, i,(ChatColor.AQUA +"No map features found nearby"));
        }

        //Back - A button which links to the main menu
        Utils.insertItemIntoInventory(inventory, Material.SPRUCE_DOOR, 1, (iRows*9),(ChatColor.AQUA+"" +ChatColor.BOLD +"Return"), ChatColor.WHITE +"Open the navigator main menu");

        return inventory;
    }
}
