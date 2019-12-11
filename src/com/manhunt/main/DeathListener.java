package com.manhunt.main;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import utils.Data;

public class DeathListener implements Listener
{
	/*
	 * listener will un-whitelist a player upon death or set them into
	 * spectator mode if they have been bypassed
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
			ResultSet results = Main.con.createStatement().executeQuery("select username from ManhuntBypassedUsers where username = \"" + username + "\"");
			results.absolute(1); //first entry
			
			if(Data.getResultSize(results) > 0)
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
}
