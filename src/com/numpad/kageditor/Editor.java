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

import de.numpad.framework.Frapp;
import de.numpad.framework.IO.ImageLoader;
import de.numpad.framework.gui.Display;
import de.numpad.framework.system.Client;
import de.numpad.framework.tool.Threaded;
import de.numpad.framework.tool.api.Plugin;

/*
 * main launcher for the editor, entry point
 * 
 * rendering etc is done in my framework 'Frapp'.
 * Frapp is also open source and can be viewed on my github profile.
 * 
 * Frapp is great for beginners and even advanced game / multimedia developers and supports the developer by taking away the dirty work of window, graphics etc
 */
public @Threaded("receiveUpdates") class Editor extends Frapp {
	private static final long serialVersionUID = 1L;
	
	public static AppStateCycle cycle;
	
	@Override public void start() {
		// create the strange, yet working appstatecycle
		AppState intro = (new Plugin("data/scr/menu.ps").value_boolean("showIntro", "true") ? new IntroState() : new NoIntroState());
		cycle = new AppStateCycle(intro, new MenuState(), new CreditsState(), new EditorState());
		
		Display.pausable(false); // disable pause on lost-focus
		Display.icon(ImageLoader.get("data/img/MenuItems.png", 32 * 3, 32 * 6, 32, 32)); // load icon from textureatlas
		
		// gotta show 'em what you got
		System.out.println("Your JARPATH is: " + Client.jarPath() + ".");
	}
	
	public void receiveUpdates() {
		// download update notes / scripts in another thread, so we dont need to wait until it finished to start the editor
		MenuState.buildUpdate();
		MenuState.buildScripts();
		
		try {
			Thread.sleep(20000); // buildUpdate and buildscripts are immediately returned if they are loaded once, yet we wait 20 seconds.
		} catch (Exception e) {  // this is pretty dumb actually. wanted to fix this looong ago
			
		}
	}
	
	// the logic entry
	@Override public void render() {
		cycle.handle();
	}
	
	// this is where everything begins
	public static void main(String[] args) {
		// std res : 1100|725
		Frapp.build(new Editor(), "King Arthur's Gold - Unofficial Map Editor", 1100, 725);
	}
}
