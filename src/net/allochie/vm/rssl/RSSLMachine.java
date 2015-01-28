package net.allochie.vm.rssl;

import java.util.ArrayList;
import java.util.HashMap;

import net.allochie.vm.rssl.api.IDebugger;
import net.allochie.vm.rssl.api.ThreadSchedule;
import net.allochie.vm.rssl.api.ThreadSchedule.Schedule;
import net.allochie.vm.rssl.ast.Identifier;
import net.allochie.vm.rssl.debug.VoidDebugger;

/**
 * The main machine container. Includes a list of all known types to the
 * machine, the machine's globals, functions, the top closure, the threads and
 * the thread scheduler.
 * 
 * @author AfterLifeLochie
 *
 */
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

	/**
	 * Creates a new RSSL machine.
	 * 
	 * @param title
	 *            The machine's name.
	 * @param debugger
	 *            The debugger hook.
	 * @param schedule
	 *            The threading schedule object.
	 */
	public RSSLMachine(String title, IDebugger debugger, ThreadSchedule schedule) {
		super(title);
		this.debugger = debugger;
		this.schedule = schedule;
		debugger.trace("machine.init.schedule", schedule);
	}

	/**
	 * Sets the debugger. Any future debug traces are sent to the debugger
	 * immediately.
	 * 
	 * @param what
	 *            The debugger to set to.
	 */
	public void setDebugger(IDebugger what) {
		debugger = what;
	}

	/**
	 * Puts a thread on the machine queue for immediate execution in the next
	 * cycle.
	 * 
	 * @param thread
	 *            The thread to put.
	 */
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

	/**
	 * Steps the machine forward one cycle.
	 */
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

	/**
	 * Finds a function in the machine. If the function doesn't exist and isn't
	 * a native function, the result is null.
	 * 
	 * @param invokeFunc
	 *            The function pointer.
	 * @return The result function or null if no such function exists.
	 */
	public VMFunction findFunction(VMFunctionPointer invokeFunc) {
		if (invokeFunc == null)
			return null;
		if (invokeFunc.functionVal != null)
			return invokeFunc.functionVal;
		return findFunction(invokeFunc.functionName);
	}

	/**
	 * Finds a function in the machine. If the function doesn't exist and isn't
	 * a native function, the result is null.
	 * 
	 * @param identifier
	 *            The identifier.
	 * @return The result function or null if no such function exists.
	 */
	public VMFunction findFunction(Identifier identifier) {
		debugger.trace("machine.findFunction", identifier);
		return findFunction(identifier.image);
	}

	/**
	 * Finds a function in the machine. If the function doesn't exist and isn't
	 * a native function, the result is null.
	 * 
	 * @param name
	 *            The function name.
	 * @return The function or null if no such function exists.
	 */
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

	/**
	 * Find a global variable in the machine.
	 * 
	 * @param identifier
	 *            The identifier.
	 * @return The global variable.
	 */
	public VMVariable findGlobal(Identifier identifier) {
		debugger.trace("machine.findGlobal", identifier);
		return globals.get(identifier.image);
	}

	/**
	 * Allocates a new thread on the machine.
	 * 
	 * @param name
	 *            The thread's name.
	 * @param pointer
	 *            The function pointer.
	 * @return The thread created.
	 */
	public RSSLThread allocateThread(String name, VMFunctionPointer pointer) {
		debugger.trace("machine.allocateThread", name, pointer);
		return new RSSLThread(this, name, global, pointer);
	}

	/**
	 * Halts the machine immediately.
	 */
	public void halt() {
		halt = true;
		wait.notifyAll();
	}

}
