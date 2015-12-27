package wdl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * Handles giving players capes.
 */
public class CapeHandler {
	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Map of player names to the skins to give the players.
	 * 
	 * You can convert a player name to a unique ID and vice versa using 
	 * <a href="https://namemc.com/">namemc.com</a>.
	 */
	private static final Map<UUID, ResourceLocation> capes = new HashMap<UUID, ResourceLocation>();
	
	private static final Set<EntityPlayer> handledPlayers = new HashSet<EntityPlayer>();
	/**
	 * Number of times a player's cape has failed; if they have failed too many times skip them.
	 */
	private static final Map<EntityPlayer, Integer> playerFailures = new HashMap<EntityPlayer, Integer>();
	/**
	 * Number of times the cape system has broken in total.
	 */
	private static int totalFailures = 0;
	/**
	 * Number of times the a player can fail to have a cape set up before they are skipped..
	 */
	private static final int MAX_PLAYER_FAILURES = 40;
	/**
	 * Number of times the system can fail to set up capes in general (IE, an
	 * exception was thrown during the tick).
	 */
	private static final int MAX_TOTAL_FAILURES = 40;
	
	/**
	 * Set up the name list. 
	 */
	static {
		//Pokechu22
		capes.put(UUID.fromString("6c8976e3-99a9-4d8b-a98e-d4c0c09b305b"),
				new ResourceLocation("wdl", "textures/cape_dev.png"));
		//Julialy
		capes.put(UUID.fromString("f6c068f1-0738-4b41-bdb2-69d81d2b0f1c"),
				new ResourceLocation("wdl", "textures/cape_dev.png"));
		//TODO: find the rest of the needed usernames/uuids and set up capes.
	}
	
	public static void onWorldTick(List<EntityPlayer> players) {
		if (totalFailures > MAX_TOTAL_FAILURES) {
			return;
		}
		
		try {
			handledPlayers.retainAll(players);
			
			for (EntityPlayer player : players) {
				if (handledPlayers.contains(player)) {
					continue;
				}
				
				if (player instanceof AbstractClientPlayer) {
					setupPlayer((AbstractClientPlayer)player);
				}
			}
		} catch (Exception e) {
			logger.warn("[WDL] Failed to tick cape setup", e);
			totalFailures++;
			
			if (totalFailures > MAX_TOTAL_FAILURES) {
				logger.warn("[WDL] Disabling cape system (too many failures)");
			}
		}
	}
	
	private static void setupPlayer(AbstractClientPlayer player) {
		try {
			NetworkPlayerInfo info = ReflectionUtils
					.stealAndGetField((AbstractClientPlayer)player,
							AbstractClientPlayer.class,
							NetworkPlayerInfo.class);
			
			if (info == null) {
				incrementFailure(player);
				return;
			}
			
			GameProfile profile = info.getGameProfile();
			
			if (capes.containsKey(profile.getId())) {
				setPlayerCape(info, capes.get(profile.getId()));
			}
			
			handledPlayers.add(player);
		} catch (Exception e) {
			logger.warn("[WDL] Failed to perform cape set up for " + player, e);
			incrementFailure(player);
		}
	}
	
	private static void setPlayerCape(NetworkPlayerInfo info,
			ResourceLocation cape) throws Exception {
		boolean foundBefore = false;
		Field capeField = null;
		for (Field f : info.getClass().getDeclaredFields()) {
			if (f.getType().equals(ResourceLocation.class)) {
				//We're looking for the second such field.
				if (foundBefore) {
					capeField = f;
				} else {
					foundBefore = true;
				}
			}
		}
		if (capeField != null) {
			capeField.setAccessible(true);
			capeField.set(info, cape);
		}
	}
	
	/**
	 * Increment the number of times a player has failed to get a cape.
	 */
	private static void incrementFailure(EntityPlayer player) {
		if (playerFailures.containsKey(player)) {
			int numFailures = playerFailures.get(player) + 1;
			playerFailures.put(player, numFailures);
			
			if (numFailures > MAX_PLAYER_FAILURES) {
				handledPlayers.add(player);
				playerFailures.remove(player);
				logger.warn("[WDL] Failed to set up cape for " + player
						+ " too many times (" + numFailures + "); skipping them");
			}
		} else {
			playerFailures.put(player, 1);
		}
	}
}
