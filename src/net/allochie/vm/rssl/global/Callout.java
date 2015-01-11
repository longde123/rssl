package net.allochie.vm.rssl.global;

import net.allochie.vm.rssl.RSSLMachine;
import net.allochie.vm.rssl.RSSLThread;
import net.allochie.vm.rssl.VMClosure;

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
