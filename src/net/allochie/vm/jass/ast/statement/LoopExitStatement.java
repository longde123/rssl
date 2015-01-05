package net.allochie.vm.jass.ast.statement;

import net.allochie.vm.jass.ast.CodePlace;
import net.allochie.vm.jass.ast.Statement;
import net.allochie.vm.jass.ast.expression.Expression;

public class LoopExitStatement extends Statement {

	/** The exit statement expression */
	public Expression conditional;
	public CodePlace where;

	@Override
	public String toString() {
		return "LoopExitStatement: " + conditional;
	}

}
