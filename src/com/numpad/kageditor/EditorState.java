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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;

import com.numpad.kageditor.Tile.Extra;

import de.numpad.framework.Frapp;
import de.numpad.framework.IO.ImageLoader;
import de.numpad.framework.gui.Display;
import de.numpad.framework.gui.Fonts;
import de.numpad.framework.gui.elements.Button;
import de.numpad.framework.gui.elements.Scroll;
import de.numpad.framework.gui.elements.Textbox;
import de.numpad.framework.input.Input;
import de.numpad.framework.input.Key;
import de.numpad.framework.math.Coordinate;
import de.numpad.framework.math.HEX;
import de.numpad.framework.math.Maths;
import de.numpad.framework.math.Timer;
import de.numpad.framework.math.Vector;
import de.numpad.framework.system.Client;
import de.numpad.framework.system.Storage;
import de.numpad.framework.tool.Array;
import de.numpad.framework.tool.api.Pastebin;
import de.numpad.framework.tool.api.Plugin;

/*
 * The full editor, contains all logic you see when editing maps
 */
public class EditorState extends AppState {
	
	// Level size
	private boolean levelSizeSet = false;
	
	// Editor
	private BufferedImage background, menuBackground, problem, exported;
	private float scrollSpeed, scrollSpeedMult, zoomSpeed = 0.5f;
	private boolean selectBlock = false;
	private boolean exitDialog = false;
	public Block[][] blocks;
	private Scroll blockScroll;
	private Sidebar sidebar;
	
	// Options
	private boolean showProblemImage = false, showExportImage = false;
	private Timer showProblemTimer, showExportTimer;
	
	private boolean optionsDisplayed = false;
	private String selection = "Dirt";
	private boolean renderOutline = true;
	private int maxPenSize = 4;
	private int penSize = 0;
	private Array<AutoScript> scripts = new Array<AutoScript>();
	private int currentScript = -1;
	public static Symmetry symmetry = Symmetry.None;
	public static Textbox blockSelection;
	private Textbox ownSize;
	
	enum Symmetry {
		None, Normal, Team;
	}
	
