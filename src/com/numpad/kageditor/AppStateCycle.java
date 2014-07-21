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

import de.numpad.framework.Listening;
import de.numpad.framework.tool.Array;
import de.numpad.framework.tool.KeyTypeListener;


/*
 * this class is for handling all the menus etc so switching between these is somewhat
 * dynamic and not as hard as in a hardcoded way.
 */
public class AppStateCycle implements KeyTypeListener {
	public Array<AppState> states;
	private int current = 0;
	
	public AppStateCycle(AppState ...states) {
		this.states = new Array<AppState>();
		
		for (AppState a : states)
			this.states.add(a);
		
		Listening.add(this);
	}
	
	public void handle() {
		states.get(current).display();
		
		if (hasNext()) {
			if (states.get(current).wantsNext()) {
				next();
			}
			if (hasLast()) {
				if (states.get(current).wantsBack())
					last();
			}
		}
		if (states.get(current).requested() != -1) {
			int ns = states.get(current).requested();
			states.get(current).requestState(-1);
			set(ns);
		}
	}
	
	// change state to state #
	public void set(int state) {
		current = state;
		initCurrent();
	}
	
	// to the next state
	public void next() {
		if (hasNext()) {
			this.current += 1;
			initCurrent();
		}
	}
	
	// go back
	public void last() {
		if (hasLast()) {
			this.current -= 1;
			initCurrent();
		}
	}
	
	// initialize the current state.
	private void initCurrent() {
		states.get(current).initialize();
	}
	
	// if state[current++] is available
	public boolean hasNext() {
		try {
			return (states.get(current +1) != null);
		} catch (Exception e) {
			return false;
		}
	}
	
	// the hasNext() implementation is actually safer, this is pretty much dumb. but faster...
	/* How it should actually look: (change this if you feel like this. didnt test this; should work tho)
	 *	try {
	 *		return (states.get(current -1) != null);
	 *	} catch (Exception e) {
	 *		return false;
	 *	}
	 */
	public boolean hasLast() {
		return (current > 0);
	}
	
	// this is not an abstract void so you can decide if you need this in your state, i guess
	@Override public void keyTyped() {
		// pretty lonely in here, time for some
		/*
		 
_____/\\\\\\\\\________/\\\\\\\\\\\__________/\\\\\\\\\__/\\\\\\\\\\\__/\\\\\\\\\\\___________________/\\\\\\\\\_______/\\\\\\\\\______/\\\\\\\\\\\\\\\_____/\\\____        
 ___/\\\\\\\\\\\\\____/\\\/////////\\\_____/\\\////////__\/////\\\///__\/////\\\///__________________/\\\\\\\\\\\\\___/\\\///////\\\___\///////\\\/////____/\\\\\\\__       
  __/\\\/////////\\\__\//\\\______\///____/\\\/_______________\/\\\_________\/\\\____________________/\\\/////////\\\_\/\\\_____\/\\\_________\/\\\________/\\\\\\\\\_      
   _\/\\\_______\/\\\___\////\\\__________/\\\_________________\/\\\_________\/\\\______/\\\\\\\\\\\_\/\\\_______\/\\\_\/\\\\\\\\\\\/__________\/\\\_______\//\\\\\\\__     
    _\/\\\\\\\\\\\\\\\______\////\\\______\/\\\_________________\/\\\_________\/\\\_____\///////////__\/\\\\\\\\\\\\\\\_\/\\\//////\\\__________\/\\\________\//\\\\\___    
     _\/\\\/////////\\\_________\////\\\___\//\\\________________\/\\\_________\/\\\___________________\/\\\/////////\\\_\/\\\____\//\\\_________\/\\\_________\//\\\____   
      _\/\\\_______\/\\\__/\\\______\//\\\___\///\\\______________\/\\\_________\/\\\___________________\/\\\_______\/\\\_\/\\\_____\//\\\________\/\\\__________\///_____  
       _\/\\\_______\/\\\_\///\\\\\\\\\\\/______\////\\\\\\\\\__/\\\\\\\\\\\__/\\\\\\\\\\\_______________\/\\\_______\/\\\_\/\\\______\//\\\_______\/\\\___________/\\\____ 
        _\///________\///____\///////////___________\/////////__\///////////__\///////////________________\///________\///__\///________\///________\///___________\///_____
		 
		 */
	}
}
