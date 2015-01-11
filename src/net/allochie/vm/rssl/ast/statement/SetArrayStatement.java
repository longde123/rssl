package net.allochie.vm.rssl.ast.statement;

import net.allochie.vm.rssl.ast.Identifier;
import net.allochie.vm.rssl.ast.Statement;
import net.allochie.vm.rssl.ast.expression.Expression;

public class SetArrayStatement extends Statement {

	/** The variable name image */
	public Identifier id;
	/** The index accessor expression */
	public Expression idx;
	/** The value to set */
	public Expression val;

	@Override
	public String toString() {
		return "SetArrayStatement: " + id + "[" + idx + "] = " + val;
	}

}
