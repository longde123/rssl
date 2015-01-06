package net.allochie.vm.jass;

import java.util.ArrayList;
import java.util.HashMap;

import net.allochie.vm.jass.api.IDebugger;
import net.allochie.vm.jass.ast.Identifier;

public class JASSMachine extends Thread {

	/** List of all known VM types */
	public HashMap<String, VMType> types = new HashMap<String, VMType>();
	/** List of all global variables */
	public HashMap<String, VMVariable> globals = new HashMap<String, VMVariable>();
	/** List of all native functions */
	public HashMap<String, VMNativeFunction> natives = new HashMap<String, VMNativeFunction>();
	/** List of all real functions */
	public HashMap<String, VMFunction> funcs = new HashMap<String, VMFunction>();
	/** The global closure */
	public VMClosure global = new VMClosure(this);
	/** The thread list */
	public ArrayList<JASSThread> threads = new ArrayList<JASSThread>();
	/** The list of dead threads */
	public ArrayList<JASSThread> dead = new ArrayList<JASSThread>();
	/** The list of new live threads */
	public ArrayList<JASSThread> live = new ArrayList<JASSThread>();

	/** The system debugger */
	public IDebugger debugger;

	public JASSMachine(String title) {
		super(title);
	}

	public void putThread(JASSThread thread) {
		synchronized (live) {
			live.add(thread);
		}
	}

	@Override
	public void run() {
		while (threads.size() != 0 || live.size() != 0) {
			advance();
		}
	}

	public void advance() {
		debugger.trace("machine.advance");
		synchronized (live) {
			if (live.size() > 0) {
				for (JASSThread add : live)
					threads.add(add);
				live.clear();
			}
		}
		for (JASSThread thread : threads) {
			if (!thread.dead()) {
				try {
					debugger.trace("thread.advance", thread);
					thread.advance();
				} catch (VMException e) {
					debugger.fatal("thread.advance", thread, e);
					e.printStackTrace();
					System.err.println("Frames on stack: ");
					for (int i = thread.callStack.size() - 1; i >= 0; i--) {
						VMStackFrame frame = thread.callStack.get(i);
						System.err.println(i + ": " + frame.dumpFrame());
					}
					dead.add(thread);
				}
				if (thread.dead())
					dead.add(thread);
			}
		}
		if (dead.size() > 0) {
			for (JASSThread remove : dead)
				threads.remove(remove);
			dead.clear();
		}
	}

	public VMFunction findFunction(VMFunctionPointer invokeFunc) {
		if (invokeFunc == null)
			return null;
		if (invokeFunc.functionVal != null)
			return invokeFunc.functionVal;
		return findFunction(invokeFunc.functionName);
	}

	public VMFunction findFunction(Identifier identifier) {
		debugger.trace("machine.findFunction", identifier);
		return findFunction(identifier.image);
	}

	public VMFunction findFunction(String name) {
		debugger.trace("machine.findFunction", name);
		VMFunction function = funcs.get(name);
		if (function != null)
			return function;
		function = natives.get(name);
		if (function != null)
			return function;
		return null;
	}

	public VMVariable findGlobal(Identifier identifier) {
		debugger.trace("machine.findGlobal", identifier);
		return globals.get(identifier.image);
	}

	public JASSThread allocateThread(String name, VMFunctionPointer pointer) {
		debugger.trace("machine.allocateThread", name, pointer);
		return new JASSThread(this, name, global, pointer);
	}

}
