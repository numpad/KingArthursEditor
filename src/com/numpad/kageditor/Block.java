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

import com.numpad.kageditor.Tile.Extra;

import de.numpad.framework.gui.Display;


/*
 * handles rendering, placement and logic of blocks.
 */
public class Block {
	public static float xOffset = 0, yOffset = 0;
	public static float size = 32;
	public static boolean forceColor = false;
	public Tile tile;
	private int id = 0;
	private boolean marked = false;
	
	
	public Block(Tile tile) {
		this.tile = tile;
		this.id = 0;
	}
	public Block(Tile tile, int id) { // id's are to give different sprites to a block, imagine different grass types
		this.tile = tile;
		this.id = id;
	}
	
	// render a block, beware of ugly math!
	public void render(int xt, int yt) {
		if (this.tile.extra != Extra.Transparent) {
			int rx = (int)(xt * size + xOffset);
			int ry = (int)(yt * size + yOffset);
			if (rx > -size && rx < Display.original_width() + size && ry > -size && ry < Display.original_height() + size) {
				if (this.tile.extra == Extra.Color || size <= 4 || forceColor) {
					Display.color(tile.r, tile.g, tile.b);
					Display.fill();
					Display.rect(xt * size + xOffset, yt * size + yOffset, size, size);
				} else if (this.tile.extra == Extra.None) {
					Display.image(Tile.set, xt * size + xOffset, yt * size + yOffset, xt * size + size + xOffset, yt * size + size + yOffset, tile.position, this.id * Tile.imageSize, Tile.imageSize);
				} else if (this.tile.extra == Extra.Sprite) {
					float stretch = (size /Tile.imageSize);
					// Display.image(this.tile.sprite, xt * size + (size /2 + xOffset - (tile.sprite.getWidth() * stretch / 2)), yt * size + (size /2 + yOffset - (tile.sprite.getHeight() * stretch /2)), stretch);
					Display.image(this.tile.sprite, xt * size + (size /2 + xOffset - (tile.sprite.getWidth() * stretch / 2)), yt * size + (size /2 + yOffset - (tile.sprite.getHeight() * stretch /2)), stretch);
					
				}
			}
		}
		if (isMarked()) { // that little outline-thingy
			unmark();
			Display.color(255, 200);
			Display.stroke(3);
			Display.rect(xt * size + xOffset, yt * size + yOffset, size);
		}
	}
	
	// when a block is placed, call this so water, grass, dirt etc check if a specific block surrounds them.
	/* example: G = grass, D = dirt w/o green, P = dirt with green on top
	 * 
	 *     - place grass ->  GGG - placed() -> GGG
	 * DDD                   DDD               PPP
	 * 
	 */
	public void placed(int x, int y, Block[][] blocks) {
		if (this.tile.name.contains("water")) {
			try { // render water based on depth
				if (blocks[x][y -1].tile.name.contains("water")) {
					blocks[x][y].setId(1);
					if (blocks[x][y -2].tile.name.contains("water")) {
						blocks[x][y].setId(2);
					}
				}
			} catch (Exception e) {}
		} else if (this.tile.name.equals("grass")) {
			this.setId(1);
			try {
				if (blocks[x][y+1].tile.name.equals("dirt")) {
					blocks[x][y+1].setId(2);
				}
			} catch (Exception e) { }
		} else if (this.tile.name.equals("dirt")) {
			try {
				if (blocks[x][y-1].tile.name.equals("grass")) {
					setId(2);
				}
			} catch (Exception e) { }
		}
	}
	
	/*
	 * block at (x|y) placed, neighbors around (x|y) get notificated and 
	 * updated so not every block needs to be updated over and over again. very performance, much efficient.
	*/
	public void notificated(int x, int y, Block[][] blocks) {
		if (this.tile.name.contains("water")) {
			try { // render water based on depth
				if (blocks[x][y -1].tile.name.contains("water")) {
					blocks[x][y].setId(1);
					if (blocks[x][y -2].tile.name.contains("water")) {
						blocks[x][y].setId(2);
					}
				}
			} catch (Exception e) {}
		} else if (this.tile.name.equals("dirt")) {
			try {
				if (!blocks[x][y-1].tile.name.equals("grass")) {
					setId(0);
				}
			} catch (Exception e) { }
		}
	}
	
	// change minor look
	public void setId(int id) {
		this.id = id;
	}
	
	// set outline -> true
	public void mark() {
		this.marked = true;
	}
	// remove outline
	public void unmark() {
		this.marked = false;
	}
	
	// has outline
	public boolean isMarked() {
		return marked;
	}
}
