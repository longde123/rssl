package net.allochie.vm.jass;

import java.util.HashMap;

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
	public final boolean array;
	public final Type type;

	public VMValue(Object value) {
		if (VMType.arrayType(value)) {
			HashMap<Integer, VMValue> store = new HashMap<Integer, VMValue>();
			Object[] d0 = (Object[]) value;
			for (int i = 0; i < d0.length; i++)
				store.put(i, new VMValue(d0[i]));
			this.value = store;
			this.array = true;
			this.type = VMType.findType(d0[0]);
		} else if (value instanceof HashMap) {
			HashMap<Integer, VMValue> map = (HashMap<Integer, VMValue>) value;
			this.value = map;
			this.array = true;
			this.type = VMType.findType(map.get(0));
		} else {
			this.value = value;
			this.array = false;
			this.type = VMType.findType(value);
		}
	}

	private VMValue(Object value, Type that, boolean array) {
		this.value = value;
		this.type = that;
		this.array = array;
	}

	public boolean isNullable() {
		return (type == Type.nullType) || value == null;
	}

	public VMValue applyCast(Type productType) throws VMException {
		if (array)
			throw new VMException("Cannot cast array");
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

	public VMValue unsafeApplyCast(Type productType) {
		return new VMValue(this.value, productType, this.array);
	}

	@Override
	public String toString() {
		return value.toString();
	}

	public double asNumericType() throws VMException {
		if (type != Type.integerType && type != Type.realType)
			throw new VMException("Not a number");
		if (array)
			throw new VMException("Cannot take numeric value of array");
		if (value instanceof Integer)
			return (double) ((Integer) value).doubleValue();
		return (double) value;
	}

	public String asStringType() throws VMException {
		if (type != Type.stringType)
			throw new VMException("Not a string");
		if (array)
			throw new VMException("Cannot take string value of array");
		return (String) value;
	}

	public boolean asBooleanType() throws VMException {
		if (type != Type.booleanType)
			throw new VMException("Not a boolean");
		if (array)
			throw new VMException("Cannot take boolean value of array");
		return (boolean) value;
	}

	public HashMap<Integer, VMValue> asArrayType() throws VMException {
		if (!array)
			throw new VMException("Cannot take array type of non-array type");
		return (HashMap<Integer, VMValue>) value;
	}

}
