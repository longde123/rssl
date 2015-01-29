package net.allochie.vm.rssl.runtime.frame;

import net.allochie.vm.rssl.runtime.RSSLMachine;
import net.allochie.vm.rssl.runtime.RSSLThread;
import net.allochie.vm.rssl.runtime.VMClosure;
import net.allochie.vm.rssl.runtime.VMException;
import net.allochie.vm.rssl.runtime.natives.NativeMethodException;
import net.allochie.vm.rssl.runtime.natives.NativeRaisedError;
import net.allochie.vm.rssl.runtime.value.VMNativeFunction;

public class VMNativeCallFrame extends VMStackFrame {

	private final VMNativeFunction nfunc;
	private final VMClosure closure;
	private boolean done = false;

	public VMNativeCallFrame(VMClosure closure, VMNativeFunction func) {
		this.closure = closure;
		nfunc = func;
	}

	@Override
	public void step(RSSLMachine machine, RSSLThread thread) throws VMException {
		machine.debugger.trace("vmNativeCallFrame.step", this, thread);
		try {
			result = nfunc.executeNative(machine, thread, closure);
		} catch (NativeMethodException nmex) {
			nmex.what = nfunc;
			throw nmex;
		} catch (NativeRaisedError nre) {
			nre.what = nfunc;
			throw nre;
		}
		machine.debugger.trace("vmNativeCallFrame.exitFrame", this, thread, result);
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
