/*
 * 
 */

package com.numpad.kageditor;

// if options are set to skip intro, we use the intro 'nointro' as intro. Boom!
public class NoIntroState extends AppState {

	@Override public void init() {
		// not much to see here...
	}

	@Override public void display() {
		requestNext();
	}

}
