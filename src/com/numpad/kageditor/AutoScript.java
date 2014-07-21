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

import java.io.File;

import de.numpad.framework.IO.FileReader;
import de.numpad.framework.math.Maths;
import de.numpad.framework.tool.api.Script;

/*
 * this is where the scripting magic happens.
 * dislaimer: scripts can do evil stuff, normally this shouldnt be a problem since they shouldnt
 * be able to steal your passwords and send them back to them. but use this with caution. dont 
 * download/execute any scripts you dont know. (You can download them and see their source at(windows): %appdata%/.frapstorage)
 */
public class AutoScript {
	private static Object es;
	private static EditorState ed;
	static {
		es = Editor.cycle.states.get(3); // editor state
		ed = (EditorState) es; // ...
	}
	// ---
	
	private Script script;
	private String name;
	private boolean useForeach = true;
	
	public String sourcecode;
	
	public AutoScript(File file) {
		script = new Script(Script.RHINO);
		
		String content = FileReader.external(file.getAbsolutePath());
		sourcecode = content;
		script.code(content);
		
		script.bind("Editor", es);
		script.bind("Maths", Maths.class);
		script.bind("self", this);
		
		script.require("ioread as FileReader");
		script.require("iowrite as FileWriter");
		script.require("input as Input");
		script.require("KEY as Key");
		script.require("sys as Client");
		
		this.name = script.svar("name");
		script.call("start");
	}
	
	public AutoScript(String name) {
		script = new Script(name, Script.RHINO);
		
		sourcecode = FileReader.string(name);
		
		script.bind("Editor", es);
		script.bind("Maths", Maths.class);
		script.bind("self", this);
		
		script.require("ioread as FileReader");
		script.require("iowrite as FileWriter");
		script.require("input as Input");
		script.require("KEY as Key");
		script.require("sys as Client");
		
		this.name = script.svar("name");
		script.call("start");
	}
	
	// call the function foreach(), will be slower than disabling it
	public void enableForeach() {
		this.useForeach = true;
	}
	
	// performance, hooraay
	public void disableForeach() {
		this.useForeach = false;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	// get name
	public String name() {
		return name;
	}
	
	// the full procedure, run the script and do foreach
	public void all() {
		execute();
		foreach();
	}
	
	public void execute() {
		script.call("execute");
	}
	
	// calls foreach() in the script for each block on the map. will be slow on medium-big sized maps
	public void foreach() {
		if (useForeach) { // just skip this when it's disabled 
			for (int x = 0; x < ed.blocks.length; x++) { // start left
				for (int y = 0; y < ed.blocks[1].length; y++) { // start top
					try { // function might not exist, ...
						script.call("foreach", x, y, ed.blocks[x][y].tile.name);
					} catch (Exception e) {
						e.printStackTrace(); // ... so heres the reason!
					}
				}
			}
		}
	}
}