	public EditorState() {
		// random background
		String[] bgs = new String[] {"Castle", "Island", "Plains", "Trees"};
		background = ImageLoader.get("data/img/Background" + bgs[Maths.random(bgs.length -1)] + ".png");
		menuBackground = ImageLoader.get("data/img/menuBackground.png");
		problem = ImageLoader.get("data/img/Problem.png");
		exported = ImageLoader.get("data/img/Exported.png");
		
		showProblemTimer = new Timer(1000) {
			@Override public void onFinish() {
				showProblemImage = false;
			}
		}.enableLoop();
		
		showExportTimer = new Timer(1000) {
			@Override public void onFinish() {
				showExportImage = false;
			}
		}.enableLoop();
		
		Plugin plugin = new Plugin("data/scr/menu.ps");
		scrollSpeed = plugin.value_float("scrollSpeed", "7.0");
		scrollSpeedMult = plugin.value_float("scrollSpeedMult", "3.0");
		
		/* * * * * * * * * * * * * * *\
		 * Huge Wall-of-GUI incoming *
		\* * * * * * * * * * * * * * */
		
		sidebar = new Sidebar();
		Button menu = new Button(10, (int)Display.original_height() - 80, 38, 45, "", 0) {
			BufferedImage img = ImageLoader.get("data/img/MenuItems.png", 2 * 32, 6 * 32, 32, 32);
			@Override public void onCreate() {
				group(2);
				this.font = Fonts.thin_light;
				this.texture = ImageLoader.get("data/img/button.png");
			}
			@Override public void onRender() {
				Display.image(img, x-11, y-14, 3);
			}
			@Override public void onRelease() {
				optionsDisplayed = true;
			}
		};
		Frapp.GUI.add(menu);
		
		Button incrPensize = new Button(90, (int)Display.original_height() - 80, 12, 10, "+", 2) {
			@Override public void onCreate() {
				group(2);
				this.font = Fonts.thin_light;
				this.texture = ImageLoader.get("data/img/button.png");
			}
			@Override public void onRelease() {
				penSize++;
				if (penSize > maxPenSize)
					penSize = maxPenSize;
			}
		};
		Frapp.GUI.add(incrPensize);
		
		Button decrPensize = new Button(90, (int)Display.original_height() - 45, 12, 10, "-", 2) {
			@Override public void onCreate() {
				group(2);
				this.font = Fonts.thin_light;
				this.texture = ImageLoader.get("data/img/button.png");
			}
			@Override public void onRelease() {
				penSize--;
				if (penSize < 0)
					penSize = 0;
			}
		};
		Frapp.GUI.add(decrPensize);
		
		Button blockSelect = new Button(144, (int)Display.original_height() - 80, 38, 45, "", 0) {
			BufferedImage img = ImageLoader.get("data/img/MenuItems.png", 0 * 32, 2 * 32, 32, 32);
			@Override public void onCreate() {
				group(2);
				this.font = Fonts.thin_light;
				this.texture = ImageLoader.get("data/img/button.png");
			}
			@Override public void onRender() {
				Display.image(img, x+4, y-2, 2);
			}
			@Override public void onRelease() {
				selectBlock = true;
			}
		};
		Frapp.GUI.add(blockSelect);
		
		Button area = new Button(Display.width /2 - 200, 190, 365, 260, "", 1) {
			@Override public void onCreate() {
				group(3);
				this.font = Fonts.thin_light;
				this.texture = ImageLoader.get("data/img/button.png");
			}
		};
		Frapp.GUI.add(area);
		
		final Textbox size = new Textbox(Display.width /2 - 180, 300, 355, 60) {
			@Override public void onCreate() {
				group(4);
				this.maxLength = 12;
				setText("200, 64");
			}
			@Override public void onClick() {
				this.setText("");
			}
			@Override public void onAction() {
				try {
					this.text = this.text.replace(" ", "");
					String[] wh = this.text.split(",");
					int width = Integer.valueOf(wh[0]);
					int height = Integer.valueOf(wh[1]);
					blocks = createSkymap(width, height);
					
					loadScripts();
				} catch (Exception e) {
					this.setText("width,height");
				}
			}
		};
		Frapp.GUI.add(size);
		
		Button ok = new Button(595, 400, 100, 40, "Okay", 2) {
			@Override public void onCreate() {
				group(4);
				this.font = Fonts.thin_light;
				this.texture = ImageLoader.get("data/img/button.png");
			}
			@Override public void onRelease() {
				size.onAction();
			}
		};
		Frapp.GUI.add(ok);
		
		Button back = new Button(368, 400, 100, 40, "", 2) {
			BufferedImage img = ImageLoader.get("data/img/MenuItems.png", 2 * 32, 0, 32, 32);
			@Override public void onCreate() {
				group(4);
				this.texture = ImageLoader.get("data/img/button.png");
			}
			@Override public void onRender() {
				Display.image(img, x + width /2 - 16, y, 2);
			}
			@Override public void onRelease() {
				requestState(1);
			}
		};
		Frapp.GUI.add(back);
		
		Button backOptions = new Button(160, 520, 100, 40, "", 2) {
			BufferedImage img = ImageLoader.get("data/img/MenuItems.png", 2 * 32, 0, 32, 32);
			@Override public void onCreate() {
				group(6);
				this.texture = ImageLoader.get("data/img/button.png");
			}
			@Override public void onRender() {
				Display.image(img, x + width /2 - 16, y, 2);
			}
			@Override public void onRelease() {
				optionsDisplayed = false;
			}
		};
		Frapp.GUI.add(backOptions);
		
		blockSelection = new Textbox(303, 522, 636, 60) {
			@Override public void onCreate()  {
				group(11);
				setText("Dirt");
			}
			@Override public void onClick() {
				setText("");
			}
			
			@Override public void onInput() {
				selection = this.getText();
			}
			@Override public void onAction() {
				this.setActive(false);
				selectBlock = false;
			}
		};
		Frapp.GUI.add(blockSelection);
		
		Button markBorderButton = new Button(510, 520, 60, 40, "", 2) {
			BufferedImage activate = ImageLoader.get("data/img/MenuItems.png", 0 * 32, 7 * 32, 32, 32);
			BufferedImage deactivate = ImageLoader.get("data/img/MenuItems.png", 1 * 32, 7 * 32, 32, 32);
			
			@Override public void onCreate() {
				group(6);
				this.texture = ImageLoader.get("data/img/button.png");
			}
			@Override public void onRender() {
				if (renderOutline) {
					Display.image(activate, x + width /2 - 16, y, 2);
				} else {
					Display.image(deactivate, x + width /2 - 16, y, 2);
				}
				
				Display.font(Fonts.thin_light, 3);
				Display.write("Outline", x + 110, y + 15);
			}
			@Override public void onRelease() {
				if (renderOutline) {
					renderOutline = false;
				} else {
					renderOutline = true;
				}
			}
		};
		Frapp.GUI.add(markBorderButton);
		
		Button forceColorButton = new Button(510, 370, 60, 40, "", 2) {
			BufferedImage activate = ImageLoader.get("data/img/MenuItems.png", 0 * 32, 7 * 32, 32, 32);
			BufferedImage deactivate = ImageLoader.get("data/img/MenuItems.png", 1 * 32, 7 * 32, 32, 32);
			
			@Override public void onCreate() {
				group(6);
				this.texture = ImageLoader.get("data/img/button.png");
			}
			@Override public void onRender() {
				if (Block.forceColor) {
					Display.image(activate, x + width /2 - 16, y, 2);
				} else {
					Display.image(deactivate, x + width /2 - 16, y, 2);
				}
				
				Display.font(Fonts.thin_light, 3);
				Display.write("draw images", x + 110, y + 15);
			}
			@Override public void onRelease() {
				if (Block.forceColor) {
					Block.forceColor = false;
				} else {
					Block.forceColor = true;
				}
			}
		};
		Frapp.GUI.add(forceColorButton);
		
		ownSize = new Textbox(510, 440, 95, 70) {
			public void onCreate() {
				group(6);
				maxLength = 2;
				fontScale = 4;
				numbersOnly = true;
				this.setText("" + 4);
				this.renderCenter = true;
			}
			public void onInput() {
				maxPenSize = Integer.valueOf(this.text);
			}
			public void onRender() {
				renderText();
				Display.font(Fonts.thin_light, 3);
				Display.write("Max Pensize", this.x + this.width + 15, this.y +14);
			}
		};
		Frapp.GUI.add(ownSize);
		
		
		final Button scriptButton = new Button(160, 300, 100, 40, "[Script]", 1) {
			@Override public void onCreate() {
				group(6);
				this.texture = ImageLoader.get("data/img/button.png");
			}
			@Override public void onRelease() {
				currentScript++;
				if (currentScript >= scripts.size())
					currentScript = 0;
				this.setText(scripts.get(currentScript).name());
			}
		};
		Frapp.GUI.add(scriptButton);
		Button execButton = new Button(320, 300, 250, 40, "Execute Script", 2) {
			@Override public void onCreate() {
				group(6);
				this.font = Fonts.thin_light;
				this.texture = ImageLoader.get("data/img/button.png");
			}
			@Override public void onRelease() {
				scripts.get(currentScript).execute();
				scripts.get(currentScript).foreach();
				scriptButton.setText(scripts.get(currentScript).name());
			}
		};
		Frapp.GUI.add(execButton);
		Button uploadButton = new Button(620, 300, 100, 40, "Upload", 2) {
			@Override public void onCreate() {
				group(6);
				this.font = Fonts.thin_light;
				this.texture = ImageLoader.get("data/img/button.png");
			}
			@Override public void onRelease() {
				String src = scripts.get(currentScript).sourcecode;
				String rsp = Pastebin.paste("e897fd0e7f2f441bb88607c75e03108b", "KAG Script", src);
				rsp = rsp.replaceAll("\\w+[:][/][/]\\w+[.]\\w+[/]", "");
				
				MenuState.firebase.add("NewScripts", rsp, scripts.get(currentScript).name());
			}
		};
		Frapp.GUI.add(uploadButton);
		
		Button exportOptions = new Button(160, 450, 100, 40, "Export", 2) {
			@Override public void onCreate() {
				group(6);
				this.texture = ImageLoader.get("data/img/button.png");
				this.font = Fonts.thin_light;
			}
			@Override public void onRelease() {
				BufferedImage e = new BufferedImage(blocks.length, blocks[1].length, BufferedImage.TYPE_INT_RGB);
				for (int x = 0; x < blocks.length; x++) {
					for (int y = 0; y < blocks[1].length; y++) {
						e.setRGB(x, y, HEX.getHEX(255, blocks[x][y].tile.r, blocks[x][y].tile.g, blocks[x][y].tile.b));
					}
				}
				
				//ImageLoader.save(e, "C:/Users/" + Client.username() + "/Desktop/export.png");
				//ImageLoader.save(e, "C:/Program Files (x86)/Steam/SteamApps/common/King Arthur's Gold/Base/Maps/sandbox.png");
				copyToClipboard(e);
				try {
					ImageLoader.save(e, "" + Client.jarPath() + "export.png"); // TODO Fix this! Might be fixed now, if it doesnt work, image goes to clipboard
					doneExporting();
				} catch (Exception x) {
					System.err.println("Error:\n" + x.toString());
					failedExporting();
				}
			}
		};
		Frapp.GUI.add(exportOptions);
		
		Button importOptions = new Button(160, 380, 100, 40, "Import", 2) {
			@Override public void onCreate() {
				group(6);
				this.texture = ImageLoader.get("data/img/button.png");
				this.font = Fonts.thin_light;
			}
			@Override public void onRelease() {
				BufferedImage e = ImageLoader.external(Client.jarPath() + "import.png");
				blocks = createSkymap(e.getWidth(), e.getHeight());
				for (int x = 0; x < blocks.length; x++) {
					for (int y = 0; y < blocks[1].length; y++) {
						blocks[x][y] = new Block(Tile.getTileByColor(HEX.getR(e.getRGB(x, y)), HEX.getG(e.getRGB(x, y)), HEX.getB(e.getRGB(x, y))));
					}
				}
			}
		};
		Frapp.GUI.add(importOptions);
		
		Button returnok = new Button(595, 400, 100, 40, "Okay", 2) {
			@Override public void onCreate() {
				group(10);
				this.font = Fonts.thin_light;
				this.texture = ImageLoader.get("data/img/button.png");
			}
			@Override public void onRelease() {
				requestState(1);
			}
		};
		Frapp.GUI.add(returnok);
		
		Button returnback = new Button(368, 400, 100, 40, "", 2) {
			BufferedImage img = ImageLoader.get("data/img/MenuItems.png", 2 * 32, 0, 32, 32);
			@Override public void onCreate() {
				group(10);
				this.texture = ImageLoader.get("data/img/button.png");
			}
			@Override public void onRender() {
				Display.image(img, x + width /2 - 16, y, 2);
			}
			@Override public void onRelease() {
				exitDialog = false;
			}
		};
		Frapp.GUI.add(returnback);
		
		Button backBlockSelect = new Button(160, 520, 100, 40, "", 2) {
			BufferedImage img = ImageLoader.get("data/img/MenuItems.png", 2 * 32, 0, 32, 32);
			@Override public void onCreate() {
				group(11);
				this.texture = ImageLoader.get("data/img/button.png");
			}
			@Override public void onRender() {
				Display.image(img, x + width /2 - 16, y, 2);
			}
			@Override public void onRelease() {
				selectBlock = false;
			}
		};
		Frapp.GUI.add(backBlockSelect);
		
		blockScroll = new Scroll((int)(Display.original_width() - 200), 140, 30, (int)(Display.original_height() - 366)) {
			public void onCreate() {
				group(11);
				this.handleSize = 60;
			}
		};
		Frapp.GUI.add(blockScroll);
	}
	
