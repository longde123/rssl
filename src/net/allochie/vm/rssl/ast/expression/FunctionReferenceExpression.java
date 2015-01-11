package net.allochie.vm.rssl.ast.expression;

import net.allochie.vm.rssl.ast.Identifier;

public class FunctionReferenceExpression extends Expression {
	/** The function name image */
	public Identifier name;

	@Override
	public String toString() {
		return "FunctionReferenceExpression: " + name;
	}

}
