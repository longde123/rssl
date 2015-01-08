package net.allochie.vm.jass.natives;

import net.allochie.vm.jass.JASSThread;
import net.allochie.vm.jass.VMException;
import net.allochie.vm.jass.VMFunctionPointer;
import net.allochie.vm.jass.VMUserCodeException;
import net.allochie.vm.jass.global.Callout;
import net.allochie.vm.jass.global.NativeMethod;

public class NativeThreading {

	@NativeMethod(name = "CreateThread")
	public static JASSThread createThread(Callout call, VMFunctionPointer pointer) throws VMException {
		if (pointer == null)
			throw new VMUserCodeException(call.closure, "Cannot point to null function");
		JASSThread thread = call.machine.allocateThread("User thread", pointer);
		if (thread == null)
			throw new VMUserCodeException(call.closure,"Cannot create new thread");
		return thread;
	}

	@NativeMethod(name = "StartThread")
	public static void startThread(Callout call, JASSThread thread) throws VMException {
		if (thread == null)
			throw new VMUserCodeException(call.closure,"Cannot provide null thread");
		if (thread.running())
			throw new VMUserCodeException(call.closure,"Thread already started");
		thread.runThread();
		call.machine.putThread(thread);
	}

	@NativeMethod(name = "GetThreadStatus")
	public static boolean isRunning(Callout call, JASSThread thread) throws VMException {
		if (thread == null)
			throw new VMUserCodeException(call.closure,"Cannot provide null thread");
		return thread.running();
	}
}
