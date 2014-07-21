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

import de.numpad.framework.Frapp;
import de.numpad.framework.IO.ImageLoader;
import de.numpad.framework.gui.Display;
import de.numpad.framework.gui.Fonts;
import de.numpad.framework.gui.elements.Button;
import de.numpad.framework.system.Client;
import de.numpad.framework.system.Storage;
import de.numpad.framework.tool.Array;
import de.numpad.framework.tool.api.Pastebin;
import de.numpad.framework.tool.api.Plugin;
import de.numpad.framework.tool.api.firebase.Firebase;

public class MenuState extends AppState {
	
	private String open, run, credits, quit;
	public static BufferedImage background, title;
	public static int halfTitleWidth = 0;
	
	public static Firebase firebase;
	
	private static Array<String> formatted = new Array<String>();
	private static Array<String> fScripts = new Array<String>();
	
	
	public MenuState() {
		super();
		
		firebase = new Firebase("mapeditor"); // connect to firebase.io to create host news etc...
											  // after open-source update: read allowed, write disabled -> no script upload anymore
											  // thanks for your support, HUGIO88 <3
		
		Plugin menu = new Plugin("data/scr/menu.ps");
		title = ImageLoader.get(menu.value("title", "data/img/title.png"));
		open = menu.value("open", "Open Editor");
		run = menu.value("run", "Run KAG");
		credits = menu.value("credits", "Credits");
		quit = menu.value("quit", "Exit");
		
		background = ImageLoader.get("data/img/TitleBackground.png");
		halfTitleWidth = title.getWidth() /2;
		final int gameID = menu.value_int("kag_steam_gameid", "219830"); // run the game via url trough steam
		final int yoff = 230;	// position of stuff
		final int height = 50;
		final int vdist = 40;
		final int xoff = 400;
		final int width = menu.value_int("width");
		
		/*
		 * Strange stuff like placement of gui like buttons
		 */
		
		Button open = new Button(0, yoff, width, height, this.open, 2){
			@Override public void onCreate() {
				this.group(0);
				this.font = Fonts.thin_light;
				this.texture = ImageLoader.get("data/img/button.png");
				this.x = (int)(Display.original_width() /2 - this.getWidth() /2) + xoff;
			}
			@Override public void onRelease() {
				requestState(3);
			}
		};
		Editor.GUI.add(open);
		Button run = new Button(0, yoff + (height + vdist) *1, width, height, this.run, 2){
			@Override public void onCreate() {
				this.group(0);
				this.font = Fonts.thin_light;
				this.texture = ImageLoader.get("data/img/button.png");
				this.x = (int)(Display.original_width() /2 - this.getWidth() /2) + xoff;
			}
			@Override public void onRelease() {
				Client.navigate("steam://rungameid/" + gameID);
				Client.EXIT();
			}
		};
		Editor.GUI.add(run);
		Button credits = new Button(0, yoff + (height + vdist) *2, width, height, this.credits, 2){
			@Override public void onCreate() {
				this.group(0);
				this.font = Fonts.thin_light;
				this.texture = ImageLoader.get("data/img/button.png");
				this.x = (int)(Display.original_width() /2 - this.getWidth() /2) + xoff;
			}
			@Override public void onRelease() {
				requestNext();
			}
		};
		Editor.GUI.add(credits);
		Button quit = new Button(0, yoff + (height + vdist *2) *3, width, height, this.quit, 2){
			@Override public void onCreate() {
				this.group(0);
				this.font = Fonts.thin_light;
				this.texture = ImageLoader.get("data/img/button.png");
				this.x = (int)(Display.original_width() /2 - this.getWidth() /2) + xoff;
			}
			@Override public void onRelease() {
				Client.EXIT();
			}
		};
		Editor.GUI.add(quit);
	}
	
	public static final void buildUpdate() {
		if (!formatted.isEmpty()) return;
		
		String update = firebase.value("Newsfeed"); // load newsfeed and format it
		// "DATE":"NEWS","DATE":"NEWS"
		update = update.substring(1, update.length() -1);
		String[] packs = update.split("[,]");
		for (int i = 0; i < packs.length; i++) { // Build and Format newsfeed
			String DATE = packs[i].split("[:]")[0].replaceAll("[\"]", "");
			String day = DATE.substring(0, 2);
			String month = DATE.substring(2, 4);
			String year = DATE.substring(4, 6);
			DATE = day + "." + month + ".20" + year;
			String CONTENT = packs[i].split("[:]")[1].replaceAll("[\"]", "");
			formatted.add(DATE + ": " + CONTENT);
		}
	}
	public static final void buildScripts() {
		if (!fScripts.isEmpty()) return;
		
		String update = firebase.value("NewScripts");
		// "DATE":"NEWS","DATE":"NEWS"
		update = update.substring(1, update.length() -1);
		String[] packs = update.split("[,]");
		for (int i = 0; i < packs.length; i++) { // Build and Format newsfeed
			final String NAME = packs[i].split("[:]")[0].replaceAll("[\"]", "");
			final String CONTENT = packs[i].split("[:]")[1].replaceAll("[\"]", "");
			fScripts.add(NAME + ": " + CONTENT);
			
			Frapp.GUI.add(new Button(510, 370 + (i * 34), 245, 7, CONTENT, 1) {
				public void onCreate() {
					this.group(0);
					this.font = Fonts.thin_light;
					this.texture = ImageLoader.get("data/img/button.png");
				}
				public void onRelease() {
					final String script = Pastebin.raw(NAME);
					Storage.save(NAME, script);
				}
			});
			
		}
		
	}
	
	@Override public void init() {
		Display.group = 0;
		fScripts.clear();
	}
	
	
	private boolean displayWindow = true;
	@Override public void display() {
		Display.background(background);
		Display.image(title, Display.original_width() /2 - halfTitleWidth *1.5f, 20, 1.5f);
		
		if (displayWindow) {
			window(40, 320, 440, 377, "Newsfeed");
			window(500, 320, 300, 377, "New Scripts");
			
			for (int i = 0; i < formatted.size(); i++) {
				element(formatted.get(i), i, 54, 410);
			}
		}
		
		
		Display.GUI(0);
	}
	
	// render a window
	private void window(int x, int y, int width, int height, String title) {
		Display.color(100, 113, 96);
		Display.fill();
		Display.rect(x, y, width, height);
		Display.outline();
		Display.line(x, y + 34, x + width, y + 34);
		Display.font(Fonts.thin_light, 2);
		Display.text(title);
		Display.write(x + width /2 - Display.textWidth() /2, y + 5);
	}
	
	// render an element
	private void element(String text, int i, int x, int w) {
		Display.font(Fonts.thin_dark, 1);
		Display.text(text);
		int y = (int)(380 + i * (Display.textHeight() + 24));
		Display.color(80, 93, 76);
		Display.fill();
		Display.rect(x, y -10, w, Display.textHeight() + 20);
		Display.outline();
		Display.write(x, y);
	}

}
