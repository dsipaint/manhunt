package com.fin.main;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import utils.Data;
import utils.TimeManager;

public class CommandListener implements CommandExecutor
{
	private Main plugin;
	private Whitelister whitelister;
	private int whitelist_time;
	
	public CommandListener(Main plugin)
	{
		whitelist_time = TimeManager.getTicks("30m"); //default set to 30m
		
		this.plugin = plugin;
		
		plugin.getCommand("settimer").setExecutor(this);
		plugin.getCommand("toggletimer").setExecutor(this);
		plugin.getCommand("bypass").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(sender.hasPermission("manhunt.changetimer"))
		{
			//specifies a valid time
			if(label.equalsIgnoreCase("settimer"))
			{
				if(args.length >= 1)
				{
					whitelist_time = TimeManager.getTicks(args[0]);
					//time will be updated on next toggle anyway, if toggled off
					if(Main.isTimerOn)
					{
						whitelister.cancel(); //restart the whitelisting process with the new time
						whitelister = new Whitelister(plugin);//once the thread is cancelled a new one must be created
						whitelister.runTaskTimer(plugin, whitelist_time, whitelist_time);
					}
					
					if(args[0].matches("\\d+(s|m|h)"))
						sender.sendMessage(utils.Chat.format(plugin.getConfig().getString("whitelist_time_change_msg").replace("<time>", args[0])));
					else
						sender.sendMessage(utils.Chat.format(plugin.getConfig().getString("whitelist_time_change_err_msg")));
				}
				else
					sender.sendMessage(utils.Chat.format(plugin.getConfig().getString("whitelist_time_change_err_msg")));
								
				return true;
			}
			
			//toggles timer
			if(label.equalsIgnoreCase("toggletimer"))
			{
				Main.isTimerOn = !Main.isTimerOn;
				
				if(Main.isTimerOn)
				{
					whitelister = new Whitelister(plugin); //if this code is activated, the cancel method has already been called
					whitelister.runTaskTimer(plugin, whitelist_time, whitelist_time);
				}
				else
					whitelister.cancel();
					
				sender.sendMessage(utils.Chat.format(plugin.getConfig().getString("timer_toggle_msg").replace("<onoff>", Main.isTimerOn ? "ON":"OFF")));
				return true;
			}
		}
		
		if(sender.hasPermission("manhunt.bypass"))
		{
			if(label.equalsIgnoreCase("bypass"))
			{
				if(args.length >= 1)
				{
					try
					{
						Statement s = Main.con.createStatement();
						
						/*
						 * this toggles a user's bypass only if they've joined the server before
						 */
						if(Data.getResultSize(s.executeQuery("select username from ManhuntWhitelistedUsers where username = \"" + args[0] + "\"")) == 1)
						{
							ResultSet rs = s.executeQuery("select bypass from ManhuntWhitelistedUsers where username = \"" + args[0] + "\"");
							rs.absolute(1); //check first row of data
							boolean isBypassed = rs.getBoolean("bypass");
							
							//their bypass status is changed to "false" if they're already bypassed
							if(isBypassed)
							{
								s.executeUpdate("update ManhuntWhitelistedUsers set bypass = 0 where username = \"" + args[0] + "\"");
								sender.sendMessage(utils.Chat.format(plugin.getConfig().getString("bypass_false_msg").replace("<player>", args[0])));
							}
							else //set to "true" if not already bypassed
							{
								s.executeUpdate("update ManhuntWhitelistedUsers set bypass = 1 where username = \"" + args[0] + "\"");
								sender.sendMessage(utils.Chat.format(plugin.getConfig().getString("bypass_true_msg").replace("<player>", args[0])));
							}
						}
						else
							sender.sendMessage(utils.Chat.format(plugin.getConfig().getString("player_not_joined_err")));
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
