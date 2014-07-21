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

import de.numpad.framework.IO.FileReader;
import de.numpad.framework.IO.ImageLoader;
import de.numpad.framework.gui.Display;
import de.numpad.framework.gui.Fonts;
import de.numpad.framework.gui.elements.Button;
import de.numpad.framework.system.Client;
import de.numpad.framework.tool.Array;

/* as the license explains, you need to give me credit.
 * the easiest way is just to include my name, christian schäl, and my username, 'numpad'
 * however, if you want additional stuff, like images, websites etc there you go:
 * - WEBSITE	:	numpad.github.io
 * - IMAGES		:	https://imgur.com/a/xbIRj
 * - NAME		:	Christian Schäl
 * - USERNAME	:	numpad, numpad0to9
*/
public class CreditsState extends AppState {
	
	private Array<String> credits;
	private int yoff = 20, xoff = 20;
	
	public CreditsState() {
		credits = FileReader.array("data/scr/credits.txt");
		
		Button bt = new Button((int)Display.original_width() - 155, (int)Display.original_height() - 80, 100, 40, "", 0){
			BufferedImage img = ImageLoader.get("data/img/MenuItems.png", 2 * 32, 0, 32, 32);
			@Override public void onCreate() {
				this.group(9);
				this.texture = ImageLoader.get("data/img/button.png");
			}
			@Override public void onRender() {
				Display.image(img, x + width /2 - 16, y, 2);
			}
			@Override public void onRelease() {
				requestBack();
			}
		};
		Editor.GUI.add(bt);
		Button info = new Button(40, (int)Display.original_height() - 80, 840, 40, "Visit thread", 1){
			@Override public void onCreate() {
				this.group(9);
				this.font = Fonts.thin_light;
				this.texture = ImageLoader.get("data/img/button.png");
			}
			@Override public void onRelease() {
				Client.navigate("https://forum.kag2d.com/resources/map-editor.173/");
			}
		};
		Editor.GUI.add(info);
		
	}
	
	@Override public void init() {
		
	}

	@Override public void display() {
		Display.background(MenuState.background);
		Display.background(0, 140);
		//Display.image(Menu.title, Display.original_width() /2 - Menu.halfTitleWidth *1.5f, 20, 1.5f);
		
		Display.font(Fonts.thin_light, 4);
		for (int i = 0; i < credits.size(); i++) {
			Display.write(credits.get(i), xoff, yoff + 60 * i);
		}
		Display.GUI(9);
	}

}