	// create empty map : width*height
	public void InitializeMap(int width, int height) {
		blocks = createSkymap(width, height);
	}
	
	// fill map with sky
	private Block[][] createSkymap(int xs, int ys) {
		Block[][] blocks = new Block[xs][ys];
		for (int x = 0; x < blocks.length; x++) {
			for (int y = 0; y < blocks[1].length; y++) {
				blocks[x][y] = new Block(Tile.getTileByName("sky"));
			}
		}
		levelSizeSet = true;
		return blocks;
	}
	
	public String[][] copyBlocks() {
		String[][] copy = new String[blocks.length][blocks[1].length];
		
		for (int x = 0; x < copy.length; x++) {
			for (int y = 0; y < copy[1].length; y++) {
				copy[x][y] = this.blocks[x][y].tile.name;
			}
		}
		
		return copy;
	}
	
	private void copyToClipboard(BufferedImage e) {
		TransferableImage trans = new TransferableImage(e);
		Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
		ClipboardOwner cbo = new ClipboardOwner() {
			@Override public void lostOwnership(Clipboard clipboard, Transferable contents) { }
		};
		c.setContents(trans, cbo);
	}
	
	@Override public void init() {
		optionsDisplayed = false;
		exitDialog = false;
		levelSizeSet = false;
		//reloadScripts();
	}
	
