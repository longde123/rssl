package net.allochie.vm.jass.ast.expression;

import net.allochie.vm.jass.ast.Identifier;

public class FunctionReferenceExpression extends Expression {
	/** The function name image */
	public Identifier name;

	@Override
	public String toString() {
		return "FunctionReferenceExpression: " + name;
	}

}
