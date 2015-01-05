package net.allochie.vm.jass.ast;

import net.allochie.vm.jass.parser.Token;

public class CodePlace {

	public String image;
	public int line;
	public int column;

	public CodePlace(Token what) {
		this.image = what.image;
		this.line = what.beginLine;
		this.column = what.beginColumn;
	}

	@Override
	public String toString() {
		return ((image != null) ? "in section " + image : " in unknown ") + " at ine " + line + ", col " + column;
	}

}
