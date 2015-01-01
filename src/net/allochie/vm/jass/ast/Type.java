package net.allochie.vm.jass.ast;

import java.util.HashMap;

public class Type {

	public static Type codeType = new Type("code");
	public static Type handleType = new Type("handle");
	public static Type integerType = new Type("integer");
	public static Type realType = new Type("real");
	public static Type booleanType = new Type("boolean");
	public static Type stringType = new Type("string");
	public static Type nullType = new Type("null");

	public static Type findProductType(Type t0, Type t1) {
		if (t0 == codeType || t1 == codeType)
			return null; // not allowed
		if (t0 == handleType || t1 == handleType)
			return null; // not allowed

		if (t0 == booleanType || t1 == booleanType)
			return booleanType; // can only get boolean products

		if (t0 == integerType && t1 == integerType)
			return integerType; // int & int == int
		if ((t0 == realType && t1 == integerType) || (t0 == integerType && t1 == realType))
			return realType; // real & int / int & real = real

		if (t0 == stringType || t1 == stringType)
			return stringType; // can only get concat

		return null; // probably not allowed
	}

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
