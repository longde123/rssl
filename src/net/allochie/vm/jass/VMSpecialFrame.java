package net.allochie.vm.jass;

public abstract class VMSpecialFrame extends VMCallFrame {

	public VMSpecialFrame(VMClosure closure) {
		super(closure, null, false);
	}

	@Override
	public void step(JASSMachine machine) throws VMException {
		doSpecialStep(machine);
	}

	public abstract void doSpecialStep(JASSMachine machine) throws VMException;
}
