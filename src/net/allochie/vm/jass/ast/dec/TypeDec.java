package net.allochie.vm.jass.ast.dec;

import net.allochie.vm.jass.ast.Identifier;


public class TypeDec extends Dec {

	public Identifier id;
	public DecType type;
	public Identifier typename;
	
	@Override
	public String toString() {
		return "TypeDec " + id + ", " + type + ", " + typename;
	}

}
