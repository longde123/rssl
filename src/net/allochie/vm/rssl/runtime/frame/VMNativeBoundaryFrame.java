package net.allochie.vm.rssl.runtime.frame;

import net.allochie.vm.rssl.runtime.RSSLMachine;
import net.allochie.vm.rssl.runtime.RSSLThread;
import net.allochie.vm.rssl.runtime.VMException;

public class VMNativeBoundaryFrame extends VMStackFrame {

	@Override
	public boolean finished() {
		return true;
	}

	@Override
	public void step(RSSLMachine machine, RSSLThread thread) throws VMException {
		throw new VMException(thread, "Cannot step boundary frame; unclean stack!");
	}

	@Override
	public void frameInfo(StringBuilder place) {
		place.append("<<boundary frame>>");
	}

}
