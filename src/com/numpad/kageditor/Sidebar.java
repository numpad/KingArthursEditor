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

import com.numpad.kageditor.Tile.Extra;

import de.numpad.framework.IO.ImageLoader;
import de.numpad.framework.gui.Display;
import de.numpad.framework.gui.Fonts;
import de.numpad.framework.input.Input;
import de.numpad.framework.input.Key;
import de.numpad.framework.math.Juice;
import de.numpad.framework.math.Maths;

// the awesome thing on the left
public class Sidebar {
	
	public int x, width;
	private BufferedImage image, arrowright, arrowleft, arrowup, arrowdown;
	private float scroll = 0;
	private int currentIndex = 0;
	public Sidebar() {
		this.x = 0;
		this.width = 230;
		image = ImageLoader.get("data/img/area.png");	// get stuff from textureatlas
		arrowright = ImageLoader.get("data/img/MenuItems.png", 2 * 32, 7 * 32, 32, 32);
		arrowleft = ImageLoader.get("data/img/MenuItems.png", 3 * 32, 7 * 32, 32, 32);
		arrowup = ImageLoader.get("data/img/MenuItems.png", 2 * 32, 1 * 32, 32, 32);
		arrowdown = ImageLoader.get("data/img/MenuItems.png", 1 * 32, 1 * 32, 32, 32);
		
	}
	
	private float wobble = 0;
	private boolean hide = false;
	private float scrollSpeed = 0.003f;
	
	// render everything
	public void display() {
		Display.patch9(image, x, 0, width, Display.original_height(), 12, 12); // patch9 ftw
		float xoff = Maths.sin(wobble) * 4;
		if (Input.mouse.x > x + width && Input.mouse.x < x + width + 64 && Input.mouse.y > 0 && Input.mouse.y < 64) {
			wobble += 0.1f;
		}
		if (x >= -10)
			Display.image(arrowleft, x + width + xoff, 0, 2);
		else {
			Display.image(arrowright, x + width + xoff, 0, 2);
		}
		
		if (Input.clicked) {
			int xp = (int)Input.mouse.x;
			int yp = (int)Input.mouse.y;
			if (yp >= 0 && yp < 64) {
				if (xp >= this.x + this.width && xp <= this.x + this.width + 64) {
					if (this.x == 0) { // push out
						//this.x = -width;
						hide = true;
					} else {
						//this.x = 0;
						hide = false;
					}
				}
			}
		}
		
		if (hide) {
			this.x += Juice.ease2d(this.x, -width -14, 0.05f);
		} else {
			this.x += Juice.ease2d(this.x, 10, 0.05f);
		}
		
		int x = 30 + this.x;
		int ys = 50;
		int ye = 30;
		
		if (Input.mouse.x < this.x + this.width && Input.scrolling) {
			scroll += Input.wheelRotation /100f;
			
			if (scroll < 0)
				scroll = 0;
			else if (scroll > 1)
				scroll = 1;
		}
		
		if (x > -this.width + 40) { // only if the sidebar is visible
			if (Input.clicked) {
				int xp = (int)Input.mouse.x;
				int yp = (int)Input.mouse.y;
				
				if (canCalculateIndex(yp, ys, ye, xp)) {
					try {
						yp = calculateIndex(yp);
						this.currentIndex = yp;
						
						EditorState.blockSelection.setText(Tile.tiles[yp].name);
						EditorState.blockSelection.onInput();
					} catch (Exception e) { }
				}
			}
			
			if (Input.mouseDown(Key.MOUSE_LEFT)) {
				int xp = (int)Input.mouse.x;
				int yp = (int)Input.mouse.y;
				if (xp < this.x + this.width) {
					if (yp < 64)
						scroll -= scrollSpeed;
					else if (yp > Display.original_height() - 142 && yp < Display.original_height() - 142 + 64)
						scroll += scrollSpeed;
				}
				if (scroll < 0)
					scroll = 0;
				else if (scroll > 1)
					scroll = 1;
			}
			
			Display.font(Fonts.thin_light, 1);
			for (int i = 0; i < Tile.tiles.length; i++) {
				float y = ys + (i * 30) - scroll * ((Tile.tiles.length -18) * ye);
				
				if (y < ys - 32) // y < ys - 40 if smooth outscroll shall work
					continue;
				if (y >= ys + 18 * ye)
					break; // break, because everything under it will definetly be invisible if the thing above is invisible
				
				
				if (i != this.currentIndex)
					Display.color(80, 93, 76);
				else
					Display.color(120, 93*2 - (93/2), 76*2-76/2);
				
				Display.fill();
				Display.rect(x - 21, y, 210, 25);
				Display.outline();
				
				/* 'Smooth' outscroll*/
				Display.color(100, 113, 96);
				Display.fill();
				Display.rect(this.x + 5, 0 + 5, this.width - 10, 44);
				
				// scroll += 0.00001f;
				
				Display.write(" " + Tile.tiles[i].name, x, y + 6);
				if (Tile.tiles[i].extra == Extra.None) {
					Display.image(Tile.set, x - Tile.imageSize *2, y + Tile.imageSize /2, x, y + Tile.imageSize *2 + Tile.imageSize /2, Tile.tiles[i].position, 0, Tile.imageSize);
				} else if (Tile.tiles[i].extra == Extra.Sprite) {
					Display.image(Tile.tiles[i].sprite, x - Tile.imageSize *2, y + Tile.imageSize /2, x, y + Tile.imageSize *2 + Tile.imageSize /2);
				} else if (Tile.tiles[i].extra == Extra.Color) {
					Display.color(Tile.tiles[i].r, Tile.tiles[i].g, Tile.tiles[i].b);
					Display.fill();
					Display.rect(x - Tile.imageSize *2, y + Tile.imageSize /2, Tile.imageSize *2, Tile.imageSize *2 + Tile.imageSize /2 -2);
				}
				
				// Smooth outscroll
				Display.color(100, 113, 96);
				Display.fill();
				Display.rect(this.x + 5, Display.original_height() - 142 +5, this.width -10, 44);
			}
			
			Display.image(arrowup, this.x + width /2 - 32, -6, 2);
			Display.image(arrowdown, this.x +  width /2 - 32, Display.original_height() - 142, 2);
		}
	}
	
	// calcutale the index of the array based on the scroll and mouse position
	private int calculateIndex(int y) {
		int ys = 50;
		int ye = 30;
		int n = ye;
		y -= ys;
		y += scroll * ((Tile.tiles.length -18) * n);
		y /= n;
		return y;
	}
	private boolean canCalculateIndex(int yp, int ys, int ye, int xp) {
		return (yp >= ys && yp < ys + 18 * ye && xp > this.x && xp < this.width);
	}
}
