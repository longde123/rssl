package net.allochie.vm.jass.ast;

import net.allochie.vm.jass.parser.Token;

public class CodePlace {

	public String image;
	public int line;
	public int column;

	public CodePlace(Token what) {
		image = what.image;
		line = what.beginLine;
		column = what.beginColumn;
	}

	@Override
	public String toString() {
		return ((image != null) ? "section `" + image + "`" : "unknown section") + ", line " + line + ":" + column;
	}

}
