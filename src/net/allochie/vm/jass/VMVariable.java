package net.allochie.vm.jass;

import java.util.HashMap;

import net.allochie.vm.jass.ast.dec.VarDec;

public class VMVariable {

	class VMSetInitFrame extends VMStackFrame {
		private VMVariable var;
		private VMClosure closure;
		private boolean finished = false;

		public VMSetInitFrame(VMClosure closure, VMVariable var) {
			this.closure = closure;
			this.var = var;
		}

		@Override
		public void step(JASSMachine machine, JASSThread thread) throws VMException {
			if (!hasPreviousCallResult()) {
				thread.resolveExpression(closure, dec.init);
				return;
			}
			var.safeSetValue(getPreviousCallResult());
			finished = true;
		}

		@Override
		public void frameInfo(StringBuilder place) {
			place.append("VMSetInitFrame: ").append(var);
		}

		@Override
		public boolean finished() {
			return finished;
		}
	}

	public final VarDec dec;
	public final VMClosure closure;
	private VMValue value;

	public VMVariable(JASSMachine machine, VMClosure closure, VarDec dec) {
		this.closure = closure;
		this.dec = dec;
		if (this.dec.array)
			this.value = new VMValue(machine, new HashMap<Integer, VMValue>()).unsafeApplyCast(dec.type);
	}

	public void init(JASSThread thread, VMClosure top) throws VMException {
		if (dec.init != null) {
			VMSetInitFrame frame = new VMSetInitFrame(top, this);
			thread.requestFrame(frame);
		}
	}

	public VMValue safeValue() throws VMException {
		if (value == null)
			throw new VMException("Attempt to access undefined variable " + dec.name);
		return value;
	}

	public void safeSetValue(VMValue val) throws VMException {
		if (val == null)
			throw new VMException("Cannot put nullpointer reference in variable " + dec.name);
		if (!val.type.equals(dec.type))
			throw new VMException("Cannot store value type " + val.type + " in var " + dec.name + " with type "
					+ dec.type);
		value = val;
	}

	@Override
	public String toString() {
		return dec.type + " " + dec.name + " = " + ((value != null) ? value : "<null>");
	}

}
