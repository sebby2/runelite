/*
 * Copyright (c) 2017, Aria <aria@ar1as.space>
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.zulrah.ZulrahInstance;
import net.runelite.client.plugins.zulrah.ZulrahPlugin;
import net.runelite.client.plugins.zulrah.phase.ZulrahPhase;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;

@Slf4j
public class ZulrahOverlay extends Overlay
{
	private static final Color TILE_BORDER_COLOR = new Color(0, 0, 0, 100);
	private static final Color NEXT_TEXT_COLOR = new Color(255, 255, 255, 100);

	private final Client client;
	private final ZulrahPlugin plugin;
	private final SpriteManager spriteManager;

	@Inject
	ZulrahOverlay(Client client, ZulrahPlugin plugin, SpriteManager spriteManager)
	{
		setPosition(OverlayPosition.DYNAMIC);
		this.client = client;
		this.plugin = plugin;
		this.spriteManager = spriteManager;
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
		ZulrahPhase nextPhase = instance.getNextPhase();
		if (currentPhase == null)
		{
			return null;
		}

		LocalPoint startTile = instance.getStartLocation();
		if (nextPhase != null && currentPhase.getStandLocation() == nextPhase.getStandLocation())
		{
			drawStandTiles(graphics, startTile, currentPhase, nextPhase);
		}
		else
		{
			drawStandTile(graphics, startTile, currentPhase, false);
			drawStandTile(graphics, startTile, nextPhase, true);
		}
		drawZulrahTileMinimap(graphics, startTile, currentPhase, false);
		drawZulrahTileMinimap(graphics, startTile, nextPhase, true);

		return null;
	}

	private void drawStandTiles(Graphics2D graphics, LocalPoint startTile, ZulrahPhase currentPhase, ZulrahPhase nextPhase)
	{
		LocalPoint standTile = currentPhase.getStandTile(startTile);
		Point localTile = Perspective.localToCanvas(client, standTile, client.getPlane());
		localTile = new Point(localTile.getX() + Perspective.LOCAL_TILE_SIZE / 2, localTile.getY() + Perspective.LOCAL_TILE_SIZE / 2);
		Polygon northPoly = getCanvasTileNorthPoly(client, localTile);
		Polygon southPoly = getCanvasTileSouthPoly(client, localTile);

		Polygon poly = Perspective.getCanvasTilePoly(client, standTile);
		Point textLoc = Perspective.getCanvasTextLocation(client, graphics, standTile, "Next", 0);
		if (northPoly != null && southPoly != null && poly != null && textLoc != null)
		{
			Color northColor = currentPhase.getColor();
			Color southColor = nextPhase.getColor();
			graphics.setColor(northColor);
			graphics.fillPolygon(northPoly);
			graphics.setColor(southColor);
			graphics.fillPolygon(southPoly);
			graphics.setColor(TILE_BORDER_COLOR);
			graphics.setStroke(new BasicStroke(2));
			graphics.drawPolygon(poly);
			graphics.setColor(NEXT_TEXT_COLOR);
			graphics.drawString("Next", textLoc.getX(), textLoc.getY());
		}
		if (nextPhase.isJad())
		{
			Image jadPrayerImg = getPrayerImage(nextPhase.getPrayer());
			if (jadPrayerImg != null)
			{
				Point imageLoc = Perspective.getCanvasImageLocation(client, standTile, (BufferedImage) jadPrayerImg, 0);
				if (imageLoc != null)
				{
					graphics.drawImage(jadPrayerImg, imageLoc.getX(), imageLoc.getY(), null);
				}
			}
		}
	}

	private void drawStandTile(Graphics2D graphics, LocalPoint startTile, ZulrahPhase phase, boolean next)
	{
		if (phase == null)
		{
			return;
		}
		LocalPoint standTile = phase.getStandTile(startTile);
		Polygon poly = Perspective.getCanvasTilePoly(client, standTile);
		Color color = phase.getColor();
		if (poly != null)
		{
			graphics.setColor(TILE_BORDER_COLOR);
			graphics.setStroke(new BasicStroke(2));
			graphics.drawPolygon(poly);
			graphics.setColor(color);
			graphics.fillPolygon(poly);
		}
		if (next)
		{
			Point textLoc = Perspective.getCanvasTextLocation(client, graphics, standTile, "Next", 0);
			if (textLoc != null)
			{
				graphics.setColor(NEXT_TEXT_COLOR);
				graphics.drawString("Next", textLoc.getX(), textLoc.getY());
			}
			if (phase.isJad())
			{
                Image jadPrayerImg = getPrayerImage(phase.getPrayer());
				if (jadPrayerImg != null)
				{
					Point imageLoc = Perspective.getCanvasImageLocation(client, standTile, (BufferedImage) jadPrayerImg, 0);
					if (imageLoc != null)
					{
						graphics.drawImage(jadPrayerImg, imageLoc.getX(), imageLoc.getY(), null);
					}
				}
			}
		}
	}

	private void drawZulrahTileMinimap(Graphics2D graphics, LocalPoint startTile, ZulrahPhase phase, boolean next)
	{
		if (phase == null)
		{
			return;
		}
		LocalPoint zulTile = phase.getZulrahTile(startTile);
		Point zulrahMinimapPoint = Perspective.localToMinimap(client, zulTile);
		Color color = phase.getColor();
		graphics.setColor(color);
		graphics.fillOval(zulrahMinimapPoint.getX(), zulrahMinimapPoint.getY(), 5, 5);
		graphics.setColor(TILE_BORDER_COLOR);
		graphics.setStroke(new BasicStroke(1));
		graphics.drawOval(zulrahMinimapPoint.getX(), zulrahMinimapPoint.getY(), 5, 5);
		if (next)
		{
			graphics.setColor(NEXT_TEXT_COLOR);
			FontMetrics fm = graphics.getFontMetrics();
			graphics.drawString("Next", zulrahMinimapPoint.getX() - fm.stringWidth("Next") / 2, zulrahMinimapPoint.getY() - 2);
		}
	}

	private Polygon getCanvasTileNorthPoly(Client client, Point localLocation)
	{
		int plane = client.getPlane();
		int halfTile = Perspective.LOCAL_TILE_SIZE / 2;

		Point p1 = Perspective.localToCanvas(client, new LocalPoint(localLocation.getX() - halfTile, localLocation.getY() - halfTile), plane);
		Point p2 = Perspective.localToCanvas(client, new LocalPoint(localLocation.getX() - halfTile, localLocation.getY() + halfTile), plane);
		Point p3 = Perspective.localToCanvas(client, new LocalPoint(localLocation.getX() + halfTile, localLocation.getY() + halfTile), plane);

		if (p1 == null || p2 == null || p3 == null)
		{
			return null;
		}

		Polygon poly = new Polygon();
		poly.addPoint(p1.getX(), p1.getY());
		poly.addPoint(p2.getX(), p2.getY());
		poly.addPoint(p3.getX(), p3.getY());

		return poly;
	}

	private Polygon getCanvasTileSouthPoly(Client client, Point localLocation)
	{
		int plane = client.getPlane();
		int halfTile = Perspective.LOCAL_TILE_SIZE / 2;

		Point p1 = Perspective.localToCanvas(client, new LocalPoint(localLocation.getX() - halfTile, localLocation.getY() - halfTile), plane);
		Point p2 = Perspective.localToCanvas(client, new LocalPoint(localLocation.getX() + halfTile, localLocation.getY() + halfTile), plane);
		Point p3 = Perspective.localToCanvas(client, new LocalPoint(localLocation.getX() + halfTile, localLocation.getY() - halfTile), plane);

		if (p1 == null || p2 == null || p3 == null)
		{
			return null;
		}

		Polygon poly = new Polygon();
		poly.addPoint(p1.getX(), p1.getY());
		poly.addPoint(p2.getX(), p2.getY());
		poly.addPoint(p3.getX(), p3.getY());

		return poly;
	}


	private BufferedImage getPrayerImage(Prayer prayer)
	{
		final int prayerSpriteID = prayer == Prayer.PROTECT_FROM_MAGIC ? SpriteID.PRAYER_PROTECT_FROM_MAGIC :
				prayer == Prayer.PROTECT_FROM_MISSILES ? SpriteID.PRAYER_PROTECT_FROM_MISSILES :
						SpriteID.PRAYER_PROTECT_FROM_MELEE;
		return spriteManager.getSprite(prayerSpriteID, 0);
	}
}
