package net.allochie.vm.jass.ast.statement;

import net.allochie.vm.jass.ast.CodePlace;
import net.allochie.vm.jass.ast.Identifier;
import net.allochie.vm.jass.ast.ParamInvokeList;
import net.allochie.vm.jass.ast.Statement;

public class CallStatement extends Statement {
	/** The function name image */
	public Identifier id;
	/** The list of parameters */
	public ParamInvokeList params;
	public CodePlace where;

	@Override
	public String toString() {
		return "CallStatement: " + id + " (" + params + ")";
	}
}
