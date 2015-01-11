package net.allochie.vm.rssl.ast.dec;

import net.allochie.vm.rssl.ast.CodePlace;

public class NativeFuncDef extends Dec {

	/** The function signature */
	public FuncDef def;
	/** Constant declaration flag */
	public boolean constant;
	public CodePlace where;

	public NativeFuncDef(FuncDef def, boolean constant) {
		this.def = def;
		this.constant = constant;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("NativeFunction ");
		result.append(def).append(", ");
		result.append((constant) ? "constant declaration" : "variable declaration");
		return result.toString();
	}

}
