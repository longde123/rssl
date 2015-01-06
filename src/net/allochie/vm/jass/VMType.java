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
	 * <p>
	 * Find the VM type of an object.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>If the object is NULL, or Void or Void[], the type is nulltype</li>
	 * <li>If the object is Integer or Integer[], the type is integertype</li>
	 * <li>If the object is Float, Float[], Double or Double[], the type is
	 * realtype</li>
	 * <li>If the object is String or String[], the type is stringtype</li>
	 * <li>If the object is Boolean or Boolean[], the type is booleantype</li>
	 * <li>If the object is VMFunctionPointer or VMFunctionPointer[], the type
	 * is codetype</li>
	 * <li>Else, the VM asks the type registry if it knows what "type name" the
	 * object is, and:
	 * <ul>
	 * <li>If the registry does not have an explicit type for the object, the
	 * type is HANDLE and cannot be cast; or</li>
	 * <li>The registry has an explicit type name for the object; the type name
	 * is provided and the handle can be cast to that type name or any parent of
	 * the type (including HANDLE).</li></li>
	 * </ul>
	 * </p>
	 * 
	 * @param machine
	 *            The working VM
	 * @param z
	 *            The object
	 * @return The type
	 */
	public static Type findType(JASSMachine machine, Object z) {
		machine.debugger.trace("vmType.findType", z);
		if (z == null || z instanceof Void || z instanceof Void[])
			return Type.nullType;
		if (z instanceof Integer || z instanceof Integer[])
			return Type.integerType;
		if (z instanceof Float || z instanceof Float[] || z instanceof Double || z instanceof Double[])
			return Type.realType;
		if (z instanceof String || z instanceof String[])
			return Type.stringType;
		if (z instanceof Boolean || z instanceof Boolean[])
			return Type.booleanType;
		if (z instanceof VMFunctionPointer || z instanceof VMFunctionPointer[])
			return Type.codeType;
		return TypeRegistry.findPreferredType(z, machine);
	}

	public static boolean isInstanceOf(JASSMachine machine, Type t, Object z) {
		machine.debugger.trace("vmType.isInstanceOf", t, z);
		return instanceofType(findType(machine, z), t);
	}

	/**
	 * Determines if the source type is an instance of the receiving type.
	 * 
	 * @param src
	 *            The source type.
	 * @param recv
	 *            The receiving type.
	 * @return If the source type is an instance of the receiving type.
	 */
	public static boolean instanceofType(Type src, Type recv) {
		if (src.equals(recv))
			return true;
		if ((src instanceof VMType) && (recv instanceof VMType)) {
			VMType a = (VMType) src, rt = (VMType) recv;
			while (a != null) {
				if (rt.mutableOf(a))
					return true;
				a = a.parentType;
			}
		}
		return false;
	}

	private boolean mutableOf(VMType a) {
		return a.typename.equals(typename);
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

	private VMType parentType;

	public VMType(String type) {
		super(type);
	}

	public void setExtensionOf(String image) {
		setExtensionOf(TypeRegistry.fromString(image));
	}

	public void setExtensionOf(Type handleType) {
		this.parentType = (VMType) handleType;
	}

	@Override
	public boolean equals(Object o) {
		return false;
	}
}
