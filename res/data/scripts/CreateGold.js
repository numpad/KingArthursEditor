var name = "Goldmines";
var veins = 0;

function start() {
	self.disableForeach();
}

function execute() {
	for (var x = 0; x < Editor.blocks.length; x++) {
		for (var y = 0; y < Editor.blocks[1].length; y++) {
			if (nameAt(x, y) == "dirt") {
				veins += 1;
			}
		}
	}
	veins = veins /100.0;
	veins *= 6; // Generate veins based on dirt count
	
	for (var i = 0; i < veins; i++) {
		var placed = false;
		
		while (!placed) {
			var x = Maths.random(0, Editor.blocks.length -2);
			var y = Maths.random(0, Editor.blocks[1].length -2);
			if (nameAt(x, y) == "dirt") {
				Editor.set(x, y, "gold");
				var added = false;
				while (!added) {
					var dx = Maths.direction();
					var dy = Maths.direction();
					if (nameAt(x + dx, y + dy) == "dirt") {
						Editor.set(x + dx, y + dy, "gold");
						added = true;
					}
				}
				placed = true;
			}
		}
	}
}

function foreach(x, y, block) {
	if (block == "dirt" && neighbors(x, y, "gold") > 3) {
		Editor.set(x, y, "gold");
	}
}

function neighbors(x, y, block) {
	var nb = 0;
	for (var xx = x -1; xx <= x+1; xx++) {
		for (var yy = y -1; yy <= y+1; yy++) {
			if (nameAt(xx, yy) == block) {
				nb += 1;
			}
		}
	}
	return nb;
}

function nameAt(x, y) {
	if (x < 0 || x > Editor.blocks.length)
		return "sky";
	if (y < 0 || y > Editor.blocks[1].length)
		return "sky";
	return Editor.blocks[x][y].tile.name;
}