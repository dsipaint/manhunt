package com.fin.main;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import utils.Data;

public class Whitelister extends BukkitRunnable
{
	private Main plugin;
	private Random r;
	
	public Whitelister(Main plugin)
	{
		this.plugin = plugin;
		r = new Random();
	}
	
	@Override public void run()
	{		
		//add player from database to the whitelist
		try
		{
			Statement s = Main.con.createStatement();
			int data_size = Data.getResultSize(s.executeQuery("select * from ManhuntQueuedUsers"));
			if(data_size > 0)
			{
				int random = r.nextInt(data_size) + 1;
				String username = Data.getResultByIndex(random, "submitted_name", "ManhuntQueuedUsers", Main.con);
				String discID = Data.getResultByIndex(random, "discord_id", "ManhuntQueuedUsers", Main.con);
				//whitelist them
				ConsoleCommandSender console = Bukkit.getConsoleSender();
				Bukkit.dispatchCommand(console, "whitelist add " + username);
				
				s.executeUpdate("insert into ManhuntWhitelistedUsers (discord_id, username, bypass) values (\"" + discID + "\", \"" + username + "\", 0)");
				s.executeUpdate("delete from ManhuntQueuedUsers where submitted_name = \"" + username + "\"");
				Main.jda.getTextChannelById(Main.channel_id).sendMessage(Main.jda.getTextChannelById(Main.channel_id).getGuild().getMemberById(discID).getAsMention() + " has been whitelisted!").queue();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
