package me.bteuk.progressmapper.guis;

import me.bteuk.progressmapper.Utils;
import me.bteuk.progressmapperbackend.maphubapi.maphubobjects.Feature;
import me.bteuk.progressmapperbackend.maphubapi.maphubobjects.GeometryType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;


//Used to pick the colour
public class ColourPicker
{
    public static final TextColor Dark_Red = TextColor.color(Integer.parseInt("cc1b15", 16));
    public static final TextColor Red = TextColor.color(Integer.parseInt("e32c31", 16));
    public static final TextColor Dark_Orange = TextColor.color(Integer.parseInt("fb6033", 16));
    public static final TextColor Orange = TextColor.color(Integer.parseInt("f18729", 16));
    public static final TextColor Light_Orange = TextColor.color(Integer.parseInt("f1ae29", 16));
    public static final TextColor Yellow = TextColor.color(Integer.parseInt("ffcc00", 16));
    public static final TextColor Bright_Yellow = TextColor.color(Integer.parseInt("fff700", 16));
    public static final TextColor Sick_Green = TextColor.color(Integer.parseInt("a0c514", 16));
    public static final TextColor Complete_Green = TextColor.color(Integer.parseInt("3cc954", 16));

    //I think I want 5 rows. Middle-middle will have an icon and the text of the icon will show the true colour
    //Perhaps at the top row, centre aligned will be a list of predefined colours
    //Then I want RGB editors. I want a -16, -1, +1, +16 for each of the colours
    //Perhaps each color could be on a different row, so, 5 lines?
    //Line 1: predefined colours
    //Line 2 Red
    //Line 3: Green
    //Line 4: Blue
    //Line 5: The colour show icon - will also server as a confirm button

    //Inventory details
    private final int iRows = 5;

    //Feature details
    private Feature feature;

    //Stores separately for ease of access
    private GeometryType geometryType;

    //Tracks the designated colour
    private TextColor textColour;

    public ColourPicker(Feature feature)
    {
        this.feature = feature;

        this.geometryType = feature.getGeometry().getType();

        String szHex;
        switch (geometryType)
        {
            case Polygon:
            case Point:
                if (feature.getProperties().fill == null)
                    szHex = "4D76CD";
                else
                    szHex = feature.getProperties().fill.replace("#", "");
                break;
            case LineString:
                if (feature.getProperties().stroke == null)
                    szHex = "4D76CD";
                else
                    szHex = feature.getProperties().stroke.replace("#", "");
                break;
            default:
                szHex = "4D76CD";
                break;
        }

        int iRGBValue = Integer.parseInt(szHex, 16);
        this.textColour = TextColor.color(iRGBValue);
    }

