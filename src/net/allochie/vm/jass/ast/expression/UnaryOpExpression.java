package net.allochie.vm.jass.ast.expression;


public class UnaryOpExpression extends Expression {

	public UnaryOp mode;
	public Expression rhs;
	
	@Override
	public String toString() {
		return "UnaryOpExpression: " + mode + " " + rhs;
	}

}
