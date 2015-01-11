package net.allochie.vm.rssl.ast.statement;

import net.allochie.vm.rssl.ast.Statement;
import net.allochie.vm.rssl.ast.expression.Expression;

public class ReturnStatement extends Statement {

	/** The return expression */
	public Expression expression;

	@Override
	public String toString() {
		return "ReturnStatement: " + expression;
	}

}
