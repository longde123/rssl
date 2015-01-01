package net.allochie.vm.jass.ast.expression;

import net.allochie.vm.jass.ast.Identifier;

public class ArrayReferenceExpression extends Expression {

	public Expression idx;
	public Identifier name;

	@Override
	public String toString() {
		return "ArrayReferenceExpression: " + name + "[" + idx + "]";
	}

}
