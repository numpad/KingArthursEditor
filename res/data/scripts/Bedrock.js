var name = "Bedrock";

function start() {
	self.disableForeach();
}

function execute() {
	var y = Editor.blocks[1].length -1;
	for (var x = 0; x < Editor.blocks.length; x++) {
		Editor.set(x, y, "Bedrock");
	}
}