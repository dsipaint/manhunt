package com.fin.main;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import utils.Data;

public class DeathListener implements Listener
{
	/*
	 * listener will un-whitelist a player upon death or set them into
	 * spectator mode if they have been bypassed
	 * 
	 * also acts as a joinListener, will fix people's database entries when they
	 * join the server, if they were whitelisted by an admin and skipped the queue
	 */
	
	private Main plugin;
	
	public DeathListener(Main plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler
	public boolean onPlayerDeath(PlayerDeathEvent e)
	{
		Player p = e.getEntity();
		String username = p.getName();
		try
		{
			ResultSet results = Main.con.createStatement().executeQuery("select bypass from ManhuntWhitelistedUsers where username = \"" + username + "\"");
			results.absolute(1); //first entry
			boolean isBypassed = results.getBoolean(1); //get the bypass status
			
			if(isBypassed)
			{
				p.setGameMode(GameMode.SPECTATOR);
			}
			else
			{
				p.kickPlayer(utils.Chat.format(plugin.getConfig().getString("kick_msg").replace("<break>", "\n")));
				p.setWhitelisted(false);
			}
		}
		catch (SQLException e1)
		{
			e1.printStackTrace();
		}
		return true;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
			try
			{
				//if user isn't already logged in the database through being submitted (e.g. they've been whitelisted by an admin hence bypassing the system)
				if(!Data.isInTable(e.getPlayer().getName(), "username", "ManhuntWhitelistedUsers", Main.con))
					Main.con.createStatement().executeUpdate("insert into ManhuntWhitelistedUsers (username, bypass) values (\"" + e.getPlayer().getName() + "\", 0)");
				//if user joined (presumably manually whitelisted) but was still in the queue
				if(Data.isInTable(e.getPlayer().getName(), "submitted_name", "ManhuntQueuedUsers", Main.con))
					Main.con.createStatement().executeUpdate("delete from ManhuntQueuedUsers where submitted_name = \"" + e.getPlayer().getName() + "\"");
			}
			catch (SQLException e1)
			{
				e1.printStackTrace();
			}
	}
}
