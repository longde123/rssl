package net.allochie.vm.rssl.ast.expression;

import net.allochie.vm.rssl.ast.Identifier;

public class ArrayReferenceExpression extends Expression {

	/** The variable name image */
	public Identifier name;
	/** The index access expression */
	public Expression idx;

	@Override
	public String toString() {
		return "ArrayReferenceExpression: " + name + "[" + idx + "]";
	}
}
