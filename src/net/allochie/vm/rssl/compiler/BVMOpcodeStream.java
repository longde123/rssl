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

	public Opcode nextOp() throws IOException {
		byte dword = (byte) this.uis.read();
		Opcode op = new Opcode();
		op.op = Opcodes.from(dword);
		
		
		
		return op;
	}

	public void writeOp(Opcode op) throws IOException {
		this.uos.write(op.op.opword);
		
	}

}
