package net.allochie.vm.jass;

public class VMNativeCallFrame extends VMStackFrame {

	private final VMNativeFunction nfunc;
	private final VMClosure closure;
	private boolean done = false;

	public VMNativeCallFrame(VMClosure closure, VMNativeFunction func) {
		this.closure = closure;
		this.nfunc = func;
	}

	@Override
	public void step(JASSMachine machine, JASSThread thread) throws VMException {
		result = nfunc.executeNative(machine, thread, this.closure);
		done = true;
	}

	@Override
	public void frameInfo(StringBuilder place) {
		place.append("NativeFunctionCall: ").append(nfunc);
	}

	@Override
	public boolean finished() {
		return done;
	}

}
