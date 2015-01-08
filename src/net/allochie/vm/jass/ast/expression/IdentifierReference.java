package net.allochie.vm.jass.ast.expression;

import net.allochie.vm.jass.ast.CodePlace;
import net.allochie.vm.jass.ast.Identifier;

public class IdentifierReference extends Expression {
	/** The variable name image */
	public Identifier identifier;

	public IdentifierReference(Identifier identifier, CodePlace where) {
		this.identifier = identifier;
		this.where = where;
	}

	@Override
	public String toString() {
		return "IdentifierReference: " + identifier;
	}

}
