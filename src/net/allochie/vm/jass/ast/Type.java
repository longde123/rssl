package net.allochie.vm.jass.ast;

import java.util.HashMap;

import net.allochie.vm.jass.global.TypeRegistry;

public class Type {

	public static Type codeType = new Type("code");
	public static Type handleType = new Type("handle");
	public static Type integerType = new Type("integer");
	public static Type realType = new Type("real");
	public static Type booleanType = new Type("boolean");
	public static Type stringType = new Type("string");
	public static Type nullType = new Type("null");

	/** Type name image */
	public final String typename;

	protected Type(String typename) {
		this.typename = typename;
	}

	public static Type fromIdentifier(Identifier typetoken) {
		return (Type) TypeRegistry.fromString(typetoken.image);
	}

	@Override
	public String toString() {
		return typename;
	}

}
