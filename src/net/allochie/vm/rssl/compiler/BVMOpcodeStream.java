package net.allochie.vm.rssl.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BVMOpcodeStream {

	private InputStream uis;
	private OutputStream uos;

	public BVMOpcodeStream() {
	}

	public void open(InputStream is) {
		this.uis = is;
	}

	public void write(OutputStream os) {
		this.uos = os;
	}

	public byte nextOp() throws IOException {
		return (byte) this.uis.read();
	}

	public void writeOp(byte op) throws IOException {
		this.uos.write(op);
	}

}
