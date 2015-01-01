package net.allochie.vm.jass.ast.expression;


public class BinaryOpExpression extends Expression {

	/** The left hand side expression */
	public Expression lhs;
	/** The operation mode */
	public BinaryOp mode;
	/** The right hand side expression */
	public Expression rhs;
	
	@Override
	public String toString() {
		return "BinaryOpExpression: " + lhs + " " + mode + " " + rhs;
	}

}
