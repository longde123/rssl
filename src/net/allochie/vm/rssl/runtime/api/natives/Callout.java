package net.allochie.vm.rssl.runtime.api.natives;

import net.allochie.vm.rssl.runtime.RSSLMachine;
import net.allochie.vm.rssl.runtime.RSSLThread;
import net.allochie.vm.rssl.runtime.VMClosure;

public class Callout {

	public RSSLMachine machine;
	public RSSLThread thread;
	public VMClosure closure;

	public Callout(RSSLMachine machine, RSSLThread thread, VMClosure closure) {
		this.machine = machine;
		this.thread = thread;
		this.closure = closure;
	}

}
