package com.manhunt.main;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import utils.Data;

public class CommandListener implements CommandExecutor
{
	private Main plugin;
	
	public CommandListener(Main plugin)
	{	
		this.plugin = plugin;	
		plugin.getCommand("bypass").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{		
		if(sender.hasPermission("manhunt.bypass"))
		{
			if(label.equalsIgnoreCase("bypass"))
			{
				if(args.length >= 1)
				{
					try
					{
						Statement s = Main.con.createStatement();
						
						if(Data.getResultSize(s.executeQuery("select username from ManhuntBypassedUsers where username = \"" + args[0] + "\"")) == 1)
						{	
							//their bypass is removed if they had it
							s.executeUpdate("delete from ManhuntBypassedUsers where username = \"" + args[0] + "\"");
							sender.sendMessage(utils.Chat.format(plugin.getConfig().getString("bypass_false_msg").replace("<player>", args[0])));
						}
						else //bypass is added if they didn't
						{
							s.executeUpdate("insert into ManhuntBypassedUsers (username) values (\"" + args[0] + "\")");
							sender.sendMessage(utils.Chat.format(plugin.getConfig().getString("bypass_true_msg").replace("<player>", args[0])));
						}
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
				}
				return true;
			}
		}
		return false;
	}
}
