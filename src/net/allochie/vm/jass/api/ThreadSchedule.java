package net.allochie.vm.jass.api;

public class ThreadSchedule {

	public static enum Schedule {
		/**
		 * A fixed number of cycles will be allocated evenly between all threads
		 * per update.
		 */
		FIXED,
		/**
		 * A fixed number of cycles will be stepped for each thread per update.
		 */
		FIXED_PER_THREAD
	}

	/** The current schedule mode */
	private Schedule mode;
	/** The current cycle count */
	private int cycles;

	public void ThreadSchedule(Schedule mode, int cycles) {
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
}