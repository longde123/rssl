package net.allochie.vm.jass.ast.expression;

import net.allochie.vm.jass.ast.Identifier;
import net.allochie.vm.jass.ast.ParamInvokeList;

public class FunctionCallExpression extends Expression {
	/** The function name image */
	public Identifier name;
	/** The list of invoke parameters */
	public ParamInvokeList params;

	@Override
	public String toString() {
		return "FunctionCallExpression: " + name + "(" + params + ")";
	}

}
