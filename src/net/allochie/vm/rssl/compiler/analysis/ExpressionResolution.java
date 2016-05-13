package net.allochie.vm.rssl.compiler.analysis;

import java.util.ArrayList;
import java.util.HashMap;

import net.allochie.vm.rssl.ast.ReturnType;
import net.allochie.vm.rssl.ast.Type;
import net.allochie.vm.rssl.ast.expression.Expression;
import net.allochie.vm.rssl.compiler.RSSLCompilerException;
import net.allochie.vm.rssl.runtime.RSSLMachine;
import net.allochie.vm.rssl.runtime.VMException;
import net.allochie.vm.rssl.runtime.value.VMFunctionPointer;
import net.allochie.vm.rssl.runtime.value.VMType;
import net.allochie.vm.rssl.runtime.value.VMValue;

public class ExpressionResolution {

	public ReturnType known_return_type;
	public Object known_return_value;
	public ArrayList<ExpressionResolution> children;
	public Expression unresolved;

	public void setValueAndType(Object value) throws RSSLCompilerException {
		known_return_type = new ReturnType(findBaseType(value), VMType.arrayType(value));
		known_return_value = value;
	}

	public void setRaw(Object value, Type rawtype) throws RSSLCompilerException {
		known_return_type = new ReturnType(rawtype, VMType.arrayType(value));
		known_return_value = value;
	}

	private Type findBaseType(Object z) throws RSSLCompilerException {
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
		throw new RSSLCompilerException("Unable to find base type for " + z.getClass());
	}

	public void applyCast(Type newtype) throws RSSLCompilerException {
		if (known_return_type.array)
			throw new RSSLCompilerException("Cannot cast array");
		if (newtype == known_return_type.type)
			return;

		if ((known_return_type.type == Type.integerType || known_return_type.type == Type.realType)
				&& (newtype == Type.integerType || newtype == Type.realType))
			if (newtype == Type.integerType)
				setValueAndType((int) Math.floor(asNumericType()));
			else
				setValueAndType(asNumericType());
		throw new RSSLCompilerException("Cast from " + known_return_type.type + " to " + newtype + " not supported");
	}

	public double asNumericType() throws RSSLCompilerException {
		if (known_return_type.type != Type.integerType && known_return_type.type != Type.realType)
			throw new RSSLCompilerException("Not a number");
		if (known_return_type.array)
			throw new RSSLCompilerException("Cannot take numeric value of array");
		if (known_return_value instanceof Integer)
			return ((Integer) known_return_value).doubleValue();
		return (Double) known_return_value;
	}

	public String asStringType() throws RSSLCompilerException {
		if (known_return_type.type != Type.stringType)
			throw new RSSLCompilerException("Not a string");
		if (known_return_type.array)
			throw new RSSLCompilerException("Cannot take string value of array");
		return (String) known_return_value;
	}

	public boolean asBooleanType() throws RSSLCompilerException {
		if (known_return_type.type != Type.booleanType)
			throw new RSSLCompilerException("Not a boolean");
		if (known_return_type.array)
			throw new RSSLCompilerException("Cannot take boolean value of array");
		return (Boolean) known_return_value;
	}

	public HashMap<Integer, VMValue> asArrayType() throws RSSLCompilerException {
		if (!known_return_type.array)
			throw new RSSLCompilerException("Cannot take array type of non-array type");
		return (HashMap<Integer, VMValue>) known_return_value;
	}

	public boolean hasConstantSolution() {
		if (known_return_type != null) {
			if (known_return_type.type == Type.nullType && known_return_value == null)
				return true;
			if (known_return_type.type != Type.nullType && known_return_value != null)
				return true;
			return false;
		} else {
			return false;
		}
	}

	public ReturnType getReturnType() {
		return known_return_type;
	}

	public Expression getUncompleteExpression() {
		return unresolved;
	}

	public boolean isSolutionVoid() {
		if (known_return_type == null)
			return true;
		if (known_return_type.type == null || known_return_type.type == Type.nullType)
			return true;
		if (known_return_value == null)
			return true;
		return false;
	}

	public boolean equalsOtherVal(ExpressionResolution r) {
		if (!r.hasConstantSolution() || hasConstantSolution())
			return false;
		if (!r.known_return_type.equals(r.known_return_type))
			return false;
		return r.known_return_value.equals(known_return_value);
	}

	public void setType(Type type, boolean array) {
		known_return_type = new ReturnType(type, array);
	}

	public void setExpression(Expression e) {
		unresolved = e;
	}

}
