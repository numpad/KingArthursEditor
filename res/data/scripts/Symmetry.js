var name = "Sym None";

function start() {
	self.disableForeach(); // Disable executing foreach function -> Performance
}

function execute() {
	if (name == "Sym None") {
		name = "Sym Normal";
		Editor.setSymmetry("Normal");
	} else if (name == "Sym Normal") {
		name = "Sym Team";
		Editor.setSymmetry("Team");
	} else if (name == "Sym Team") {
		name = "Sym None";
		Editor.setSymmetry("None");
	}
	self.setName(name);
}

function foreach(x, y, name) {
	// Does not get executed => see start method
}
