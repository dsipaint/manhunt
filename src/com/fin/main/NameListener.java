package com.fin.main;
import java.sql.SQLException;
import java.sql.Statement;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.Data;

public class NameListener extends ListenerAdapter
{
	/*
	 * Listens for names to be submitted for the plugin
	 * in a specific channel (one account per user)
	 */
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent e)
	{
		String msg = e.getMessage().getContentRaw();
		String[] arguments = msg.split(" ");
		String userID = e.getAuthor().getId();
		
		//>add (in the right channel)
		if(e.getChannel().getId().equals(Main.channel_id) && arguments[0].equalsIgnoreCase(Main.PREFIX + "add") && arguments.length == 2)
		{	
			String name = arguments[1];
			
			try
			{
				//input sanitisation- only allowed a combination of letters, numbers or underscores
				if(!name.matches("[\\w|_]+"))
					return;
				
				//minecraft names fit this criteria
				if(name.length() < 3 || name.length() > 16)
					return;
				
				//if discord id is found in ManhuntWhitelistedUsers, don't allow them to submit a name whilst they are whitelisted
				if(Data.isInTable(userID, "discord_id", "ManhuntWhitelistedUsers", Main.con))
					return;
				
				//if a name is already whitelisted, don't allow resubmission via another account
				if(Data.isInTable(name, "username", "ManhuntWhitelistedUsers", Main.con))
					return;
				
				Statement s = Main.con.createStatement();
				
				//if user is in database already
				if(Data.isInTable(userID, "discord_id", "ManhuntQueuedUsers", Main.con))
					s.executeUpdate("update ManhuntQueuedUsers set submitted_name = \"" + name + "\" where discord_id = \"" + userID + "\"");
				else //if not
					s.executeUpdate("insert into ManhuntQueuedUsers (discord_id, submitted_name) values (\"" + userID + "\", \"" + name + "\")");
			}
			catch (SQLException e2)
			{
				e2.printStackTrace();
			}
		}
	}
}
