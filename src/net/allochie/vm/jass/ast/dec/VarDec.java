package net.allochie.vm.jass.ast.dec;

import net.allochie.vm.jass.VMValue;
import net.allochie.vm.jass.ast.Identifier;
import net.allochie.vm.jass.ast.Type;
import net.allochie.vm.jass.ast.expression.Expression;

public class VarDec extends Dec {

	/** The array mode flag */
	public boolean array;
	/** The variable type image */
	public Type type;
	/** The variable name image */
	public Identifier name;
	/** The initializer expression */
	public Expression init;
	/** The constant declaration flag */
	public boolean constant;

	public VarDec() {
	}

	public VarDec(Identifier name, Type type, boolean array, boolean constant, Expression init) {
		this.name = name;
		this.type = type;
		this.array = array;
		this.constant = constant;
		this.init = init;
	}

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
