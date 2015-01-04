package net.allochie.vm.jass;

import net.allochie.vm.jass.ast.Type;
import net.allochie.vm.jass.ast.dec.TypeDec;
import net.allochie.vm.jass.global.TypeRegistry;

public class VMType extends Type {

	public static Type findProductType(Type t0, Type t1) {
		if (t0 == codeType || t1 == codeType || t0 == handleType || t1 == handleType)
			return null; // not allowed

		if (t0 == booleanType && t1 == booleanType)
			return booleanType; // can only get boolean products

		if (t0 == integerType && t1 == integerType)
			return integerType; // int & int == int

		if (t0 == realType && t1 == realType)
			return realType; // real & real == real

		if ((t0 == realType && t1 == integerType) || (t0 == integerType && t1 == realType))
			return realType; // real & int or int & real = real

		if (t0 == stringType || t1 == stringType)
			return stringType; // can only get concat

		return null; // probably not allowed
	}

	/**
	 * Find the VM type of an object. If the object has no native type in the
	 * VM, the type is HANDLE. If the object is null, the VM type is NULLTYPE.
	 * 
	 * @param machine
	 *            The working VM
	 * @param z
	 *            The object
	 * @return The type
	 */
	public static Type findType(JASSMachine machine, Object z) {
		if (z == null || z instanceof Void || z instanceof Void[])
			return Type.nullType;
		if (z instanceof Integer || z instanceof Integer[])
			return Type.integerType;
		if (z instanceof Float || z instanceof Float[] || z instanceof Double || z instanceof Double[])
			return Type.realType;
		if (z instanceof String || z instanceof String[])
			return Type.stringType;
		if (z instanceof Boolean)
			return Type.booleanType;
		if (z instanceof VMFunctionPointer || z instanceof VMFunctionPointer[])
			return Type.codeType;
		return TypeRegistry.findPreferredType(z, machine);
	}

	public static boolean arrayType(Object z) {
		return (z instanceof Void[] || z instanceof Integer[] || z instanceof Float[] || z instanceof Double[]
				|| z instanceof String[] || z instanceof VMFunctionPointer[]);
	}

	public static boolean numericType(Object z) {
		return (z instanceof Integer || z instanceof Float || z instanceof Double);
	}

	public static boolean isTypeNumeric(Type t) {
		return (t == Type.integerType || t == Type.realType);
	}

	public static boolean isInstanceOf(JASSMachine machine, Type t, Object z) {
		return (findType(machine, z) == t);
	}

	private VMType(String typename) {
		super(typename);
	}

	public static VMType fromDec(TypeDec type) {
		// TODO Auto-generated method stub
		return null;
	}
}
