package net.allochie.vm.jass.ast.expression;

import net.allochie.vm.jass.ast.Identifier;
import net.allochie.vm.jass.ast.ParamInvokeList;


public class FunctionCallExpression extends Expression {

	public Identifier name;
	public ParamInvokeList params;
	
	@Override
	public String toString() {
		return "FunctionCallExpression: " + name + "(" + params + ")";
	}

}
