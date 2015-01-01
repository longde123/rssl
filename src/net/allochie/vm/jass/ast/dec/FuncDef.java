package net.allochie.vm.jass.ast.dec;

import net.allochie.vm.jass.ast.Identifier;
import net.allochie.vm.jass.ast.ParamList;
import net.allochie.vm.jass.ast.Type;

public class FuncDef {

	public Identifier id;
	public ParamList params;
	public Type returns;

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(id).append("(").append(params).append(")");
		result.append(" => ").append(returns);
		return result.toString();
	}
}
