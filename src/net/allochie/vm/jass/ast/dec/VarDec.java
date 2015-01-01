package net.allochie.vm.jass.ast.dec;

import net.allochie.vm.jass.ast.Identifier;
import net.allochie.vm.jass.ast.Type;
import net.allochie.vm.jass.ast.expression.Expression;

public class VarDec extends Dec {

	public boolean array;
	public Type type;
	public Identifier name;
	public Expression init;

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(type).append(" ").append(name);
		result.append((array) ? "[]" : "");
		if (init != null)
			result.append(" init: ").append(init);
		return result.toString();
	}

}
