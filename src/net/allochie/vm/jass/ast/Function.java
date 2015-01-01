package net.allochie.vm.jass.ast;

import net.allochie.vm.jass.ast.dec.FuncDef;

public class Function {

	public boolean constant;
	public FuncDef sig;
	public VarList lvars;
	public StatementList statements;

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(sig).append(", ");
		result.append("locals: (").append(lvars).append("), ");
		result.append(statements.size()).append(" statements, ");
		result.append((constant) ? "constant declaration" : "variable declaration");
		return result.toString();
	}

}
