package net.allochie.vm.jass.ast.statement;

import net.allochie.vm.jass.ast.CodePlace;
import net.allochie.vm.jass.ast.Statement;
import net.allochie.vm.jass.ast.StatementList;

public class TryCatchStatement extends Statement {

	public StatementList statements;
	public CodePlace whereCatch;
	public StatementList catchStatements;

}
