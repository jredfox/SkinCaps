package com.jredfox.skincaps;

import com.evilnotch.lib.main.skin.SkinCache;
import com.evilnotch.lib.main.skin.SkinEntry;
import com.evilnotch.lib.minecraft.util.EnumChatFormatting;
import com.evilnotch.lib.minecraft.util.PlayerUtil;
import com.evilnotch.lib.util.JavaUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;

/**
 * Doesn't run when the world isn't remote
 * @author jredfox
 */
public class SkinCommand extends CommandBase {
	
    /**
     * Return the required permission level for this command.
     */
	@Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

	@Override
	public String getName() 
	{
		return SkinCaps.cmdprefix + "skin" + SkinCaps.cmdsuffix;
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		String name = this.getName();
		return  "/" + name + " [user, url]\n"
			  + "/" + name + " set [skin, cape, model, elytra] [user, url]\n"
			  + "/" + name + " get [entry, encode, skin, cape, model, elytra] [user, url]\n"
			  + "/" + name + " refresh [user, url]\n"
			  + "/" + name + " refresh\n";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException 
	{
		if(!(sender instanceof EntityPlayerSP) || args.length < 1)
			throw new WrongUsageException(this.getUsage(null), new Object[0]);
		
		EntityPlayer p = (EntityPlayer) sender;
		
		//Handles /skin refresh user
		if(args[0].equals("refresh"))
		{
			if(args.length >= 2)
			{
				SkinCache.INSTANCE.refresh(args[1].toLowerCase(), false);//re-download the specified user
				SkinCache.INSTANCE.refreshClientSkin();//re-sync the client's skin in case they wanted to use the refreshed skin
				p.sendMessage(new TextComponentString("Skin is Refreshing: " + args[1]));
			}
			else
			{
				SkinCache.INSTANCE.refreshClientSkin();//re-sync the client's skin in case they wanted to use the refreshed skin
				p.sendMessage(new TextComponentString("Skin is Refreshing: " + p.getName()));
			}
			return;
		}
		//Handles /skin user --> /skin set skin user
		else if(args.length == 1)
			args = new String[]{"set", "skin", args[0]};
		
		if(args.length < 3 || !args[0].equals("set") && !args[0].equals("get"))
		{
			throw new WrongUsageException(this.getUsage(null), new Object[0]);
		}
		
		boolean isSkin = true;
		args[1] = args[1].toLowerCase();
		String arg = args[2].replace("\"", "").trim();
		arg = JavaUtil.isURL(arg) ? arg : arg.toLowerCase();//lowercase usernames
		if(arg.equals("empty"))
			arg = "";
		
		//Handles /skin get <protocal> username
		if(args[0].equals("get")) 
		{
			final String farg = arg;
			final String[] fargs = args;
			Thread run = new Thread(()->
			{
				Minecraft mc = Minecraft.getMinecraft();
				EntityPlayer tp = mc.player;
				if(mc.player == null || mc.world == null)
				{
					System.err.println("Player or World is NULL!");
					return;
				}
					
				SkinEntry entry = SkinCache.INSTANCE.getOrDownload(farg);
				if(entry.isEmpty) 
				{
					tp.sendMessage(new TextComponentString(EnumChatFormatting.RED + "Skin Failed to Download: " + farg));
					return;
				}
				switch(fargs[1])
				{
					case "entry":
						PlayerUtil.sendURL(tp, "Skin Entry Copied to Clipboard", "", ClickEvent.Action.OPEN_URL);
						PlayerUtil.copyClipBoard(tp, JavaUtil.toPrettyFormat(entry.serialize().toString()));
					break;
					
					case "encode":
						PlayerUtil.sendURL(tp, "Skin Entry Copied to Clipboard", "", ClickEvent.Action.OPEN_URL);
						PlayerUtil.copyClipBoard(tp, JavaUtil.toPrettyFormat(entry.encodeJSON().toString()));
					break;
					
					case "skin":
						tp.sendMessage(new TextComponentString("Skin Copied to Clipboard"));
						PlayerUtil.copyClipBoard(tp, entry.skin);
					break;
					
					case "model":
						tp.sendMessage(new TextComponentString("Model Copied to Clipboard"));
						PlayerUtil.copyClipBoard(tp, entry.hasModel() ? entry.model : "default");
					break;
					
					case "cape":
						String cape = entry.cape;
						if(cape.isEmpty())
						{
							tp.sendMessage(new TextComponentString(EnumChatFormatting.RED + "Cape is Empty For: " + EnumChatFormatting.BLUE + farg));
							break;
						}
						tp.sendMessage(new TextComponentString("Cape Copied to Clipboard"));
						PlayerUtil.copyClipBoard(tp, cape);
					break;
					
					case "elytra":
						String elytra = entry.elytra.isEmpty() ? entry.cape : entry.elytra;
						if(elytra.isEmpty())
						{
							tp.sendMessage(new TextComponentString(EnumChatFormatting.RED + "Elytra is Empty For: " + EnumChatFormatting.BLUE + farg));
							break;
						}
						tp.sendMessage(new TextComponentString("Elytra Copied to Clipboard"));
						PlayerUtil.copyClipBoard(tp, elytra);
					break;
					
					default:
						tp.sendMessage(new TextComponentString(EnumChatFormatting.RED + "Unkown Skin Protocal:" + fargs[1]));
				}
			});
			run.setDaemon(true);
			run.start();
			return;
		}
		
		//Handles /skin set <protocal> user
		switch(args[1])
		{
			case "skin":
				SkinCaps.skin = arg;
			break;
			
			case "cape":
				SkinCaps.cape = arg;
			break;
			
			case "model":
				if(!arg.isEmpty() && !arg.equals("slim") && !arg.equals("default"))
					throw new WrongUsageException(this.getUsage(null), new Object[0]);
				SkinCaps.model = arg;
			break;
			
			case "elytra":
				SkinCaps.elytra = arg;
			break;
			
			case "mouse_ears":
			case "ears":
				SkinCaps.ears = arg.equals("true");
				SkinCaps.syncCaps();
				isSkin = false;
			break;
			
			case "dinnerbone":
			case "grumm":
				SkinCaps.dinnerbone = arg.equals("true");
				SkinCaps.syncCaps();
				isSkin = false;
			break;
			
			default:
				throw new WrongUsageException(EnumChatFormatting.RED + "Unkown Skin Protocal:" + args[1], new Object[0]);
		}
		
		if(isSkin)
			SkinCache.INSTANCE.refreshClientSkin();
		else
			SkinCaps.saveConfig();
	}

}
