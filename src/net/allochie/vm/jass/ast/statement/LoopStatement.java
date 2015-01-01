package net.allochie.vm.jass.ast.statement;

import net.allochie.vm.jass.ast.Statement;
import net.allochie.vm.jass.ast.StatementList;

public class LoopStatement extends Statement {

	public StatementList statements;
	
	@Override
	public String toString() {
		return "LoopStatement: " + statements.size() + " statements.";
	}

}
