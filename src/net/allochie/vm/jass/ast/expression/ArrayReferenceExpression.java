package net.allochie.vm.jass.ast.expression;

import net.allochie.vm.jass.VMClosure;
import net.allochie.vm.jass.VMException;
import net.allochie.vm.jass.VMValue;
import net.allochie.vm.jass.VMVariable;
import net.allochie.vm.jass.ast.Identifier;
import net.allochie.vm.jass.ast.Type;
import net.allochie.vm.jass.ast.dec.TypeDec;

public class ArrayReferenceExpression extends Expression {

	/** The variable name image */
	public Identifier name;
	/** The index access expression */
	public Expression idx;

	@Override
	public String toString() {
		return "ArrayReferenceExpression: " + name + "[" + idx + "]";
	}

	@Override
	public VMValue resolve(VMClosure closure) throws VMException {
		VMVariable var = closure.getVariable(name);
		if (!var.dec.array)
			throw new VMException("Not an array");
		Object[] what = (Object[]) var.value.value;
		VMValue index = idx.resolve(closure);
		if (index.type != Type.integerType)
			throw new VMException(Type.integerType.typename + " expected, got " + index.type.typename);
		Integer idx = (Integer) index.value;
		if (0 > idx || idx < what.length - 1)
			throw new VMException("Index out of bounds");
		Object theWhat = what[idx];
		VMValue val = new VMValue(theWhat, var.dec.type);
		return val;
	}

}