	// rendering goes here! oh yeah, logic too
	@Override public void display() {
		Display.background(165, 189, 200);
		Display.background(background);
		
		if (levelSizeSet) {
			if (!optionsDisplayed && !exitDialog && !selectBlock) {
				// Scrolling
				if (Input.mouseDown(Key.MOUSE_MID)) {
					scroll(Input.velocity);
				}
				
				float sp = scrollSpeed;
				if (Input.down(Key.SHIFT)) {
					sp *= scrollSpeedMult;
				}
				if (Input.down(Key.A)) {
					Block.xOffset += sp;
				}
				if (Input.down(Key.D)) {
					Block.xOffset -= sp;
				}
				if (Input.down(Key.W)) {
					Block.yOffset += sp;
				}
				if (Input.down(Key.S)) {
					Block.yOffset -= sp;
				}
				
				// Zooming
				if (Input.down(Key.UP)) {
					Block.size += zoomSpeed;
					if (Block.size < 2)
						Block.size = 2;
					else if (Block.size > 32)
						Block.size = 32;
				}
				if (Input.down(Key.DOWN)) {
					Block.size -= zoomSpeed;
					if (Block.size < 2)
						Block.size = 2;
					else if (Block.size > 32)
						Block.size = 32;
				}
				if (Input.scrolling && Input.mouse.x > sidebar.x + sidebar.width) {
					Block.size -= Input.wheelRotation * zoomSpeed *2;
					if (Block.size < 2)
						Block.size = 2;
					else if (Block.size > 32)
						Block.size = 32;
				}
				
				// Debugging
				if (Input.down(Key.SPACE) && Input.down(Key.R)) {
					reloadScripts();
				}
			}
			
			for (int x = 0; x < blocks.length; x++) {
				for (int y = 0; y < blocks[1].length; y++) {
					blocks[x][y].render(x, y);
				}
			}
			
			if (!optionsDisplayed && !exitDialog && !selectBlock) { // Pens
				if (Input.mouseDown(Key.MOUSE_LEFT)) {
					if (Input.mouse.x > sidebar.x + sidebar.width && Input.mouse.y < (int)Display.original_height() - 80) {
						for (int xo = -penSize; xo <= penSize; xo++) {
							for (int yo = -penSize; yo <= penSize; yo++) {
								if (penSize != 0 && ((yo == -penSize && xo == -penSize) || (yo == -penSize && xo == penSize) || (yo == penSize && xo == -penSize) || (yo == penSize && xo == penSize)))
									continue;
								set(getTileIndexByPosition(Input.mouse.x - Block.xOffset) + xo, getTileIndexByPosition(Input.mouse.y - Block.yOffset) + yo, selection);
							}
						}
					}
				} else if (!Input.mouseDown(Key.MOUSE_LEFT)){
					for (int xo = -penSize; xo <= penSize; xo++) {
						for (int yo = -penSize; yo <= penSize; yo++) {
							if (penSize != 0 && ((yo == -penSize && xo == -penSize) || (yo == -penSize && xo == penSize) || (yo == penSize && xo == -penSize) || (yo == penSize && xo == penSize)))
								continue;
							setMarked(getTileIndexByPosition(Input.mouse.x - Block.xOffset) + xo, getTileIndexByPosition(Input.mouse.y - Block.yOffset) + yo);
						}
					}
				}
				if (Input.mouseDown(Key.MOUSE_RIGHT)) {
					for (int xo = -penSize; xo <= penSize; xo++) {
						for (int yo = -penSize; yo <= penSize; yo++) {
							if (penSize != 0 && ((yo == -penSize && xo == -penSize) || (yo == -penSize && xo == penSize) || (yo == penSize && xo == -penSize) || (yo == penSize && xo == penSize)))
								continue;
							set(getTileIndexByPosition(Input.mouse.x - Block.xOffset) + xo, getTileIndexByPosition(Input.mouse.y - Block.yOffset) + yo, "Sky");
						}
					}
				}
				
			}
			
			if (renderOutline) {
				Display.color(21, 100);
				Display.stroke(3);
				Display.rect(0 + Block.xOffset, 0 + Block.yOffset, blocks.length * Block.size, blocks[1].length * Block.size);
			}
			
			sidebar.display();
			
			Display.GUI(2);
			if (!optionsDisplayed) {
				if (Input.down(Key.ESC)) {
					exitDialog = true;
					optionsDisplayed = false;
					selectBlock = false;
				}
				
				if (exitDialog) {
					Display.GUI(3);
					Display.font(Fonts.thin_dark, 4);
					Display.write("Quit", 205, Coordinate.X);
					Display.font(Fonts.thin_light, 2);
					Display.write("Return to menu?", 310, Coordinate.X);
					Display.GUI(10);
				}
				if (selectBlock) {
					Display.background(menuBackground);
					
					Display.font(Fonts.thin_light, 2);
					
					blockScroll.handle((Input.scrolling) ? Input.wheelRotation * 3f : 0);
					
					if (Input.clicked) {
						int x = (int)Input.mouse.x;
						int y = (int)Input.mouse.y;
						
						if (y >= 190 && y < 190 + 11*30 && x > 200 && x < Display.original_width() - 200) {
							try {
								int n = 30;
								y -= 190;
								y += blockScroll.value() * ((Tile.tiles.length -11) * n);
								y /= n;
								
								blockSelection.setText(Tile.tiles[y].name);
								blockSelection.onInput();
							} catch (Exception e) { }
						}
					}
					
					for (int i = 0; i < Tile.tiles.length; i++) {
						int x = 200;
						float y = 190 + (i * 30) - blockScroll.value() * ((Tile.tiles.length -11) * 30);
						
						if (y < 190)
							continue;
						if (y >= 190 + 11 * 30)
							break; // break, because everything under it will definetly be invisible if the thing above is invisible
						
						Display.write(" " + Tile.tiles[i].name, x, y);
						if (Tile.tiles[i].extra == Extra.None) {
							Display.image(Tile.set, x - Tile.imageSize *2, y + Tile.imageSize /2, x, y + Tile.imageSize *2 + Tile.imageSize /2, Tile.tiles[i].position, 0, Tile.imageSize);
						} else if (Tile.tiles[i].extra == Extra.Sprite) {
							Display.image(Tile.tiles[i].sprite, x - Tile.imageSize *2, y + Tile.imageSize /2, x, y + Tile.imageSize *2 + Tile.imageSize /2);
						} else if (Tile.tiles[i].extra == Extra.Color) {
							Display.color(Tile.tiles[i].r, Tile.tiles[i].g, Tile.tiles[i].b);
							Display.fill();
							Display.rect(x - Tile.imageSize *2, y + Tile.imageSize /2, Tile.imageSize *2, Tile.imageSize *2 + Tile.imageSize /2);
							
						}
					}
					
					Display.font(Fonts.thin_dark, 3);
					Display.write("Blocks", 130, Coordinate.X);
					
					Display.GUI(11);
				}
			} else {
				Display.background(menuBackground);
				
				Display.font(Fonts.thin_dark, 3);
				Display.write("Options", 120, Coordinate.X);
				
				if (Input.down(Key.ESC)) {
					optionsDisplayed = false;
				}
				
				Display.GUI(6);
			}
			
			if (showProblemImage) {
				Display.background(0, 210);
				Display.image(problem, 0, 0);
				showProblemTimer.tick();
			} else if (showExportImage) {
				Display.background(0, 210);
				Display.image(exported, 0, 0);
				showExportTimer.tick();
			}
		} else {
			Display.GUI(3); // background area
			Display.font(Fonts.thin_dark, 4);
			Display.write("Mapsize", 210, Coordinate.X);
			Display.font(Fonts.thin_light, 1);
			Display.write("Seperated by comma", 285, Coordinate.X);
			
			
			Display.GUI(4); // Foreground buttons
		}
	}
	
