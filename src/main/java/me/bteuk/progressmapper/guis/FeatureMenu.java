package me.bteuk.progressmapper.guis;

import me.bteuk.progressmapper.BlockCoordinates;
import me.bteuk.progressmapper.GeometryEditor;
import me.bteuk.progressmapper.Utils;
import me.bteuk.progressmapperbackend.maphubapi.actions.Append;
import me.bteuk.progressmapperbackend.maphubapi.actions.GetMap;
import me.bteuk.progressmapperbackend.maphubapi.actions.Update;
import me.bteuk.progressmapperbackend.maphubapi.maphubobjects.*;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;

public class FeatureMenu
{
    //Feature Menu is a menu used to edit the properties and geometry of each feature
    //It should have several icons which when clicked on allow you to edit a specific property and one for editing the geometry

    //Inventory details
    final int iRows = 3;
    Player player;

    //Menu components
    private ItemStack titleBook;
    private ItemStack descriptionBook;
    private ItemStack mediaURLBook;

    private BookMeta titleBookMeta;
    private BookMeta descriptionBookMeta;
    private BookMeta mediaURLBookMeta;

    //Feature details
    int iMapID;
    Feature feature;

    //Object details
    boolean bNew;
    //If true, it represents a new feature
    //If false, it represents a current feature

    //Colour GUI
    private ColourPicker colourPicker;

    //Geometry handler
    private GeometryEditor geometryEditor;

    //Used for editing existing features
    public FeatureMenu(int iMapID, Feature feature, Player player)
    {
        this.iMapID = iMapID;
        this.feature = feature;
        this.player = player;
        this.bNew = false;

        this.colourPicker = new ColourPicker(feature);
        this.geometryEditor = new GeometryEditor(feature, player);

        titleBook = new ItemStack(Material.WRITABLE_BOOK);
        titleBookMeta = (BookMeta) titleBook.getItemMeta();
        descriptionBook = new ItemStack(Material.WRITABLE_BOOK);
        descriptionBookMeta = (BookMeta) titleBook.getItemMeta();
        mediaURLBook = new ItemStack(Material.WRITABLE_BOOK);
        mediaURLBookMeta = (BookMeta) titleBook.getItemMeta();


        //Setup title book
        titleBookMeta.setTitle("Edit Title");
        if (feature.getProperties().title == null)
        {
            titleBookMeta.addPages(Component.text(""));
            System.out.println("Title was empty");
        }
        else
        {
            titleBookMeta.addPages(Component.text(feature.getProperties().title));
            System.out.println("Title set as: " +titleBookMeta.page(1));
        }

        //Setup description book
        descriptionBookMeta.setTitle("Edit Description");
        if (feature.getProperties().description == null)
        {
            descriptionBookMeta.addPages(Component.text(""));
            System.out.println("Description was empty");
        }
        else
        {
            descriptionBookMeta.addPages(Component.text(feature.getProperties().description));
            System.out.println("Description set as: " +descriptionBookMeta.page(1));
        }

        //Setup media_url book
        mediaURLBookMeta.setTitle("Edit media url");
        if (feature.getProperties().media_url == null)
        {
            mediaURLBookMeta.addPages(Component.text(""));
            System.out.println("Media url was empty");
        }
        else
        {
            mediaURLBookMeta.addPages(Component.text(feature.getProperties().media_url));
            System.out.println("Media url set as: " +mediaURLBookMeta.page(1));
        }

        //Adds the new metas to the books
        titleBook.setItemMeta(titleBookMeta);
        descriptionBook.setItemMeta(descriptionBookMeta);
        mediaURLBook.setItemMeta(mediaURLBookMeta);
    }

    //Used for adding new features
    public FeatureMenu(int iMapID, Player player)
    {
        this.iMapID = iMapID;
        this.player = player;
        this.bNew = true;
    }

    public boolean isNew()
    {
        return bNew;
    }

    public ItemStack getTitleBook()
    {
        return titleBook;
    }
    public ItemStack getDescriptionBook()
    {
        return descriptionBook;
    }

    public ItemStack getMedialURLBook()
    {
        return mediaURLBook;
    }

    public ColourPicker getColourPicker()
    {
        return colourPicker;
    }

