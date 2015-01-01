package net.allochie.vm.jass.ast;

public class Param {

	/** Parameter name identifier */
	public Identifier name;
	/** Parameter type image */
	public Type type;
	
	@Override
	public String toString() {
		return type.toString() + " " + name.toString();
	}

}
