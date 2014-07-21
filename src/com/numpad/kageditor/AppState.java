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

/*
 * AppStates provide a somewhat simple interface to easialy use and switch
 * between different menus.
 * you can request the last state, next state etc.
 * adding your own state may create some confusion because the whole menu stuff
 * is hacked together in the appstatecycle. gl hf with that...
 */
public abstract class AppState {
	int req = -1;
	
	public AppState() {
		init();
	}
	
	private boolean back = false, next = false;
	
	public void initialize() {
		reset();
		init();
	}
	public abstract void init(); // load assets etc here.
	public abstract void display(); // do your logic right here
	
	public void requestBack() {
		back = true;
	}
	public void requestNext() {
		next = true;
	}
	
	public void requestState(int number) {
		this.req = number;
	}
	public int requested() {
		return req;
	}
	public boolean wantsBack() {
		return back;
	}
	public boolean wantsNext() {
		return next;
	}
	
	public void reset() {
		back = false;
		next = false;
	}
}
