package net.allochie.vm.rssl.ast;

public class Param {

	/** Parameter name identifier */
	public Identifier name;
	/** Parameter type image */
	public Type type;
	/** Parameter is array? */
	public boolean array;

	@Override
	public String toString() {
		return type.toString() + " " + name.toString();
	}

}
