package net.allochie.vm.jass.ast.statement;

import net.allochie.vm.jass.ast.Identifier;
import net.allochie.vm.jass.ast.Statement;
import net.allochie.vm.jass.ast.expression.Expression;

public class SetStatement extends Statement {

	public Identifier id;
	public Expression val;
	
	@Override
	public String toString() {
		return "SetStatement: " + id + " = " + val;
	}

}
