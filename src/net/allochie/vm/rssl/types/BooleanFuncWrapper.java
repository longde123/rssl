package net.allochie.vm.rssl.types;

import net.allochie.vm.rssl.VMFunction;
import net.allochie.vm.rssl.VMFunctionPointer;
import net.allochie.vm.rssl.VMNativeBoundaryFrame;
import net.allochie.vm.rssl.VMValue;
import net.allochie.vm.rssl.global.Callout;
import net.allochie.vm.rssl.global.NativeMethod;

public class BooleanFuncWrapper {

	private Callout context;
	private VMFunctionPointer pointer;

	public BooleanFuncWrapper() {
	}

	public BooleanFuncWrapper(Callout vm, VMFunctionPointer pointer) {
		context = vm;
		this.pointer = pointer;
	}

	@NativeMethod(name = "Condition")
	public static BooleanFuncWrapper genConditionWrapper(Callout vm, VMFunctionPointer pointer) {
		return new BooleanFuncWrapper(vm, pointer);
	}

	@NativeMethod(name = "InvokeCondition")
	public static boolean invokeCondition(Callout vm, BooleanFuncWrapper what) throws Exception {
		VMFunction wx = vm.machine.findFunction(what.pointer);
		if (wx == null)
			throw new Exception("Can't find pointer function!");
		VMNativeBoundaryFrame boundary = new VMNativeBoundaryFrame();
		vm.thread.requestFrame(boundary);
		vm.thread.foreignCallImmediately(vm.closure, wx, new VMValue[0]);
		boolean result = vm.thread.callStack.peek().getPreviousCallResult().asBooleanType();
		vm.thread.removeFrame(boundary);
		return result;
	}

}
