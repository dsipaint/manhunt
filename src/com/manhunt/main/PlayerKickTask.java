package com.manhunt.main;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerKickTask extends BukkitRunnable
{
	private Player p;
	private Main plugin;
	
	public PlayerKickTask(Main plugin, Player p)
	{
		this.p = p;
		this.plugin = plugin;
	}

	@Override
	public void run()
	{
		p.setWhitelisted(false);
		p.kickPlayer(utils.Chat.format(plugin.getConfig().getString("kick_msg").replace("<break>", "\n")));
	}
	
}