	public int getPositionAsTileOnScreen(float m) {
		return (int)((int)(m / Block.size) * Block.size);
	}
	
	public int getTileIndexByPosition(float m) {
		return (int)(m / Block.size);
	}
	
	public void setSymmetry(String sym) {
		if (sym.equals("None")) {
			symmetry = Symmetry.None;
		} else if (sym.equals("Normal")) {
			symmetry = Symmetry.Normal;
		} else if (sym.equals("Team")) {
			symmetry = Symmetry.Team;
		}
	}
	
	public void setId(int xt, int yt, int id) {
		if (xt < 0 || xt >= blocks.length  ||  yt < 0 || yt >= blocks[1].length)
			return;
		
		blocks[xt][yt].setId(id);
	}
	
	public void set(int xt, int yt, String tile) {
		if (xt < 0 || xt >= blocks.length  ||  yt < 0 || yt >= blocks[1].length)
			return;
		
		blocks[xt][yt] = new Block(Tile.getTileByName(tile));
		blocks[xt][yt].placed(xt, yt, blocks);
		notifyNeighbors(xt, yt);
		
		if (symmetry == Symmetry.Normal) {
			int xx = blocks.length - xt -1;
			if (xx == xt) // we don't want to place a block twice, right? (See: mapwidth = 3 ==> set(0, y) --normal--> set(2, y), set(1, y) --bad--> set(1, y)
				return;
			
			blocks[xx][yt] = new Block(Tile.getTileByName(tile));
			blocks[xx][yt].placed(xx, yt, blocks);
			notifyNeighbors(xx, yt);
		} else if (symmetry == Symmetry.Team) {
			int xx = blocks.length - xt -1;
			if (xx == xt) { // we don't want to place a block twice, right? (See: mapwidth = 3 ==> set(0, y) --normal--> set(2, y), set(1, y) --bad--> set(1, y)
				tile = tile.toLowerCase().replace("red", "").replace("blue", ""); // Red Mine becomes [Neutral] Mine, just as blue mine, etc...
			}
			if (tile.toLowerCase().contains("blue")) { // team symmetry is pretty dumb, if name contains blue -> replace it with red vice versa
				tile = tile.replace("blue", "red");
			} else if (tile.toLowerCase().contains("red")) {
				tile = tile.replace("red", "blue");
			}
			blocks[xx][yt] = new Block(Tile.getTileByName(tile));
			blocks[xx][yt].placed(xx, yt, blocks);
			notifyNeighbors(xx, yt);
		}
	}
	
	
	private boolean possible(int x, int y) {
		if (x < 0 || x >= blocks.length  ||  y < 0 || y >= blocks[1].length) // could be a nice one-liner, yet i like this more
			return false;
		return true;
	}
	
