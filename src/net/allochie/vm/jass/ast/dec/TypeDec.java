package net.allochie.vm.jass.ast.dec;

import net.allochie.vm.jass.ast.Identifier;

public class TypeDec extends Dec {

	/** The type name image */
	public Identifier id;
	/** The raw type */
	public DecType type;
	/** The inferred non-raw type */
	public Identifier typename;

	@Override
	public String toString() {
		return "TypeDec " + id + ", " + type + ", " + typename;
	}

}
