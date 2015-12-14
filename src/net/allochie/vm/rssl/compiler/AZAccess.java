package net.allochie.vm.rssl.compiler;

public class AZAccess {

	public AZAccessType mode;
	public QWord value;

	public AZAccess(QWord word) {
		value = word;
	}

}
