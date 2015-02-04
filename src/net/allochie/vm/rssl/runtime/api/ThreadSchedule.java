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
	
	public abstract boolean suspend(RSSLThread thread, Object... params);
	public abstract boolean resume(RSSLThread thread);

	@Override
	public String toString() {
		return "ThreadSchedule: " + mode + ", " + cycles + " cycles";
	}
}
