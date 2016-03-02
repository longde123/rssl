package net.allochie.vm.rssl.ast;

import net.allochie.vm.rssl.runtime.api.TypeRegistry;

public class Type {

	public static Type codeType = Type.fromIdentifier("code");
	public static Type handleType = Type.fromIdentifier("handle");
	public static Type integerType = Type.fromIdentifier("integer");
	public static Type realType = Type.fromIdentifier("real");
	public static Type booleanType = Type.fromIdentifier("boolean");
	public static Type stringType = Type.fromIdentifier("string");
	public static Type nullType = Type.fromIdentifier("nothing");

	/** Type name image */
	public final String typename;

	protected Type(String typename) {
		this.typename = typename;
	}

	public static Type fromIdentifier(Identifier typetoken, CodePlace codePlace) {
		return Type.fromIdentifier(typetoken.image);
	}

	public static Type fromIdentifier(String what) {
		return TypeRegistry.fromString(what);
	}

	@Override
	public String toString() {
		return typename;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Type))
			return false;
		Type that = (Type) o;
		return that.typename.equals(this.typename);
	}

}
