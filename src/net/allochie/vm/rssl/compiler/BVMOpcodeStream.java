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
		if (op.op == null)
			throw new BVMOpcodeStreamException(String.format("Illegal opword: %X", dword));
		op.readFromStream(this);
		return op;
	}

	public QWord nextQWord() throws IOException {
		QWord word = new QWord();
		byte dword = (byte) this.uis.read();
		word.flag_bit = (dword & 0x80) != 0;
		word.value = (dword & 0x7F);
		if (word.value == 0x7F) {
			byte[] blob = new byte[4];
			this.uis.read(blob);
			word.value = (blob[0] << 24) & 0xFF000000 | (blob[1] << 16) & 0x00FF0000 | (blob[2] << 8) & 0x0000FF00
					| (blob[3] << 0) & 0x000000FF;
		}
		return word;
	}

	public void writeOp(Opcode op) throws IOException {
		this.uos.write(op.op.opword);
		op.writeToStream(this);
	}

	public void writeQWord(QWord word) throws IOException {
		boolean longword = (word.value > 127);
		if (!longword) {
			byte bz = (byte) ((word.value & 0x7F) | ((word.flag_bit) ? 0x80 : 0x00));
			this.uos.write(bz);
		} else {
			byte[] bar = new byte[5];
			bar[0] = (byte) (0x7F | ((word.flag_bit) ? 0x80 : 0x00));
			bar[1] = (byte) (word.value >> 24);
			bar[2] = (byte) (word.value >> 16);
			bar[3] = (byte) (word.value >> 8);
			bar[4] = (byte) (word.value);
			this.uos.write(bar);
		}

	}

}
