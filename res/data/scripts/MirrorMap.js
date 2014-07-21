var name = "Mirror";

var blocks;

function start() {
	self.disableForeach();
}

function execute() {
	var width = Editor.blocks.length *2;
	var height = Editor.blocks[1].length;
	blocks = Editor.copyBlocks();
	Editor.InitializeMap(width, height);
	
	for (var x = 0; x < width /2; x++) {
		for (var y = 0; y < height; y++) {
			Editor.set(x, y, blocks[x][y]);
			}
	}
	for (var x = width/2; x < width; x++) {
		for (var y = 0; y < height; y++) {
			Editor.set(x, y, blocks[width - x -1][y].replace("blue", "red"));
		}
	}
}