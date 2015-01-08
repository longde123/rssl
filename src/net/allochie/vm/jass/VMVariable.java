package net.allochie.vm.jass;

import java.util.HashMap;

import net.allochie.vm.jass.ast.Statement;
import net.allochie.vm.jass.ast.dec.VarDec;

public class VMVariable {

	class VMSetInitFrame extends VMStackFrame {
		private VMVariable var;
		private VMClosure closure;
		private VarDec statement;
		private boolean finished = false;

		public VMSetInitFrame(VMClosure closure, VarDec statement, VMVariable var) {
			this.closure = closure;
			this.var = var;
			this.statement = statement;
		}

		@Override
		public void step(JASSMachine machine, JASSThread thread) throws VMException {
			machine.debugger.trace("vmSetInitFrame.step", this, thread);
			if (!hasPreviousCallResult()) {
				thread.resolveExpression(closure, dec.init);
				return;
			}
			VMValue result = getPreviousCallResult();
			if (!VMType.instanceofType(result.type, var.dec.type))
				throw new VMUserCodeException(statement, "Invalid initializer, got " + result.type + ", expected "
						+ var.dec.type);
			var.safeSetValue(result);
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

	public void init(JASSThread thread, VarDec var, VMClosure top) throws VMException {
		if (dec.init != null) {
			VMSetInitFrame frame = new VMSetInitFrame(top, var, this);
			thread.requestFrame(frame);
		}
	}

	public VMValue safeValue() throws VMException {
		if (value == null)
			throw new VMException(dec, "Attempt to access undefined variable " + dec.name);
		return value;
	}

	public void safeSetValue(VMValue val) throws VMException {
		if (val == null)
			throw new VMException(dec, "Unchecked nullpointer write to " + dec.name);
		if (!VMType.instanceofType(val.type, dec.type))
			throw new VMException(dec, "Unchecked write of type " + val.type + " to var " + dec.name + ", expected "
					+ dec.type);
		value = val;
	}

	@Override
	public String toString() {
		return dec.type + " " + dec.name + " = " + ((value != null) ? value : "<null>");
	}

}
