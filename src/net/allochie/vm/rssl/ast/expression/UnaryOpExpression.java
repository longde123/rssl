package net.allochie.vm.rssl.ast.expression;

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
