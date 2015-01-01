package net.allochie.vm.jass.ast.statement;

import net.allochie.vm.jass.ast.Identifier;
import net.allochie.vm.jass.ast.ParamInvokeList;
import net.allochie.vm.jass.ast.Statement;

public class CallStatement extends Statement {

	public ParamInvokeList params;
	public Identifier id;

	@Override
	public String toString() {
		return "CallStatement: " + id + " (" + params + ")";
	}
}
