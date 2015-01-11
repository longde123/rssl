package net.allochie.vm.rssl.natives;

import net.allochie.vm.rssl.RSSLThread;
import net.allochie.vm.rssl.VMException;
import net.allochie.vm.rssl.VMFunctionPointer;
import net.allochie.vm.rssl.VMUserCodeException;
import net.allochie.vm.rssl.global.Callout;
import net.allochie.vm.rssl.global.NativeMethod;

public class NativeThreading {

	@NativeMethod(name = "CreateThread")
	public static RSSLThread createThread(Callout call, VMFunctionPointer pointer) throws VMException {
		if (pointer == null)
			throw new VMUserCodeException(call.closure, "Cannot point to null function");
		RSSLThread thread = call.machine.allocateThread("User thread", pointer);
		if (thread == null)
			throw new VMUserCodeException(call.closure, "Cannot create new thread");
		return thread;
	}

	@NativeMethod(name = "StartThread")
	public static void startThread(Callout call, RSSLThread thread) throws VMException {
		if (thread == null)
			throw new VMUserCodeException(call.closure, "Cannot provide null thread");
		if (thread.running())
			throw new VMUserCodeException(call.closure, "Thread already started");
		thread.runThread();
		call.machine.putThread(thread);
	}

	@NativeMethod(name = "GetThreadStatus")
	public static boolean isRunning(Callout call, RSSLThread thread) throws VMException {
		if (thread == null)
			throw new VMUserCodeException(call.closure, "Cannot provide null thread");
		return thread.running();
	}
}