package net.allochie.vm.rssl.runtime.api;

import net.allochie.vm.rssl.runtime.RSSLThread;

public abstract class ThreadSchedule {

	public static enum Schedule {
		/**
		 * A fixed number of cycles will be allocated evenly between all threads
		 * per update.
		 */
		FIXED_PER_MACHINE,
		/**
		 * A fixed number of cycles will be stepped for each thread per update.
		 */
		FIXED_PER_THREAD
	}

	/** The current schedule mode */
	private Schedule mode;
	/** The current cycle count */
	private int cycles;

	public ThreadSchedule(Schedule mode, int cycles) {
		this.mode = mode;
		this.cycles = cycles;
	}

	public void setMode(Schedule mode) {
		this.mode = mode;
	}

	public Schedule getSchedule() {
		return mode;
	}

	public void setCycles(int cycles) {
		this.cycles = cycles;
	}

	public int getCycles() {
		return cycles;
	}

	/**
	 * Called by the system when a thread is suspended by the user or by the
	 * system. The thread object is provided and a list of parameters is passed
	 * to the callee method. The callee method decides on whether the thread
	 * should be suspended by returuning a boolean state indicating if the
	 * thread is now suspended (true) or if the thread should not be suspended
	 * (false).
	 * 
	 * @param thread
	 *            The thread which has been requested to be suspended
	 * @param params
	 *            The parameters provided from the suspend call
	 * @return If the thread should be suspended
	 */
	public abstract boolean suspend(RSSLThread thread, Object... params);

	/**
	 * Called by the system before a suspended thread is updated. The callee
	 * must return a boolean which indicates if the thread should be resumed
	 * (true) or if the thread should remain suspended (false). If the thread is
	 * resumed, the thread will be returned to the active thread queue and will
	 * be advanced.
	 * 
	 * @param thread
	 *            The thread which has been suspended
	 * @return If the thread should be resumed
	 */
	public abstract boolean resume(RSSLThread thread);

	@Override
	public String toString() {
		return "ThreadSchedule: " + mode + ", " + cycles + " cycles";
	}
}
