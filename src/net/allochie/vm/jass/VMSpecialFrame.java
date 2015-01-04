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

	public abstract void frameInfo(StringBuilder place);

	public String dumpFrame() {
		StringBuilder frameInfo = new StringBuilder();
		frameInfo(frameInfo);
		return frameInfo.toString();
	}
}
