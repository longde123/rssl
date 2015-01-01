package net.allochie.vm.jass.ast.statement;

import net.allochie.vm.jass.ast.Identifier;
import net.allochie.vm.jass.ast.Statement;
import net.allochie.vm.jass.ast.expression.Expression;

public class SetArrayStatement extends Statement {

	public Identifier id;
	public Expression idx;
	public Expression val;
	
	@Override
	public String toString() {
		return "SetArrayStatement: " + id + "[" + idx + "] = " + val;
	}

}
