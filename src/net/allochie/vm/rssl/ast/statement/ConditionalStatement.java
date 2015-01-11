package net.allochie.vm.rssl.ast.statement;

import net.allochie.vm.rssl.ast.Statement;
import net.allochie.vm.rssl.ast.StatementList;
import net.allochie.vm.rssl.ast.expression.Expression;

public class ConditionalStatement extends Statement {

	/** The conditional expression */
	public Expression conditional;
	/** The conditional's statements */
	public StatementList statements;
	/** The child conditional expression */
	public ConditionalStatement child;
	/** The type of conditional */
	public StatementType type;

	public ConditionalStatement() {
	}

	public ConditionalStatement(StatementType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "ConditionalStatement: " + conditional + ": " + statements.size() + " statements, "
				+ ((child != null) ? "has child" : "no child") + ", type " + type;
	}

}
