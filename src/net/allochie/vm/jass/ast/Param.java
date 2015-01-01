package net.allochie.vm.jass.ast;

public class Param {

	public Identifier name;
	public Type type;
	
	@Override
	public String toString() {
		return type.toString() + " " + name.toString();
	}

}
