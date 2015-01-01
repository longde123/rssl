package net.allochie.vm.jass.ast.expression;


public class BinaryOpExpression extends Expression {

	public Expression lhs;
	public BinaryOp mode;
	public Expression rhs;
	
	@Override
	public String toString() {
		return "BinaryOpExpression: " + lhs + " " + mode + " " + rhs;
	}

}
