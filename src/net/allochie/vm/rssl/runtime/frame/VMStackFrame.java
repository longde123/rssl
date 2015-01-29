package net.allochie.vm.rssl.runtime.frame;

import net.allochie.vm.rssl.ast.CodePlace;
import net.allochie.vm.rssl.runtime.RSSLMachine;
import net.allochie.vm.rssl.runtime.RSSLThread;
import net.allochie.vm.rssl.runtime.VMException;
import net.allochie.vm.rssl.runtime.value.VMValue;

public abstract class VMStackFrame {

	/** The resultant value */
	public VMValue result;
	/** The last call result */
	public VMValue callResult;
	/** The current working place */
	public CodePlace workPlace;

	public abstract boolean finished();

	public abstract void step(RSSLMachine machine, RSSLThread thread) throws VMException;

	public abstract void frameInfo(StringBuilder place);

	public VMValue getReturnResult() {
		return result;
	}

	public void setInvokeResult(VMValue result) {
		callResult = result;
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
