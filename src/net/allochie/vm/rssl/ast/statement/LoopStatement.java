package net.allochie.vm.rssl.ast.statement;

import net.allochie.vm.rssl.ast.Statement;
import net.allochie.vm.rssl.ast.StatementList;

public class LoopStatement extends Statement {

	/** The list of loop statements */
	public StatementList statements;

	@Override
	public String toString() {
		return "LoopStatement: " + statements.size() + " statements.";
	}

}
