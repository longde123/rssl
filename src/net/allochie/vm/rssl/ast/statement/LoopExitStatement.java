package net.allochie.vm.rssl.ast.statement;

import net.allochie.vm.rssl.ast.Statement;
import net.allochie.vm.rssl.ast.expression.Expression;

public class LoopExitStatement extends Statement {

	/** The exit statement expression */
	public Expression conditional;

	@Override
	public String toString() {
		return "LoopExitStatement: " + conditional;
	}

}
