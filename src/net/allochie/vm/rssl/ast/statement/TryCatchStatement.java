package net.allochie.vm.rssl.ast.statement;

import net.allochie.vm.rssl.ast.CodePlace;
import net.allochie.vm.rssl.ast.Statement;
import net.allochie.vm.rssl.ast.StatementList;

public class TryCatchStatement extends Statement {

	public StatementList statements;
	public CodePlace whereCatch;
	public StatementList catchStatements;

}