    public GeometryEditor getGeometryEditor()
    {
        return geometryEditor;
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
        Inventory inventory = Bukkit.createInventory(null, iRows*9, component);

        //Title
        if (feature.getProperties().title == null)
            Utils.insertItemIntoInventory(inventory, Material.COBWEB, 1, 1,(ChatColor.AQUA +"Edit Title"), ChatColor.DARK_AQUA +"Current: None");
        else
            Utils.insertItemIntoInventory(inventory, Material.COBWEB, 1, 1,(ChatColor.AQUA +"Edit Title"), ChatColor.DARK_AQUA +"Current: "+feature.getProperties().title);

        //Description
        Utils.insertItemIntoInventory(inventory, Material.WRITABLE_BOOK, 1, 3,(ChatColor.AQUA +"Edit Description"), ChatColor.DARK_AQUA +"Click to view the current description and edit");

        //Colours
        Component iconTitle = Component.text("Edit Colour", Style.style(TextColor.color(Color.AQUA.asRGB())));
        
        // Sets up the lore
        Component iconLoreLine1 = Component.text("");
        if (feature.getGeometry().getType().equals(GeometryType.Polygon))
            iconLoreLine1 = Component.text("Current: "+feature.getProperties().fill, Style.style(TextColor.fromHexString(feature.getProperties().fill)));
        else if (feature.getGeometry().getType().equals(GeometryType.LineString))
            iconLoreLine1 = Component.text("Current: "+feature.getProperties().stroke, Style.style(TextColor.fromHexString(feature.getProperties().stroke)));
        
        ArrayList<Component> iconLore = new ArrayList<>();
        iconLore.add(iconLoreLine1);

        //Inserts the item into the inventory
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.ORANGE_WOOL, 1, 5, iconTitle, iconLore);

        //For these two we could even have an RGB thing. An RGB Indicator might be a bit hard though. We could have a "Show colour in chat message" button possibly, or have the Text in the icons coloured correctly
        
        //Media URL
        if (feature.getProperties().media_url == null)
            Utils.insertItemIntoInventory(inventory, Material.PAINTING, 1, 7,(ChatColor.AQUA +"Edit media"), ChatColor.DARK_AQUA +"Current: None");
        else
            Utils.insertItemIntoInventory(inventory, Material.PAINTING, 1, 7,(ChatColor.AQUA +"Edit media"), ChatColor.DARK_AQUA +"Current: "+feature.getProperties().media_url);

        //Coordinates - A button which links to the coordinate menu
        Utils.insertItemIntoInventory(inventory, Material.MAP, 1, 9,(ChatColor.AQUA +"Edit shape and coordinates"), ChatColor.DARK_AQUA +"Click to edit the area or view the current area");

        //Send update - A button which links to the coordinate menu
        Utils.insertItemIntoInventory(inventory, Material.EMERALD, 1, 23,(ChatColor.AQUA +"Update Map"));

        //Back - A button which links to the coordinate menu
        Utils.insertItemIntoInventory(inventory, Material.SPRUCE_DOOR, 1, 27,(ChatColor.AQUA +"" +ChatColor.BOLD +"Return"), ChatColor.WHITE +"Return to the list of nearby map features");
        return inventory;
    }

    private Inventory getGuiNewFeature()
    {
        //Called when creating a new feature, and also every time an edit is made to a new feature (the inventory display needs to refresh/update)


        Component component = Component.text("Create Feature", Style.style(TextColor.color(Color.AQUA.asRGB()), TextDecoration.BOLD));
        Inventory inventory = Bukkit.createInventory(null, iRows, component);

        Properties properties = new Properties("#555555", "Title", "Description");
        properties.media_url = "";
        Geometry geometry = new Geometry(GeometryType.Polygon); //All new items on the BTE UK map added from the MC server will be polygons
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
                titleBookMeta.page(1, Component.text(szNewContent));

                //Adds the new meta to the books
                titleBook.setItemMeta(titleBookMeta);
                break;
            case Description:
                feature.getProperties().description = szNewContent;
                descriptionBookMeta.page(1, Component.text(szNewContent));

                //Adds the new meta to the books
                descriptionBook.setItemMeta(descriptionBookMeta);
                break;
            case Media_url:
                feature.getProperties().media_url = szNewContent;
                mediaURLBookMeta.page(1, Component.text(szNewContent));

                //Adds the new meta to the books
                mediaURLBook.setItemMeta(mediaURLBookMeta);
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
