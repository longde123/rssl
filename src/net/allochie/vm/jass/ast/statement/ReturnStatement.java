package net.allochie.vm.jass.ast.statement;

import net.allochie.vm.jass.ast.Statement;
import net.allochie.vm.jass.ast.expression.Expression;

public class ReturnStatement extends Statement {

	/** The return expression */
	public Expression expression;
	
	@Override
	public String toString() {
		return "ReturnStatement: " + expression;
	}

}
