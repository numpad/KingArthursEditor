var name = "AutoGrass";

function start() {
	self.enableForeach();
}

function execute() { // Executes once when the user clicks "Execute Script".
	
}

function foreach(x, y, name) { // Executes once for every block giving the arguments of the x and y position in the array and the name of the block.
	if (name == "dirt") {
		if (nameAt(x, y -1) == "sky") {
			Editor.set(x, y-1, "grass");
			Editor.setId(x, y, 2);
			Editor.setId(x, y-1, Maths.random(0, 2));
		}
	}
}

function nameAt(x, y) {
	return Editor.blocks[x][y].tile.name;
}