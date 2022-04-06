package com.dynious.refinedrelocation.config;

import com.dynious.refinedrelocation.lib.Reference;
import cpw.mods.fml.client.config.GuiConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class ConfigGUI extends GuiConfig
{
    public ConfigGUI(GuiScreen parent)
    {
        super(parent,
                new ConfigElement(ConfigHandler.configFile.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
                Reference.MOD_ID, false, false, GuiConfig.getAbridgedConfigPath(ConfigHandler.configFile.toString()));
    }
}
