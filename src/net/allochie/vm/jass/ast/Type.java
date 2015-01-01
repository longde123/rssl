package net.allochie.vm.jass.ast;

public class Type {

	public static Type codeType = new Type("code");
	public static Type handleType = new Type("handle");
	public static Type integerType = new Type("integer");
	public static Type realType = new Type("real");
	public static Type booleanType = new Type("boolean");
	public static Type stringType = new Type("string");
	public static Type nullType = new Type("null");

	public final String typename;

	private Type(String typename) {
		this.typename = typename;
	}

	public static Type fromIdentifier(Identifier tmp0) {
		System.out.println("findType: " + tmp0);
		return null;
	}
	
	@Override
	public String toString() {
		return typename;
	}

}
