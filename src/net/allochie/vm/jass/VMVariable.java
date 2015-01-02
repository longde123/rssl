package net.allochie.vm.jass;

import net.allochie.vm.jass.ast.dec.VarDec;

public class VMVariable {

	public final VarDec dec;
	public final VMClosure closure;
	private VMValue value;

	public VMVariable(VMClosure closure, VarDec dec) {
		this.closure = closure;
		this.dec = dec;
	}

	public void init(JASSMachine machine, VMClosure top) throws VMException {
		if (dec.init != null)
			safeSetValue(machine.resolveExpression(top, dec.init));
	}

	public VMValue safeValue() throws VMException {
		if (value == null)
			throw new VMException("Attempt to access undefined variable");
		return value;
	}

	public void safeSetValue(VMValue val) throws VMException {
		if (val.type != dec.type)
			throw new VMException("Cannot store " + val.type + " in var " + dec.name + " with type " + dec.type);
		value = val;
	}

	@Override
	public String toString() {
		return value.type + " " + dec.name + " = " + value;
	}

}
