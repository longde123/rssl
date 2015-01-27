package net.allochie.vm.rssl;

import java.util.ArrayList;
import java.util.HashMap;

import net.allochie.vm.rssl.api.IDebugger;
import net.allochie.vm.rssl.api.ThreadSchedule;
import net.allochie.vm.rssl.api.ThreadSchedule.Schedule;
import net.allochie.vm.rssl.ast.Identifier;
import net.allochie.vm.rssl.debug.VoidDebugger;

public class RSSLMachine extends Thread {

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
	/** The thread schedule */
	public ThreadSchedule schedule;
	/** The thread list */
	public ArrayList<RSSLThread> threads = new ArrayList<RSSLThread>();
	/** The list of dead threads */
	public ArrayList<RSSLThread> dead = new ArrayList<RSSLThread>();
	/** The list of new live threads */
	public ArrayList<RSSLThread> live = new ArrayList<RSSLThread>();
	/** If the system has been halted */
	public boolean halt = false;
	/** The wait object */
	private final Object wait = new Object();

	/** The system debugger */
	public IDebugger debugger = new VoidDebugger();

	public RSSLMachine(String title, IDebugger debugger, ThreadSchedule schedule) {
		super(title);
		this.debugger = debugger;
		this.schedule = schedule;
		debugger.trace("machine.init.schedule", schedule);
	}

	public void setDebugger(IDebugger what) {
		debugger = what;
	}

	public void putThread(RSSLThread thread) {
		debugger.trace("machine.putThread", thread);
		synchronized (live) {
			live.add(thread);
		}
		synchronized (wait) {
			wait.notifyAll();
		}
	}

	@Override
	public void run() {
		while (!halt) {
			while (threads.size() == 0 && live.size() == 0 && dead.size() == 0) {
				synchronized (wait) {
					try {
						wait.wait();
					} catch (Throwable t) {
					}
				}
				if (halt)
					return;
			}
			advance();
		}
	}

	public void advance() {
		debugger.trace("machine.advance");
		synchronized (live) {
			if (live.size() > 0) {
				for (RSSLThread add : live)
					threads.add(add);
				live.clear();
			}
		}

		int speed = (schedule.getSchedule() == Schedule.FIXED_PER_THREAD) ? schedule.getCycles() : (int) Math.max(1,
				Math.floor(schedule.getCycles() / threads.size()));
		for (RSSLThread thread : threads)
			if (!thread.dead()) {
				try {
					thread.setFrequency(speed);
					thread.advance();
				} catch (VMException e) {
					debugger.fatal("thread.advance", thread, e);
					System.err.println("Thread died: " + e.generateMessage(this, thread));
					dead.add(thread);
				}
				if (thread.dead())
					dead.add(thread);
			}
		if (dead.size() > 0) {
			for (RSSLThread remove : dead)
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

	public RSSLThread allocateThread(String name, VMFunctionPointer pointer) {
		debugger.trace("machine.allocateThread", name, pointer);
		return new RSSLThread(this, name, global, pointer);
	}

	public void halt() {
		halt = true;
		wait.notifyAll();
	}

}
