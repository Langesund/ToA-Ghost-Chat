package com.toaghost;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@PluginDescriptor(
		name = "ToA Ghost Chat",
		description = "Replaces messages from ghost players in Tombs of Amascut with ghostly phrases.",
		tags = {"toa", "tombs", "chat", "ghost"}
)
public class ToAGhostChatPlugin extends Plugin {

	@Inject
	private Client client;

	private final Set<String> ghostPlayers = new HashSet<>();

	// List of Tombs of Amascut region IDs
	private static final Set<Integer> TOA_REGION_IDS = Set.of(
			14160, 15186, 15698, 14162, 14674, 15188, 14164,
			14676, 15700, 15184, 15696, 14672
	);

	// List of ghostly messages
	private final String[] ghostMessages = {
			"woowoowoo",
			"Wooo000oooooo!",
			"whooooooo",
			"OOoooOOOOo...",
			"Boooooo!",
			"Wooooooowoo!"
	};

	@Subscribe
	public void onGameTick(GameTick event) {
		if (isInToA()) {
			for (Player player : client.getPlayers()) {
				if (player == null || player.getName() == null) {
					continue;
				}

				String playerName = player.getName();
				int healthRatio = player.getHealthRatio();

				// If player health is 0 and not already marked as a ghost, add them to ghost state
				if (healthRatio == 0 && !ghostPlayers.contains(playerName)) {
					ghostPlayers.add(playerName);
				}

				// If player is in ghostPlayers but their health is restored, remove them from ghost state
				if (healthRatio > 0 && ghostPlayers.contains(playerName)) {
					ghostPlayers.remove(playerName);
					player.setOverheadText(null); // Clear overhead text
				}
			}
		} else {
			// Reset all ghost states when leaving ToA
			clearGhostPlayers();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (!isInToA()) {
			return; // Do nothing if not in ToA
		}

		String message = event.getMessage();

		// Detect room completion, boss death, or room failure messages
		if (message.startsWith("Challenge complete:") ||
				message.startsWith("Your party failed to complete the challenge")) {
			clearGhostPlayers();
		}

		// Handle ghost chat modification
		Player player = findPlayerByName(event.getName());
		if (player != null && ghostPlayers.contains(player.getName())) {
			// Generate a new random ghostly message, with a 0.1% chance of "uwu"
			String ghostMessage = getRandomGhostMessage();
			event.getMessageNode().setValue(ghostMessage); // Update chat message
			player.setOverheadText(ghostMessage); // Update overhead message
			client.refreshChat();
		}
	}

	private boolean isInToA() {
		// Check if the player's current region matches one of the ToA region IDs
		int[] currentRegions = client.getMapRegions();
		if (currentRegions == null) {
			return false;
		}

		for (int region : currentRegions) {
			if (TOA_REGION_IDS.contains(region)) {
				return true;
			}
		}
		return false;
	}

	private String getRandomGhostMessage() {
		// 0.1% chance to return "uwu"
		if (ThreadLocalRandom.current().nextDouble() < 0.001) {
			return "uwu";
		}

		// Otherwise, select a random message from the ghostMessages array
		int randomIndex = ThreadLocalRandom.current().nextInt(ghostMessages.length);
		return ghostMessages[randomIndex];
	}

	private void clearGhostPlayers() {
		ghostPlayers.clear();
	}

	private Player findPlayerByName(String name) {
		for (Player player : client.getPlayers()) {
			if (player != null && name.equals(player.getName())) {
				return player;
			}
		}
		return null;
	}
}
