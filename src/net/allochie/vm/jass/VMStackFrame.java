package net.allochie.vm.jass;

public abstract class VMStackFrame {

	/** The resultant value */
	public VMValue result;
	/** The last call result */
	public VMValue callResult;

	public abstract boolean finished();

	public abstract void step(JASSMachine machine, JASSThread thread) throws VMException;

	public abstract void frameInfo(StringBuilder place);

	public VMValue getReturnResult() {
		return result;
	}

	public void setInvokeResult(VMValue result) {
		this.callResult = result;
	}

	public boolean hasPreviousCallResult() {
		return (callResult != null);
	}

	public VMValue getPreviousCallResult() {
		VMValue rt = callResult;
		callResult = null;
		return rt;
	}

	public String dumpFrame() {
		StringBuilder frameInfo = new StringBuilder();
		frameInfo(frameInfo);
		return frameInfo.toString();
	}

}
