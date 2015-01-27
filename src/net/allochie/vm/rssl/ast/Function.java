package net.allochie.vm.rssl.ast;

import net.allochie.vm.rssl.ast.dec.FuncDef;

public class Function {

	/** Constant declaration flag */
	public boolean constant;
	/** Function signature */
	public FuncDef sig;
	/** Function local var heap */
	public VarList lvars;
	/** Function statement heap */
	public StatementList statements;
	/** Function declaration location */
	public CodePlace where;

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(sig).append(", ");
		result.append("locals: (").append(lvars).append("), ");
		result.append((statements != null) ? statements.size() : "<null>").append(" statements, ");
		result.append((constant) ? "constant declaration" : "variable declaration").append(", ");
		result.append(where.toString());
		return result.toString();
	}

}
