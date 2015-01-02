package net.allochie.vm.jass;

import net.allochie.vm.jass.ast.Type;

public class VMValue {

	public static boolean areValuesEqual(VMValue v0, VMValue v1) {
		if (v0.isNullable() && v1.isNullable())
			return true;
		if ((v0.isNullable() && !v1.isNullable()) || (!v0.isNullable() && v1.isNullable()))
			return false;
		if (v0.type != v1.type)
			return false;
		return v0.value.equals(v1.value);
	}

	public final Object value;
	public final Type type;

	public VMValue(Object value) {
		this.value = value;
		this.type = VMType.findType(value);
	}

	public boolean isNullable() {
		return (type == Type.nullType) || value == null;
	}

	public VMValue applyCast(Type productType) throws VMException {
		if (productType == type)
			return new VMValue(value);
		if ((type == Type.integerType || type == Type.realType)
				&& (productType == Type.integerType || productType == Type.realType)) {
			if (productType == Type.integerType)
				return new VMValue((int) Math.floor(asNumericType()));
			else
				return new VMValue(asNumericType());
		}
		throw new VMException("Cast from " + type + " to " + productType + " not supported");
	}

	@Override
	public String toString() {
		return type.toString();
	}

	public double asNumericType() throws VMException {
		if (type != Type.integerType && type != Type.realType)
			throw new VMException("Not a number");
		if (value instanceof Integer)
			return (double) ((Integer) value).doubleValue();
		return (double) value;
	}

	public String asStringType() throws VMException {
		if (type != Type.stringType)
			throw new VMException("Not a string");
		return (String) value;
	}

	public boolean asBooleanType() throws VMException {
		if (type != Type.booleanType)
			throw new VMException("Not a boolean");
		return (boolean) value;
	}

}
