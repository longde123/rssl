package net.allochie.vm.jass.ast.expression;

public class UnaryOpExpression extends Expression {

	/** The unary operation mode */
	public UnaryOp mode;
	/** The right hand side of the unary operation */
	public Expression rhs;

	@Override
	public String toString() {
		return "UnaryOpExpression: " + mode + " " + rhs;
	}

}
