package com.gmail.tomahawkmissile2.pexrankup;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Main extends JavaPlugin implements Listener, CommandExecutor {

	public static Main plugin;
	
	private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;
	
	@Override
	public void onEnable() {
		Main.plugin=this;
		if(Bukkit.getServer().getPluginManager().getPlugin("PermissionsEx")==null) {
			System.out.println(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" Plugin disabled because PermissionsEx is not installed.");
			Bukkit.getServer().getPluginManager().disablePlugin(this);
			return;
		}
		if (!setupEconomy()) {
			System.out.println(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" Plugin disabled because Vault is not installed.");
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();
        setupChat();
		
		this.getServer().getPluginManager().registerEvents(this,this);
		if(!new File(this.getDataFolder()+"/").exists()) {
			if(!new File(this.getDataFolder()+"/").mkdir());
		}
		if(!new File(this.getDataFolder()+"/config.yml").exists()) {
			try {
				new File(this.getDataFolder()+"/config.yml").createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.getCommand("rankup").setExecutor(this);
		this.getCommand("ranks").setExecutor(this);
	}
	@Override
	public void onDisable() {}
	
	public boolean groupExists(String groupName) {
		for(String s:perms.getGroups()) {
			if(s.equals(groupName)) return true;
		}
		return false;
	}
	public void rankupPlayer(Player p) {
		p.sendMessage(ChatColor.GOLD+"[Rankup] "+ChatColor.YELLOW+"Attemping rankup...");
		PermissionUser user = PermissionsEx.getUser(p);
		if(Config.get("ranks")==null) {
			System.out.println(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" Error: config is empty.");
			p.sendMessage(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" Unable to rank up due to a server configuration error. Please contact an administrator.");
			return;
		}
		if(user.getParentIdentifiers().size()!=1) {
			p.sendMessage(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" Unable to rank up because you have multiple ranks. Please contact an administrator if this is an error.");
		}
		String rank = user.getParentIdentifiers().get(0);
		List<String> ranks = Config.getSectionHeaders("ranks");
		int currentRankId=0,nextId=0;
		double cost=0.0;
		boolean def=false;
		if(ranks.contains(rank)) {
			try {
				currentRankId = Integer.parseInt((String) Config.get("ranks."+rank+".id").toString());
			} catch(NumberFormatException|ClassCastException|NullPointerException e) {
				p.sendMessage(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" Unable to rank up due to a server configuration error. Please contact an administrator.");
				System.out.println(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" Invalid config for rank: "+rank+". ID is missing.");
				return;
			}
			for(int i=0;i<ranks.size();i++) {
				if(i==ranks.size()-1) {
					p.sendMessage(ChatColor.GOLD+"[Rankup] "+ChatColor.YELLOW+"You are the maximum rank!");
					return;
				}
				String s = ranks.get(i+1);
				if(!this.groupExists(s)) {
					continue;
				}
				try {
					nextId = Integer.parseInt((String) Config.get("ranks."+s+".id").toString());
					def = Config.get("ranks."+s+".default")==null?false:Boolean.parseBoolean((String) Config.get("ranks."+s+".default").toString());
				} catch(NumberFormatException|ClassCastException|NullPointerException e) {
					p.sendMessage(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" Unable to rank up due to a server configuration error. Please contact an administrator.");
					System.out.println(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" Invalid config for rank: "+s+". Default or ID is missing.");
					return;
				}
				try {
					if(def) {
						p.sendMessage(ChatColor.GOLD+"[Rankup]"+ChatColor.YELLOW+" You should already have this rank by default! If you do not, please contact an administrator.");	
					} else {
						cost = Double.parseDouble((String) Config.get("ranks."+s+".cost").toString());
					}
				} catch(NumberFormatException|ClassCastException|NullPointerException e) {
					p.sendMessage(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" Unable to rank up due to a server configuration error. Please contact an administrator.");
					System.out.println(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" Invalid config for rank: "+s+". Cost is missing!");
					return;
				}
				if(nextId==currentRankId+1) {
					if(econ.getBalance(p)<cost && (!p.hasPermission("rankup.cost.exempt"))) {
						p.sendMessage(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" You do not have enough funds to rankup!");	
					} else {
						user.removeGroup(rank);
						user.addGroup(ranks.get(i+1));
						econ.withdrawPlayer(p, cost);
						p.sendMessage(ChatColor.DARK_GREEN+"[Rankup] "+ChatColor.GREEN+"You are now rank: "+ranks.get(i+1));
						System.out.println(ChatColor.DARK_GREEN+"[Rankup]"+ChatColor.GREEN+" Ranking up user "+p.getName()+" to rank "+ranks.get(i+1)+".");
						Bukkit.getServer().broadcastMessage(ChatColor.DARK_GREEN+"[Rankup]"+ChatColor.GREEN+" "+p.getName()+" has ranked up to "+ranks.get(i+1)+"!");
					}
					return;
				}
			}
			p.sendMessage(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" Unable to rank up due to a server configuration error. Please contact an administrator.");
		} else {
			p.sendMessage(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" Unable to rank up because your rank is not configured (you might be an admin). Please contact an administrator if this is an error.");
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(cmd.getName().equalsIgnoreCase("rankup")) {
				if(!p.hasPermission("rankup.use")) {
					p.sendMessage(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" You do not have permission.");
					return true;
				}
				if(args.length==0) {
					this.rankupPlayer(p);
				} else {
					p.sendMessage(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" Invalid command.");
				}
				return true;
			} else if(cmd.getName().equalsIgnoreCase("ranks")) {
				if(!p.hasPermission("rankup.list")) {
					p.sendMessage(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" You do not have permission.");
					return true;
				}
				if(args.length==0) {
					List<String> ranks = Config.getSectionHeaders("ranks");
					if(ranks==null||ranks.isEmpty()) {
						p.sendMessage(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" Unable to rank up due to a server configuration error. Please contact an administrator.");
						System.out.println(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" Config is empty.");
					} else {
						p.sendMessage(ChatColor.DARK_GREEN+"[Rankup] "+ChatColor.GREEN+"Available ranks:");
						for(String s:ranks) {
							String def="";
							String cost="";
							String id="";
							try {
								id=Config.get("ranks."+s+".id").toString();
							} catch(NullPointerException e) {
								id="ERROR";
							}
							try {
								cost=Config.get("ranks."+s+".cost").toString();
							} catch(NullPointerException e) {
								cost="ERROR";
							}
							try {
								def=Config.get("ranks."+s+".default").toString();
							} catch(NullPointerException e) {
								def="ERROR";
							}
							if(def.equals("true")) {
								p.sendMessage(ChatColor.GREEN+"- Rank "+s+" (#"+id+") is default and has no cost.");
							} else {
								p.sendMessage(ChatColor.GREEN+"- Rank "+s+" (#"+id+") costs $"+cost+".");
							}
						}
						p.sendMessage(" ");
					}
				} else {
					p.sendMessage(ChatColor.DARK_RED+"[Rankup]"+ChatColor.RED+" Invalid command.");
				}
				return true;
			}
		}
		return false;
	}
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }
    
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
}
