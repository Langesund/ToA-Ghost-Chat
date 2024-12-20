package com.toaghost;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;


public class ToAGhostChatPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ToAGhostChatPlugin.class);
		RuneLite.main(args);
	}
}