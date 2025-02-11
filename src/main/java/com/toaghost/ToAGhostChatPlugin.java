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

	private static final Set<Integer> TOA_REGION_IDS = Set.of(
			14160, 15186, 15698, 14162, 14674, 15188, 14164,
			14676, 15700, 15184, 15696, 14672
	);

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

				if (healthRatio == 0 && !ghostPlayers.contains(playerName)) {
					ghostPlayers.add(playerName);
				}

				if (healthRatio > 0 && ghostPlayers.contains(playerName)) {
					ghostPlayers.remove(playerName);
					player.setOverheadText(null);
				}
			}
		} else {
			clearGhostPlayers();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (!isInToA()) {
			return;
		}

		String message = event.getMessage();

		if (message.startsWith("Challenge complete:") ||
				message.startsWith("Your party failed to complete the challenge")) {
			clearGhostPlayers();
		}

		// Handle ghost chat modification
		Player player = findPlayerByName(event.getName());
		if (player != null && ghostPlayers.contains(player.getName())) {
			String ghostMessage = getRandomGhostMessage();
			event.getMessageNode().setValue(ghostMessage);
			player.setOverheadText(ghostMessage);
			client.refreshChat();
		}
	}

	private boolean isInToA() {
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
		if (ThreadLocalRandom.current().nextDouble() < 0.001) {
			return "uwu";
		}

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
