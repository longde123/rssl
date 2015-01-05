package net.allochie.vm.jass.ast.statement;

import net.allochie.vm.jass.ast.CodePlace;
import net.allochie.vm.jass.ast.Statement;
import net.allochie.vm.jass.ast.StatementList;

public class LoopStatement extends Statement {

	/** The list of loop statements */
	public StatementList statements;
	public CodePlace where;

	@Override
	public String toString() {
		return "LoopStatement: " + statements.size() + " statements.";
	}

}
