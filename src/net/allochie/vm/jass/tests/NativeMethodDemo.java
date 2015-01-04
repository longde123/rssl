package net.allochie.vm.jass.tests;

import net.allochie.vm.jass.JASSMachine;
import net.allochie.vm.jass.JASSThread;
import net.allochie.vm.jass.VMClosure;
import net.allochie.vm.jass.global.Callout;
import net.allochie.vm.jass.global.NativeMethod;

public class NativeMethodDemo {

	@NativeMethod(name = "PrintConsole")
	public void printString(Callout call, String what) {
		System.out.println(what);
	}

}
