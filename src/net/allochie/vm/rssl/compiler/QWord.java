package net.allochie.vm.rssl.compiler;

public class QWord {

	public boolean flag_bit;
	public int value;

	public QWord() {
	}

	public QWord(int value, boolean reg) {
		this.value = value;
		this.flag_bit = reg;
	}
}
