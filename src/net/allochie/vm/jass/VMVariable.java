package net.allochie.vm.jass;

import net.allochie.vm.jass.ast.dec.VarDec;

public class VMVariable {

	class VMSetInitFrame extends VMSpecialFrame {
		VMVariable var;

		public VMSetInitFrame(VMClosure closure, VMVariable v) {
			super(closure);
			var = v;
		}

		@Override
		public void doSpecialStep(JASSMachine machine) throws VMException {
			if (!hasPreviousCallResult()) {
				machine.resolveExpression(closure, dec.init);
				return;
			}
			var.value = getPreviousCallResult();
			finished = true;
		}
	}

	public final VarDec dec;
	public final VMClosure closure;
	private VMValue value;

	public VMVariable(VMClosure closure, VarDec dec) {
		this.closure = closure;
		this.dec = dec;
	}

	public void init(JASSMachine machine, VMClosure top) throws VMException {
		if (dec.init != null) {
			VMSetInitFrame frame = new VMSetInitFrame(top, this);
			machine.requestFrame(frame);
		}
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
