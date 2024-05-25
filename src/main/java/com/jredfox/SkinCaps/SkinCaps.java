package com.jredfox.SkinCaps;

import java.io.File;

import com.evilnotch.lib.main.skin.SkinCache;
import com.evilnotch.lib.main.skin.SkinEntry;
import com.evilnotch.lib.main.skin.SkinEvent;
import com.evilnotch.lib.minecraft.registry.GeneralRegistry;
import com.evilnotch.lib.util.JavaUtil;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = SkinCaps.MODID, name = SkinCaps.NAME, version = SkinCaps.VERSION , clientSideOnly = true, dependencies = "required-before:evilnotchlib@[1.2.3.09,)")
public class SkinCaps
{
    public static final String MODID = "skincapabilities";
    public static final String NAME = "Skin Capabilities";
    public static final String VERSION = "0.8.0";
    
    public static String userSession;
    public static Configuration cfg;
    public static String skin = "";
    public static String model = "";
    public static String cape = "";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        //load the config
        File dir = new File(event.getSuggestedConfigurationFile().getParentFile(), "SkinCapabilities");
        if(!dir.exists())
        	dir.mkdir();
        
        userSession = Minecraft.getMinecraft().getSession().getUsername().toLowerCase();
        cfg = new Configuration(new File(dir, userSession + ".cfg"));
        cfg.load();
        skin = cfg.get("caps", "skin", "").getString().trim();
        cape = cfg.get("caps", "cape", "").getString().trim();
        model = cfg.get("caps", "model", "").getString().trim().toLowerCase();
        cfg.save();
        
        //register the command
        GeneralRegistry.registerClientCommand(new SkinCommand());
    }
    
    @SubscribeEvent
    public void skinUser(SkinEvent.User event)
    {
    	if(!skin.isEmpty() && !JavaUtil.isURL(skin))
    	{
    		event.username = skin.toLowerCase();
    	}
    }
    
    @SubscribeEvent
    public void skinCaps(SkinEvent.Capability event)
    {
    	boolean dirty = false;
    	
    	if(!skin.isEmpty() && JavaUtil.isURL(skin))
    	{
    		event.skin.skin = skin;
    		dirty = true;
    	}
    	
    	if(!model.isEmpty())
    	{
    		event.skin.model = model.toLowerCase();
    		dirty = true;
    	}
    	
    	//converts the cape from username to URL
    	if(!cape.isEmpty())
    	{
    		if(cape.equals("$clear") || cape.equals("$nocape"))
    		{
    			event.skin.cape = "";//clears the cape
    		}
    		else if(!JavaUtil.isURL(cape))
    		{
    			String url = SkinCache.INSTANCE.getOrDownload(event.skin, cape.toLowerCase()).cape;
    			if(!JavaUtil.isURL(url) && !SkinCache.INSTANCE.isMojangOnline())
    				return;//if the cape url fetch failed do not continue
    			cape = url;
    			saveConfig();//save cape conversion to URL
    		}
			event.skin.cape = cape;
    		dirty = true;
    	}
    	
    	if(dirty)
    	{
    		if(event.skin.isEmpty)
    			event.skin.skin = "http://textures.minecraft.net/texture/$steve"; //will always return 400-404 but will allow for model and cape to still work
    		event.skin.isEmpty = false;
    	}
    }

	public static void saveConfig()
	{
        cfg.get("caps", "skin", "").set(skin);
        cfg.get("caps", "cape", "").set(cape);
        cfg.get("caps", "model", "").set(model);
        cfg.save();
	}
}
