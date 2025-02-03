package com.jredfox.skincaps;

import com.evilnotch.lib.main.skin.SkinCache;
import com.evilnotch.lib.util.JavaUtil;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

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
	public String getName() {
		return "skin";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/skin set [skin, cape, model, elytra] [username or URL unless model which is empty, default or slim]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException 
	{
		if(args.length < 3 || !args[0].equals("set"))
		{
			throw new WrongUsageException(this.getUsage(null), new Object[0]);
		}
		
		boolean isSkin = true;
		args[1] = args[1].toLowerCase();
		String arg = args[2].replace("\"", "").trim();
		arg = JavaUtil.isURL(arg) ? arg : arg.toLowerCase();//lowercase usernames
		if(arg.equals("empty"))
			arg = "";
		
		if(args[1].equals("skin"))
		{
			SkinCaps.skin = arg;
		}
		else if(args[1].equals("cape"))
		{
			SkinCaps.cape = arg;
		}
		else if(args[1].equals("model"))
		{
			if(!arg.isEmpty() && !arg.equals("slim") && !arg.equals("default"))
				throw new WrongUsageException(this.getUsage(null), new Object[0]);
			SkinCaps.model = arg;
		}
		else if(args[1].equals("elytra"))
		{
			SkinCaps.elytra = arg;
		}
		else if(args[1].equals("mouse_ears") || args[1].equals("ears"))
		{
			SkinCaps.ears = arg.equals("true");
			SkinCaps.syncCaps();
			isSkin = false;
		}
		else if(args[1].equals("dinnerbone"))
		{
			SkinCaps.dinnerbone = arg.equals("true");
			SkinCaps.syncCaps();
			isSkin = false;
		}
		SkinCaps.saveConfig();
		if(isSkin)
			SkinCache.INSTANCE.refreshClientSkin();
	}

}