    public Inventory getGUI()
    {
        //Creates the inventory
        Component component = Component.text("Edit colour", Style.style(textColour, TextDecoration.BOLD));
        Inventory inventory = Bukkit.createInventory(null, iRows*9, component);

        //----------------------------------------------------
        //----------- Top Row - Predefined colours -----------
        //----------------------------------------------------

        Component default1 = Component.text("Dark Red", Style.style(Dark_Red, TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.RED_DYE, 1, 1, default1, new ArrayList<>());

        Component default2 = Component.text("Red", Style.style(Red, TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.RED_DYE, 1, 2, default2, new ArrayList<>());

        Component default3 = Component.text("Dark Orange", Style.style(Dark_Orange, TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.ORANGE_DYE, 1, 3, default3, new ArrayList<>());

        Component default4 = Component.text("Orange", Style.style(Orange, TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.ORANGE_DYE, 1, 4, default4, new ArrayList<>());

        Component default5 = Component.text("Light Orange", Style.style(Light_Orange, TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.ORANGE_DYE, 1, 5, default5, new ArrayList<>());

        Component default6 = Component.text("Yellow", Style.style(Yellow, TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.YELLOW_DYE, 1, 6, default6, new ArrayList<>());

        Component default7 = Component.text("Bright Yellow", Style.style(Bright_Yellow, TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.YELLOW_DYE, 1, 7, default7, new ArrayList<>());

        Component default8 = Component.text("Sick Green", Style.style(Sick_Green, TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.LIME_DYE, 1, 8, default8, new ArrayList<>());

        Component default9 = Component.text("Complete Green", Style.style(Complete_Green, TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.GREEN_DYE, 1, 9, default9, new ArrayList<>());

        //-----------------------------------------------------
        //---------------- Line 2 - Red Editor ----------------
        //-----------------------------------------------------
        //Slots: 10-18, 14 is the middle. 12,13, then space, then 15,16

        Component take16Red = Component.text("-16 red", Style.style(TextColor.color(Color.RED.asRGB()), TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.RED_WOOL, 16, 12, take16Red, new ArrayList<>());

        Component take1Red = Component.text("-1 red", Style.style(TextColor.color(Color.RED.asRGB()), TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.RED_DYE, 1, 13, take1Red, new ArrayList<>());

        Component add1Red = Component.text("+1 red", Style.style(TextColor.color(Color.RED.asRGB()), TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.RED_DYE, 1, 15, add1Red, new ArrayList<>());

        Component add16Red = Component.text("+16 red", Style.style(TextColor.color(Color.RED.asRGB()), TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.RED_WOOL, 16, 16, add16Red, new ArrayList<>());

        String szRedHex = HexFormat.of().withUpperCase().toHexDigits(textColour.red());
        //The red display

        //-----------------------------------------------------
        //--------------- Line 3 - Green Editor ---------------
        //-----------------------------------------------------

        Component take16Green = Component.text("-16 green", Style.style(TextColor.color(Color.GREEN.asRGB()), TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.GREEN_WOOL, 16, 21, take16Green, new ArrayList<>());

        Component take1Green = Component.text("-1 green", Style.style(TextColor.color(Color.GREEN.asRGB()), TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.GREEN_DYE, 1, 22, take1Green, new ArrayList<>());

        Component add1Green = Component.text("+1 green", Style.style(TextColor.color(Color.GREEN.asRGB()), TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.GREEN_DYE, 1, 24, add1Green, new ArrayList<>());

        Component add16Green = Component.text("+16 green", Style.style(TextColor.color(Color.GREEN.asRGB()), TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.GREEN_WOOL, 16, 25, add16Green, new ArrayList<>());

        String szGreenHex = HexFormat.of().withUpperCase().toHexDigits(textColour.green());
        //The green display

        //----------------------------------------------------
        //--------------- Line 4 - Blue Editor ---------------
        //----------------------------------------------------

        Component take16Blue = Component.text("-16 blue", Style.style(TextColor.color(Color.BLUE.asRGB()), TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.BLUE_WOOL, 16, 30, take16Blue, new ArrayList<>());

        Component take1Blue = Component.text("-1 blue", Style.style(TextColor.color(Color.BLUE.asRGB()), TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.BLUE_DYE, 1, 31, take1Blue, new ArrayList<>());

        Component add1Blue = Component.text("+1 blue", Style.style(TextColor.color(Color.BLUE.asRGB()), TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.BLUE_DYE, 1, 33, add1Blue, new ArrayList<>());

        Component add16Blue = Component.text("+16 blue", Style.style(TextColor.color(Color.BLUE.asRGB()), TextDecoration.BOLD));
        Utils.insertItemIntoInventoryViaComponents(inventory, Material.BLUE_WOOL, 16, 34, add16Blue, new ArrayList<>());

        String szBlueHex = HexFormat.of().withUpperCase().toHexDigits(textColour.blue());
        //The blue display

        //-----------------------------------------------------
        //-------------- Line 5 - Colour display --------------
        //-----------------------------------------------------
        //Displays the colour
        Component colourDisplay = Component.text("This is the colour you've chosen", Style.style(textColour, TextDecoration.BOLD));
        Component loreLine1 = Component.text(textColour.asHexString(), Style.style(textColour, TextDecoration.BOLD));
        List<Component> lore = new ArrayList();
        lore.add(loreLine1);

        Utils.insertItemIntoInventoryViaComponents(inventory, Material.WHITE_STAINED_GLASS_PANE, 1, 41, colourDisplay, lore);

        return inventory;
    }

    public void updateColour(TextColor textColour)
    {
        this.textColour = textColour;
    }

    public void raiseRed1()
    {
        if (!((textColour.red() + 1) > 255))
            this.textColour = TextColor.color(textColour.red() + 1, textColour.green(), textColour.blue());
        else
            this.textColour = TextColor.color(textColour.red(), textColour.green(), textColour.blue());
    }

    public void raiseRed16()
    {
        if (!((textColour.red() + 16) > 255))
            this.textColour = TextColor.color(textColour.red() + 16, textColour.green(), textColour.blue());
        else
            this.textColour = TextColor.color(255, textColour.green(), textColour.blue());
    }

    public void raiseGreen1()
    {
        if (!((textColour.green() + 1) > 255))
            this.textColour = TextColor.color(textColour.red(), textColour.green() + 1, textColour.blue());
        else
            this.textColour = TextColor.color(textColour.red(), textColour.green(), textColour.blue());
    }

    public void raiseGreen16()
    {
        if (!((textColour.green() + 16) > 255))
            this.textColour = TextColor.color(textColour.red(), textColour.green() + 16, textColour.blue());
        else
            this.textColour = TextColor.color(textColour.red(), 255, textColour.blue());
    }

    public void raiseBlue1()
    {
        if (!((textColour.blue() + 1) > 255))
            this.textColour = TextColor.color(textColour.red(), textColour.green(), textColour.blue() + 1);
        else
            this.textColour = TextColor.color(textColour.red(), textColour.green(), textColour.blue());
    }

    public void raiseBlue16()
    {
        if (!((textColour.blue() + 16) > 255))
            this.textColour = TextColor.color(textColour.red(), textColour.green(), textColour.blue() + 16);
        else
            this.textColour = TextColor.color(textColour.red(), textColour.green(), 255);
    }

    public void lowerRed1()
    {
        if (!((textColour.red() - 1) < 0))
            this.textColour = TextColor.color(textColour.red() - 1, textColour.green(), textColour.blue());
        else
            this.textColour = TextColor.color(textColour.red(), textColour.green(), textColour.blue());
    }

    public void lowerRed16()
    {
        if (!((textColour.red() - 16) < 0))
            this.textColour = TextColor.color(textColour.red() - 16, textColour.green(), textColour.blue());
        else
            this.textColour = TextColor.color(0, textColour.green(), textColour.blue());
    }

    public void lowerGreen1()
    {
        if (!((textColour.green() - 1) < 0))
            this.textColour = TextColor.color(textColour.red(), textColour.green() - 1, textColour.blue());
        else
            this.textColour = TextColor.color(textColour.red(), textColour.green(), textColour.blue());
    }

    public void lowerGreen16()
    {
        if (!((textColour.green() - 16) < 0))
            this.textColour = TextColor.color(textColour.red(), textColour.green() - 16, textColour.blue());
        else
            this.textColour = TextColor.color(textColour.red(), 0, textColour.blue());
    }

    public void lowerBlue1()
    {
        if (!((textColour.blue() - 1) < 0))
            this.textColour = TextColor.color(textColour.red(), textColour.green(), textColour.blue() - 1);
        else
            this.textColour = TextColor.color(textColour.red(), textColour.green(), textColour.blue());
    }

    public void lowerBlue16()
    {
        if (!((textColour.blue() - 16) < 0))
            this.textColour = TextColor.color(textColour.red(), textColour.green(), textColour.blue() - 16);
        else
            this.textColour = TextColor.color(textColour.red(), textColour.green(), 0);
    }

    /**
     * Saves the colour of the feature being edited to the feature's object
     */
    public void confirmColour()
    {
        switch (geometryType)
        {
            case Polygon:
            case Point:
                feature.getProperties().fill = textColour.asHexString();
                break;
            case LineString:
                feature.getProperties().stroke = textColour.asHexString();
                break;
        }
    }
}
