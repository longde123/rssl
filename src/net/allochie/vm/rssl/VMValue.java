package net.allochie.vm.rssl;

import java.util.HashMap;

import net.allochie.vm.rssl.ast.Type;

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

	public VMValue(RSSLMachine machine, Object value) {
		machine.debugger.trace("vmValue.createValue", value);
		if (VMType.arrayType(value)) {
			machine.debugger.trace("vmValue.createValue.arrayType", value);
			HashMap<Integer, VMValue> store = new HashMap<Integer, VMValue>();
			Object[] d0 = (Object[]) value;
			for (int i = 0; i < d0.length; i++)
				store.put(i, new VMValue(machine, d0[i]));
			this.value = store;
			array = true;
			type = VMType.findType(machine, d0[0]);
		} else if (value instanceof HashMap) {
			machine.debugger.trace("vmValue.createType.hashType", value);
			HashMap<Integer, VMValue> map = (HashMap<Integer, VMValue>) value;
			this.value = map;
			array = true;
			type = VMType.findType(machine, map.get(0));
		} else {
			machine.debugger.trace("vmValue.createType.rawType", value);
			this.value = value;
			array = false;
			type = VMType.findType(machine, value);
		}
	}

	private VMValue(Object value, Type that, boolean array) {
		this.value = value;
		type = that;
		this.array = array;
	}

	public boolean isNullable() {
		return (type == Type.nullType) || value == null;
	}

	public VMValue applyCast(RSSLMachine machine, Type productType) throws VMException {
		machine.debugger.trace("vmValue.applyCast", this, productType);
		if (array)
			throw new VMException(this, "Cannot cast array");
		if (productType == type)
			return new VMValue(machine, value);
		machine.debugger.trace("vmValue.applyCast.apply", this, productType);
		if ((type == Type.integerType || type == Type.realType)
				&& (productType == Type.integerType || productType == Type.realType))
			if (productType == Type.integerType)
				return new VMValue(machine, (int) Math.floor(asNumericType()));
			else
				return new VMValue(machine, asNumericType());
		throw new VMException(this, "Cast from " + type + " to " + productType + " not supported");
	}

	public VMValue unsafeApplyCast(Type productType) {
		return new VMValue(value, productType, array);
	}

	@Override
	public String toString() {
		return (value != null) ? value.toString() : "<null>";
	}

	public double asNumericType() throws VMException {
		if (type != Type.integerType && type != Type.realType)
			throw new VMException(this, "Not a number");
		if (array)
			throw new VMException(this, "Cannot take numeric value of array");
		if (value instanceof Integer)
			return ((Integer) value).doubleValue();
		return (double) value;
	}

	public String asStringType() throws VMException {
		if (type != Type.stringType)
			throw new VMException(this, "Not a string");
		if (array)
			throw new VMException(this, "Cannot take string value of array");
		return (String) value;
	}

	public boolean asBooleanType() throws VMException {
		if (type != Type.booleanType)
			throw new VMException(this, "Not a boolean");
		if (array)
			throw new VMException(this, "Cannot take boolean value of array");
		return (boolean) value;
	}

	public HashMap<Integer, VMValue> asArrayType() throws VMException {
		if (!array)
			throw new VMException(this, "Cannot take array type of non-array type");
		return (HashMap<Integer, VMValue>) value;
	}

}
