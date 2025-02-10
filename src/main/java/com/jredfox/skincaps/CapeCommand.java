package com.jredfox.skincaps;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

public class CapeCommand extends SkinCommand {
	
	@Override
	public String getName() 
	{
		return SkinCaps.simpleNames ? "cape" : "cape_cap";
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/" + this.getName() + " [username, url]";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException 
	{
		if(args.length != 1)
			throw new WrongUsageException(this.getUsage(null), new Object[0]);
		
		super.execute(server, sender, new String[]{"set", "cape", args[0]});
	}

}
