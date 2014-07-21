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

import de.numpad.framework.IO.ImageLoader;
import de.numpad.framework.gui.Display;
import de.numpad.framework.input.Input;
import de.numpad.framework.input.Key;
import de.numpad.framework.math.Maths;
import de.numpad.framework.math.Timer;

// that awesome dancin' logo
public class IntroState extends AppState {
	private Timer untilNext;
	private BufferedImage image, wing;
	private float stretch = 4;
	private int w, h;
	
	
	/*
	 * You are allowed to remove / modify the clanlogo in any way, as long as you don't defy the license.
	 * legal hint: (this is a short form of the license, which is not 100% accurate and therefore doesnt overwrite the actual license) source: https://tldrlegal.com/license/mit-license
	 * 			YOU CAN               YOU CANNOT               YOU MUST
	 * 		- commercialize  - hold me liable for anything   - include copyright
	 *      - modify                                         - include the license
	 *      - distribute
	 *      - create sublicenses
	 *      - use it private
	 *      
	 */
	public IntroState() {
		super();
		image = ImageLoader.get("data/img/numpad.png");
		wing = ImageLoader.get("data/img/WiNG.png");
		w = image.getWidth();
		h = image.getHeight();
	}
	
	@Override public void init() {
		untilNext = new Timer(400){
			@Override public void onFinish() {
				requestNext();
			}
		}.start();
		
	}
	
	@Override public void display() {
		if (Input.down(Key.SPACE))
			requestNext();
		
		Display.group = 9;
		if (untilNext.time() < 255) {
			Display.background(44, 175, 222);
			Display.image(image, Display.original_width() /2 -(w * stretch) /2,
					Display.original_height() /2 - (h * stretch) /2,
					stretch);
			stretch += Maths.sin(untilNext.time() /10f) * 0.2f; // boing boing
			untilNext.tick();
			
			Display.color(250, 212, 40, (untilNext.time() > 127) ? 2*(untilNext.time() - 128) : 0);
			Display.fill();
			Display.rect(0, 0, Display.original_width(), Display.original_height());
		} else { // here, have a kitten: https://i.imgur.com/k1N13cY.gif  <3
			Display.background(250, 212, 40);
			Display.image(wing, Display.original_width() /2 - (wing.getWidth() /2), Display.original_height() /2 - wing.getHeight() /2);
			untilNext.tick();
		}
	}

}
