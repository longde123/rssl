package net.allochie.vm.rssl;

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
