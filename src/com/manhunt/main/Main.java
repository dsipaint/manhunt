package com.manhunt.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Main extends JavaPlugin
{
	static Connection con;
	static final String PREFIX = ">";
	
	/*
	 * v2.1 CHANGELOG:
	 * 
	 * Being killed by a mob caused a problem because the server would try to tick the player/mob
	 * when they were kicked/unwhitelisted on death- this crashed the server. To fix this, I scheduled
	 * these as a task called PlayerKickTask, which is scheduled on death. This seemed to fix it.
	 * 
	 */
	
	public void onEnable()
	{		
		//set up SQL
		con = getConnection();
		try
		{
			Statement s = con.createStatement();
			//setup bypassed users table
			s.executeUpdate("drop table if exists ManhuntBypassedUsers");
			s.executeUpdate("create table ManhuntBypassedUsers ("
					+ "username varchar(25) not null"
					+ ")");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		//add death listener for the server
		Bukkit.getPluginManager().registerEvents(new DeathListener(this), this);
		
		//add commands
		new CommandListener(this);
	}
	
	@Override
	public void onDisable()
	{		
		//empty SQL database
		try
		{
			con.createStatement().executeUpdate("delete from ManhuntBypassedUsers");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private Connection getConnection()
	{
		Connection con = null;
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			
			/*
			 Here is how manhuntdata.xml should look
			 <?xml version = '1.0'?>
				
				<game>
					<database>
						<username>SQL username</username>
						<password>SQL password</password>
						<location>database url</location>
						<port>3306</port>
						<databasename>database name</databasename>
					</database>
	
					<channelid>discord channel id</channelid>
				</game>
			 */
			
			//getting xml elements
			Document doc = getDocument();
			Element game = doc.getDocumentElement();
			NodeList list = game.getChildNodes();
			NodeList database = list.item(1).getChildNodes();
			Node username = database.item(1);
			Node password = database.item(3);
			Node port = database.item(5);
			Node location = database.item(7);
			Node databasename = database.item(9);
			
			//setting database values for login
			String urlstr = "jdbc:mysql://" + location.getFirstChild().getNodeValue() + ":" + port.getFirstChild().getNodeValue() + "/" + databasename.getFirstChild().getNodeValue();
			String usrstr = username.getFirstChild().getNodeValue();
			String pwdstr = password.getFirstChild().getNodeValue();
			con = DriverManager.getConnection(urlstr, usrstr, pwdstr);
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return con;
	}
	
	private Document getDocument()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);
			factory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder db = factory.newDocumentBuilder();
			return db.parse(new InputSource("./plugins/whitelisterdata.xml"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
