package com.jredfox.skincaps;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.evilnotch.lib.main.capability.CapRegDefaultHandler;
import com.evilnotch.lib.main.skin.SkinCache;
import com.evilnotch.lib.main.skin.SkinEntry;
import com.evilnotch.lib.main.skin.SkinEvent;
import com.evilnotch.lib.minecraft.capability.client.ClientCap;
import com.evilnotch.lib.minecraft.capability.client.ClientCapHooks;
import com.evilnotch.lib.minecraft.capability.primitive.CapBoolean;
import com.evilnotch.lib.minecraft.capability.registry.CapabilityRegistry;
import com.evilnotch.lib.minecraft.registry.GeneralRegistry;
import com.evilnotch.lib.util.JavaUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author jredfox
 */
@Mod(modid = SkinCaps.MODID, name = SkinCaps.NAME, version = SkinCaps.VERSION , clientSideOnly = true, dependencies = "required-before:evilnotchlib@[1.2.3.09,)")
public class SkinCaps
{
    public static final String MODID = "skincapabilities";
    public static final String NAME = "Skin Capabilities";
    public static final String VERSION = "0.8.3";
	public static final ResourceLocation ID_EARS = new ResourceLocation("skincaps", "ears");
	public static final ResourceLocation ID_DINNERBONE = new ResourceLocation("skincaps", "dinnerbone");
    
    public static String userSession;
    public static Configuration cfg;
    public static String skin = "";
    public static String model = "";
    public static String cape = "";
	public static String elytra = "";
	public static boolean ears = false;
	public static boolean dinnerbone = false;
    public static boolean cacheCapes;
    
    //cape cache
    public static File cape_cache;
    public static Set<String> capes;

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
        elytra = cfg.get("caps", "elytra", "").getString().trim();
        ears = cfg.get("caps", "mouse_ears", ears).getBoolean();
        dinnerbone = cfg.get("caps", "dinnerbone", dinnerbone).getBoolean();
        cacheCapes = cfg.get("caps", "cache_capes", true).getBoolean();
        cfg.save();
        
        //Load Cape Cache
        if(cacheCapes)
        {
	        cape_cache = new File(dir, "cahe_capes.txt");
	        this.capes = cape_cache.exists() ? new HashSet(JavaUtil.getFileLines(cape_cache, true)) : new HashSet();
        }
        
        //register the command client-side only
        GeneralRegistry.registerClientCommand(new SkinCommand());
        
        //register mouse ears and dinner bone IClientCaps
		ClientCapHooks.register(new ClientCap(ID_EARS, ears));
		ClientCapHooks.register(new ClientCap(ID_DINNERBONE, dinnerbone));
    }
    
    @SubscribeEvent
    public void render(SkinEvent.Mouse event)
    {
    	event.ears = ClientCapHooks.getBoolean(event.player, ID_EARS);
    }
    
    @SubscribeEvent
    public void render(SkinEvent.Dinnerbone event)
    {
    	event.dinnerbone = ClientCapHooks.getBoolean(event.player, ID_DINNERBONE);
    }
    
    public static void syncCaps()
    {
    	ClientCapHooks.set(ID_EARS, ears);
    	ClientCapHooks.set(ID_DINNERBONE, dinnerbone);
    	Minecraft mc = Minecraft.getMinecraft();
    	if(mc.player != null && mc.player.connection != null && ((CapBoolean) CapabilityRegistry.getCapability(mc.player, CapRegDefaultHandler.addedToWorld)).value)
    		ClientCapHooks.uploadUpdate(ID_EARS, ID_DINNERBONE);
    }
    
    @SubscribeEvent
    public void skinUser(SkinEvent.User event)
    {
    	if(!skin.isEmpty() && !JavaUtil.isURL(skin))
    		event.username = skin;
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
    	
    	this.cacheCape(event.skin.cape);//cache initial cape if found
    	
    	//converts the cape from username to URL
    	if(!cape.isEmpty())
    	{
    		if(cape.equalsIgnoreCase("$clear") || cape.equalsIgnoreCase("$noCape"))
    		{
    			event.skin.cape = "";//clears the cape
    		}
    		else if(!JavaUtil.isURL(cape))
    		{
    			SkinEntry capeSkin = SkinCache.INSTANCE.getOrDownload(cape.toLowerCase());
    			//if there is no cape don't make yourself loose your current cape this is what "$clear" or "$nocape" is for
    			if(capeSkin.isEmpty || !JavaUtil.isURL(capeSkin.cape))
    				return;
    			event.skin.cape = capeSkin.cape;
    			cape = capeSkin.cape;
    			saveConfig();//save cape conversion to URL
    		}
    		else
    			event.skin.cape = cape;
    		dirty = true;
    	}
    	
    	if(!elytra.isEmpty())
    	{
    		if(elytra.equalsIgnoreCase("$clear") || elytra.equalsIgnoreCase("$noElytra"))
    		{
    			event.skin.elytra = "";//clears the elytra
    		}
    		if(!JavaUtil.isURL(elytra))
    		{
    			SkinEntry skin = SkinCache.INSTANCE.getOrDownload(elytra.toLowerCase());
    			//use $clear or $noElytra to clear the elytra otherwise don't loose your current settings
    			if(skin.isEmpty || !JavaUtil.isURL(skin.elytra) && !JavaUtil.isURL(skin.cape))
    				return;
    			event.skin.elytra = skin.elytra.isEmpty() ? skin.cape : skin.elytra;//if the user doesn't have a specific elytra skin use the cape texture as the eyltra skin
    			elytra = event.skin.elytra;
    			saveConfig();//save the conversion to URL
    		}
    		else
    			event.skin.elytra = elytra;
    		dirty = true;
    	}
    	
    	if(dirty)
    	{
    		event.skin.isEmpty = false;
    		this.cacheCape(event.skin.cape);//cache cape after
    		this.cacheCape(event.skin.elytra);//cache cape or elytra produced by elytra call
    	}
    }
    
    public void cacheCape(String url)
    {
        if(!cacheCapes || url.isEmpty())
        	return;
        
    	if(JavaUtil.isURL(url))
    	{
    		if(this.capes.add(url))
    		{
    			JavaUtil.saveFileLines(this.capes, this.cape_cache, true);
    		}
    	}
    }

	public static void saveConfig()
	{
        cfg.get("caps", "skin", "").set(skin);
        cfg.get("caps", "cape", "").set(cape);
        cfg.get("caps", "model", "").set(model);
        cfg.get("caps", "elytra", "").set(elytra);
        cfg.get("caps", "mouse_ears", "").set(ears);
        cfg.get("caps", "dinnerbone", "").set(dinnerbone);
        cfg.get("caps", "cache_capes", true);
        cfg.save();
	}
}
