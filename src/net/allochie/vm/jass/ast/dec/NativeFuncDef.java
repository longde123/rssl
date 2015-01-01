package net.allochie.vm.jass.ast.dec;

public class NativeFuncDef extends Dec {

	public FuncDef def;
	public boolean constant;

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
