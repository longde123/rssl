package net.allochie.vm.jass.ast;

import net.allochie.vm.jass.parser.Token;

public class CodePlace {

	public int line;
	public int column;

	public CodePlace(Token what) {
		this.line = what.beginLine;
		this.column = what.beginColumn;
	}

}
