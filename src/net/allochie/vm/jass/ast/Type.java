package net.allochie.vm.jass.ast;

import java.util.HashMap;

import net.allochie.vm.jass.parser.Token;

public class Type {

	public static Type codeType = new Type("code");
	public static Type handleType = new Type("handle");
	public static Type integerType = new Type("integer");
	public static Type realType = new Type("real");
	public static Type booleanType = new Type("boolean");
	public static Type stringType = new Type("string");
	public static Type nullType = new Type("null");

	public static HashMap<String, Type> map = new HashMap<String, Type>();

	/** Type name image */
	public final String typename;

	private Type(String typename) {
		this.typename = typename;
	}

	public static Type fromIdentifier(Identifier typetoken) {
		if (!map.containsKey(typetoken.image))
			map.put(typetoken.image, new Type(typetoken.image));
		return map.get(typetoken.image);
	}

	@Override
	public String toString() {
		return typename;
	}


}