	// notify all 4 neighbors, coul've used a for loop. this was easier and is faster
	public void notifyNeighbors(int x, int y) {
		if (possible(x -1, y))
			blocks[x -1][y].notificated(x -1, y, blocks);
		if (possible(x +1, y))
			blocks[x +1][y].notificated(x +1, y, blocks);
		if (possible(x, y -1))
			blocks[x][y -1].notificated(x, y-1, blocks);
		if (possible(x, y +1))
			blocks[x][y +1].notificated(x, y +1, blocks);
	}
	
	public void setMarked(int xt, int yt) {
		if (xt < 0 || xt >= blocks.length  ||  yt < 0 || yt >= blocks[1].length)
			return;
		
		blocks[xt][yt].mark();
	}
	
	public void scroll(Vector vel) {
		scroll(vel.x, vel.y);
	}
	public void scroll(float x, float y) {
		Block.xOffset += x;
		Block.yOffset += y;
	}
	
	public void reloadScripts() {
		scripts.clear();
		loadScripts();
	}
	
	public void loadScripts() {
		Plugin p = new Plugin("data/scripts/load.ps");
		String[] toload = p.value_array("scripts", "{}");
		scripts.clear();
		for (String l : toload) {
			scripts.add(new AutoScript("data/scripts/" + l));
		}
		
		File[] files = Storage.files();
		for (File f : files) {
			scripts.add(new AutoScript(f));
		}
	}
	
	// information :o
	public void failedExporting() {
		showProblemImage = true;
		showProblemTimer.start();
	}
	
	// information :)
	public void doneExporting() {
		showExportImage = true;
		showExportTimer.start();
	}
}
