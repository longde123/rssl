package net.allochie.vm.jass.ast.statement;

import net.allochie.vm.jass.ast.Statement;
import net.allochie.vm.jass.ast.StatementList;
import net.allochie.vm.jass.ast.expression.Expression;

public class ConditionalStatement extends Statement {

	public Expression conditional;
	public StatementList statements;
	public ConditionalStatement child;
	public StatementType type;

	public ConditionalStatement() {
	}

	public ConditionalStatement(StatementType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "ConditionalStatement: " + conditional + ": "
				+ statements.size() + " statements, "
				+ ((child != null) ? "has child" : "no child") + ", type "
				+ type;
	}

}
