package net.allochie.vm.rssl.ast.statement;

import net.allochie.vm.rssl.ast.Identifier;
import net.allochie.vm.rssl.ast.ParamInvokeList;
import net.allochie.vm.rssl.ast.Statement;

public class CallStatement extends Statement {
	/** The function name image */
	public Identifier id;
	/** The list of parameters */
	public ParamInvokeList params;

	@Override
	public String toString() {
		return "CallStatement: " + id + " (" + params + ")";
	}
}
