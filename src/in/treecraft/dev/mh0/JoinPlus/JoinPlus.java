/* Originally designed and made for TreeCraft(http://treecraft.in) by Micheal
 * Harker (http://michealhark.tk)
 * 
 * Copyright (c) 2013, Micheal Harker
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 *  *  Redistributions of source code must retain the above copyright notice, this 
 *     list of conditions and the following disclaimer.
 *  *  Redistributions in binary form must reproduce the above copyright notice, 
 *     this list of conditions and the following disclaimer in the documentation 
 *     and/or other materials provided with the distribution.
 *  *  Neither the name of the TreeCraft nor the names of its contributors may
 *     be used to endorse or promote products derived from this software without 
 *     specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package in.treecraft.dev.mh0.JoinPlus;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import uk.org.whoami.geoip.GeoIPLookup;
import uk.org.whoami.geoip.GeoIPTools;

public class JoinPlus extends JavaPlugin implements Listener {
	private GeoIPLookup geo = null;
	private String leaveMessage;
	private String joinMessage;
	private String kickMessage;
	
	@Override
	public void onEnable() {
		initConfigs();
		initGeoIP();
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	private void initConfigs() {
		final FileConfiguration config = getConfig();
		
		leaveMessage = config.getString("leavemsg", "&c<-- &e{PLAYER}&c disconnected");
		joinMessage = config.getString("joinmsg", "&a--> &e{PLAYER}&a connected from &b{COUNTRY}&a.");
		kickMessage = config.getString("kickmsg", "&c<-- &e{PLAYER}&c was kicked ({REASON})");
	
		config.set("joinmsg", joinMessage);
		config.set("leavemsg", leaveMessage);
		config.set("kickmsg", kickMessage);
		saveConfig();
		
		leaveMessage = ChatColor.translateAlternateColorCodes('&', leaveMessage);
		joinMessage = ChatColor.translateAlternateColorCodes('&', joinMessage);
		kickMessage = ChatColor.translateAlternateColorCodes('&', kickMessage);
	}
	
	private void initGeoIP() {
		if (!joinMessage.contains("{COUNTRY}")) {
			getLogger().info("Country support not enabled becuase no {COUNTRY} in 'message'.");
			return;
		}
		
		Boolean caughtException = false;
		
		try {
			Plugin plugin = this.getServer().getPluginManager().getPlugin("GeoIPTools");

			if( plugin != null ) {
				geo = ((GeoIPTools) plugin).getGeoIPLookup();
			}
		} catch( NullPointerException e ) {
			caughtException = true;

			getLogger( ).warning("Exception happened during GeoIPTools load: "+e.getMessage());
			getLogger( ).warning("Make sure you are running updated version!");
		} finally {
			if( !caughtException && geo == null ) {
				getLogger().warning("Failed to load GeoIPTools.");
				getLogger().warning("Are you sure 'GeoIPTools' plugin is running?");
			}
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String msg = joinMessage;
		
		if (geo != null) {
			String country = geo.getCountry(player.getAddress().getAddress()).getName();
			if (country.equals("N/A")) {
				country = "Unknown";
			}
			
			msg = msg.replace("{COUNTRY}", country);
					
			event.setJoinMessage(msg.replace("{PLAYER}", player.getDisplayName()));
		}
	}
		
	@EventHandler(ignoreCancelled=true)
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String msg = leaveMessage;
		
		event.setQuitMessage(msg.replace("{PLAYER}",player.getDisplayName()));
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onPlayerKickEvent(PlayerKickEvent event) {
		Player player = event.getPlayer();
		String msg = kickMessage;
		String reason = event.getReason();
		
		msg = msg.replace("{REASON}", reason);
		
		event.setLeaveMessage(msg.replace("{PLAYER}",player.getDisplayName()));
	}
}
