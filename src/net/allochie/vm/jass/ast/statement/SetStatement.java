package net.allochie.vm.jass.ast.statement;

import net.allochie.vm.jass.ast.Identifier;
import net.allochie.vm.jass.ast.Statement;
import net.allochie.vm.jass.ast.expression.Expression;

public class SetStatement extends Statement {
	/** The variable name image */
	public Identifier id;
	/** The value to set */
	public Expression val;

	@Override
	public String toString() {
		return "SetStatement: " + id + " = " + val;
	}

}
