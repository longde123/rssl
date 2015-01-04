package net.allochie.vm.jass.global;

import net.allochie.vm.jass.JASSMachine;
import net.allochie.vm.jass.JASSThread;
import net.allochie.vm.jass.VMClosure;

public class Callout {

	public JASSMachine machine;
	public JASSThread thread;
	public VMClosure closure;

	public Callout(JASSMachine machine, JASSThread thread, VMClosure closure) {
		this.machine = machine;
		this.thread = thread;
		this.closure = closure;
	}

}
