package me.bteuk.progressmapper.guis;

import me.bteuk.progressmapper.ProgressMapper;
import me.bteuk.progressmapper.Utils;
import me.bteuk.progressmapperbackend.maphubapi.actions.Append;
import me.bteuk.progressmapperbackend.maphubapi.actions.GetMap;
import me.bteuk.progressmapperbackend.maphubapi.actions.Update;
import me.bteuk.progressmapperbackend.maphubapi.maphubobjects.*;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class FeatureMenu
{
    //Feature Menu is a menu used to edit the properties and geometry of each feature
    //It should have several icons which when clicked on allow you to edit a specific property and one for editing the geometry

    //Inventory details
    final int iRows = 3;
    Player player;

    //Menu components
    private Book titleBook;
    private Book descriptionBook;
    private Book mediaURLBook;

    //Feature details
    int iMapID;
    Feature feature;

    //Object details
    boolean bNew;
    //If true, it represents a new feature
    //If false, it represents a current feature

    //Used for editing existing features
    public FeatureMenu(int iMapID, Feature feature, Player player)
    {
        this.iMapID = iMapID;
        this.feature = feature;
        this.player = player;
        this.bNew = false;

        titleBook = Book.book(Component.text("Title"), Component.text("Progress Map"), (Component.text(feature.getProperties().title)));
        descriptionBook = Book.book(Component.text("Description"), Component.text("Progress Map"), (Component.text(feature.getProperties().description)));
        mediaURLBook = Book.book(Component.text("Media URL"), Component.text("Progress Map"), (Component.text(feature.getProperties().media_url)));
    }

    //Used for adding new features
    public FeatureMenu(int iMapID, Player player)
    {
        this.iMapID = iMapID;
        this.player = player;
        this.bNew = true;
    }

    public Book getTitleBook()
    {
        return titleBook;
    }
    public Book getDescriptionBook()
    {
        return descriptionBook;
    }

    public Book getMedialURLBook()
    {
        return mediaURLBook;
    }

    public Inventory getGUI()
    {
        if (bNew)
            return getGuiNewFeature();
        else
            return getEditGUI();
    }

    private Inventory getEditGUI()
    {
        //Do we want the GetMap to call the map data every time we refresh? What happens if someone else has edited it in the meantime?
        //Would we want those edits to show up or would we just say that whoever submits last if they didn't also make the changes that first person made then it reverts back

        Component component = Component.text("Edit Feature", Style.style(TextColor.color(Color.AQUA.asRGB()), TextDecoration.BOLD));
        Inventory inventory = Bukkit.createInventory(null, iRows, component);

        //Title
        Utils.insertItemIntoInventory(inventory, Material.COBWEB, 1, 1,(ChatColor.AQUA +"Edit Title"), ChatColor.DARK_AQUA +"Current: "+feature.getProperties().title);

        //Description
        Utils.insertItemIntoInventory(inventory, Material.WRITABLE_BOOK, 1, 3,(ChatColor.AQUA +"Edit Description"), ChatColor.DARK_AQUA +"Click to view the current description and edit");
        //Will open a book

        if (feature.getGeometry().getType().equals("Polygon"))
            //fill
            Utils.insertItemIntoInventory(inventory, Material.GREEN_DYE, 1, 5,(ChatColor.AQUA +"Edit Colour"), ChatColor.DARK_AQUA +"Current: "+feature.getProperties().fill);
        else if (feature.getGeometry().getType().equals("LineString"))
            //stroke
            Utils.insertItemIntoInventory(inventory, Material.GREEN_DYE, 1, 5,(ChatColor.AQUA +"Edit Colour"), ChatColor.DARK_AQUA +"Current: "+feature.getProperties().stroke);
        //For these two we could even have an RGB thing. An RGB Indicator might be a bit hard though. We could have a "Show colour in chat message" button possibly

        //Media URL
        Utils.insertItemIntoInventory(inventory, Material.PAINTING, 1, 7,(ChatColor.AQUA +"Edit media"), ChatColor.DARK_AQUA +"Current: "+feature.getProperties().media_url);

        //Coordinates - A button which links to the coordinate menu
        Utils.insertItemIntoInventory(inventory, Material.MAP, 1, 9,(ChatColor.AQUA +"Edit shape and coordinates"), ChatColor.DARK_AQUA +"Click to make a new selection");

        //Send update - A button which links to the coordinate menu
        Utils.insertItemIntoInventory(inventory, Material.EMERALD, 1, 23,(ChatColor.AQUA +"Update Map"));

        //Back - A button which links to the coordinate menu
        Utils.insertItemIntoInventory(inventory, Material.SPRUCE_DOOR, 1, 26,(ChatColor.AQUA +"Back"));
        return inventory;
    }

    private Inventory getGuiNewFeature()
    {
        //Called when creating a new feature, and also every time an edit is made to a new feature (the inventory display needs to refresh/update)


        Component component = Component.text("Create Feature", Style.style(TextColor.color(Color.AQUA.asRGB()), TextDecoration.BOLD));
        Inventory inventory = Bukkit.createInventory(null, iRows, component);

        Properties properties = new Properties("#555555", "Title", "Description");
        Geometry geometry = new Geometry(GeometryType.Polygon); //All new items on the BTe UK map from the MC server will be polygons
        geometry.coordinates = new double[0][2];

        this.feature = new Feature(geometry, properties);
        return inventory;
    }

    public void fieldEdit(Field fieldType, String szNewContent)
    {
        switch (fieldType)
        {
            case Title:
                feature.getProperties().title = szNewContent;
                break;
            case Description:
                feature.getProperties().description = szNewContent;
                break;
            case Media_url:
                feature.getProperties().media_url = szNewContent;
                break;
        }
    }

    //Remember - all of these functions are performed async. Once one happens, nothing happens util the player does something at which point the plugin
    //will find this again from the list of these open menus and call the relevant class
    //It is all stored within the plugin list

    public void sendUpdate(String szMapHubAPIKey)
    {
        //Fetches the map from the API again. We want to be editing the latest version and then sending that as the Update.
        GetMap getMap = GetMap.getMap(szMapHubAPIKey, iMapID);
        Geojson mapData = getMap.getGeojson();
        Feature[] features = mapData.getFeatures();
        int iNumFeatures = features.length;
        int iIndex = -1;

        //Find the index of the feature again - this may seem tedious and wasteful but if the map has changed since the menu was opened,
        //going by the index of the feature could easily result in loss and overriding of data if the order has been switched around

        for (int i = 0 ; i < iNumFeatures ; i++)
        {
            if (features[i].getID() == feature.getID())
            {
                iIndex = i;
                break;
            }
        }

        //If the feature can't now be found
        if (iIndex == -1)
            //Append a new feature
            sendAppend(szMapHubAPIKey);
        else
        {
            //Replace the old feature with the new one
            mapData.getFeatures()[iIndex] = feature;
            //Send the new geojson as an update
            Update.sendMapUpdate(szMapHubAPIKey, iMapID, mapData);
        }
    }

    public void sendAppend(String szMapHubAPIKey)
    {
        //Append a new feature
        Geojson geojson = new Geojson(new Feature[]{feature});
        Append.sendMapAppend(szMapHubAPIKey, iMapID, geojson);
    }
}