/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Christian 'numpad' Schäl
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
 */

package com.numpad.kageditor;

import java.awt.image.BufferedImage;

import com.numpad.kageditor.Tile;

import de.numpad.framework.IO.ImageLoader;
import de.numpad.framework.math.HEX;
import de.numpad.framework.tool.api.Plugin;

/*
 * Tile: informaton about color, sprite, size etc..
 * Block: the thing with tile that gets rendered etc
 */
public class Tile {
	public static BufferedImage set;
	public static Tile[] tiles;
	public static int imageSize;
	
	public static int size = 32;
	
	public BufferedImage sprite;
	
	static {
		set = ImageLoader.get("data/img/tiles.png");
		
		Plugin tl = new Plugin("data/scr/tiles.ps");
		String[] names = tl.value_array("names");
		String[] colors = tl.value_array("colors");
		int[] positions = tl.value_array_int("positions");
		imageSize = tl.value_int("size", "8");
		tiles = new Tile[names.length];
		
		for (int i = 0; i < tiles.length; i++) {
			tiles[i] = new Tile((int)(positions[i] * Tile.imageSize), names[i], colors[i]);
		}
	}
	
	public static int getIndexByColor(int r, int g, int b) {
		for (int i = 0; i < tiles.length; i++) {
			if (tiles[i].r == r && tiles[i].g == g && tiles[i].b == b) {
				return i;
			}
		}
		return 0;
	}
	
	public static Tile getTileByColor(int r, int g, int b) {
		return tiles[getIndexByColor(r, g, b)];
	}
	
	public static int getIndexByName(String name) {
		for (int i = 0; i < tiles.length; i++) {
			if (tiles[i].name.toLowerCase().replace(" ", "").equals(name.toLowerCase().replace(" ", ""))) {
				return i;
			}
		}
		return 0;
	}
	
	public static Tile getTileByName(String name) {
		return tiles[getIndexByName(name)];
	}
	
	public String name = "";
	public int r, g, b; // Represents the pixelcolor when exporting (ingame: block)
	public int position; // Source X
	public Extra extra = Extra.Color;
	
	private BufferedImage imageInMenu;
	public BufferedImage icon() {
		return imageInMenu;
	}
	
	public Tile(int position, String name, String color) {
		this.name = name;
		this.position = position;
		String[] c = color.split("/");
		r = Integer.valueOf(c[0]);
		g = Integer.valueOf(c[1]);
		b = Integer.valueOf(c[2]);
		if (c.length > 3) {
			try {
				this.sprite = ImageLoader.get("data/img/" + c[3]);
			} catch (Exception e) {
				System.out.println("Error at tile: " + "data/img/" + c[3]);
				e.printStackTrace();
			}
			this.imageInMenu = this.sprite; // sprite
			this.extra = Extra.Sprite;
			return;
		} else {
			if (name.equals("sky")) {
				this.extra = Extra.Transparent;
				this.imageInMenu = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB); // transparent
				return;
			} else {
				this.extra = Extra.None;
				try {
					this.imageInMenu = set.getSubimage(position, 0, Tile.imageSize, Tile.imageSize);
				} catch (Exception e) {
					this.imageInMenu = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB); // transparent
				}
			}
			if (position == (-1 * Tile.imageSize)) {
				this.extra = Extra.Color;
				this.imageInMenu = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB); // transparent
				this.imageInMenu.setRGB(0, 0, HEX.getHEX(255, r, g, b));
			}
		}
	}
	
	enum Extra {
		None, Transparent, Color, Sprite;
	}
}
