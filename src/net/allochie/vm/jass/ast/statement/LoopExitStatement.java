package net.allochie.vm.jass.ast.statement;

import net.allochie.vm.jass.ast.Statement;
import net.allochie.vm.jass.ast.expression.Expression;

public class LoopExitStatement extends Statement {

	public Expression conditional;

	@Override
	public String toString() {
		return "LoopExitStatement: " + conditional;
	}

}
