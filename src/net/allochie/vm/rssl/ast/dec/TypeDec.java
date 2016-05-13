package net.allochie.vm.rssl.ast.dec;

import net.allochie.vm.rssl.ast.CodePlace;
import net.allochie.vm.rssl.ast.Identifier;

public class TypeDec extends Dec {

	/** The type name image */
	public Identifier id;
	/** The raw type */
	public DecType type;
	/** The inferred non-raw type */
	public Identifier typename;
	/** Where the declaration was created */
	public CodePlace where;

	@Override
	public String toString() {
		if (typename == null && type == null)
			return "TypeDec " + id + "(" + where.toString() + "), root type";
		if (typename == null && type != null)
			return "TypeDec " + id + "(" + where.toString() + "), R[" + type + "]";
		if (typename != null && type == null)
			return "TypeDec " + id + "(" + where.toString() + "), S[" + typename + "]";
		return "TypeDec " + id + " (" + where.toString() + "), R[" + type + "], S[" + typename + "]";
	}

}
