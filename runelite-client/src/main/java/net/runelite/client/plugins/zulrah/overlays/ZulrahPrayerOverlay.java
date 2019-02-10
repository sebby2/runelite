/*
 * Copyright (c) 2017, Devin French <https://github.com/devinfrench>
 * Copyright (c) 2019, Frosty Fridge <https://github.com/frostyfridge>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.zulrah.overlays;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.zulrah.ZulrahInstance;
import net.runelite.client.plugins.zulrah.ZulrahPlugin;
import net.runelite.client.plugins.zulrah.phase.ZulrahPhase;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

@Slf4j
public class ZulrahPrayerOverlay extends Overlay
{
	private final Client client;
	private final ZulrahPlugin plugin;
	private final SpriteManager spriteManager;
	private final PanelComponent imagePanelComponent = new PanelComponent();
	@Inject
	ZulrahPrayerOverlay(Client client, ZulrahPlugin plugin, SpriteManager spriteManager)
	{
		super(plugin);
		setPosition(OverlayPosition.BOTTOM_RIGHT);
		setPriority(OverlayPriority.MED);
		this.client = client;
		this.plugin = plugin;
		this.spriteManager = spriteManager;
		imagePanelComponent.setPreferredSize(new Dimension(60, 0));

	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		ZulrahInstance instance = plugin.getInstance();

		if (instance == null)
		{
			return null;
		}

		ZulrahPhase currentPhase = instance.getPhase();
		if (currentPhase == null)
		{
			return null;
		}

		Prayer prayer = currentPhase.isJad() ? null : currentPhase.getPrayer();
		if (prayer == null || client.isPrayerActive(prayer))
		{
			return null;
		}

		BufferedImage prayerImage = getPrayerImage(prayer);
		imagePanelComponent.getChildren().clear();
		imagePanelComponent.getChildren().add(TitleComponent.builder().text("Switch!").build());
		imagePanelComponent.getChildren().add(new ImageComponent(prayerImage));
		return imagePanelComponent.render(graphics);
	}


	private BufferedImage getPrayerImage(Prayer prayer)
	{
		final int prayerSpriteID = prayer == Prayer.PROTECT_FROM_MAGIC ? SpriteID.PRAYER_PROTECT_FROM_MAGIC :
				prayer == Prayer.PROTECT_FROM_MISSILES ? SpriteID.PRAYER_PROTECT_FROM_MISSILES :
				SpriteID.PRAYER_PROTECT_FROM_MELEE;
		return spriteManager.getSprite(prayerSpriteID, 0);
	}
}
