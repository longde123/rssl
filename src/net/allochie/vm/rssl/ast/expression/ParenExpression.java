package net.allochie.vm.rssl.ast.expression;

public class ParenExpression extends Expression {
	/** The nested expression */
	public Expression child;

	@Override
	public String toString() {
		return "ParenExpression: " + child;
	}

}
