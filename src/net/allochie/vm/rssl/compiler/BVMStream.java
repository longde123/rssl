package net.allochie.vm.rssl.compiler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.allochie.vm.rssl.ast.Type;
import net.allochie.vm.rssl.ast.dec.DecType;

public class BVMStream {

	private DataInputStream uis;
	private DataOutputStream uos;

	public BVMStream() {
	}

	public void open(InputStream is) {
		this.uis = new DataInputStream(is);
	}

	public void write(OutputStream os) {
		this.uos = new DataOutputStream(os);
	}

	public byte nextByte() throws IOException {
		return (byte) this.uis.read();
	}

	public void writeByte(byte b) throws IOException {
		this.uos.write(b);
	}

	public int nextInt() throws IOException {
		return this.uis.readInt();
	}

	public void writeInt(int i) throws IOException {
		this.uos.writeInt(i);
	}

	public byte[] nextBytes(int q) throws IOException {
		byte[] data = new byte[q];
		this.uis.read(data);
		return data;
	}

	public void writeBytes(byte[] q) throws IOException {
		this.uos.write(q);
	}

	public void writeString(String s) throws IOException {
		this.uos.write(s.length());
		for (char c : s.toCharArray())
			this.uos.writeChar(c);
	}

	public String readString() throws IOException {
		int size = this.uis.read();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < size; i++)
			builder.append(this.uis.readChar());
		return builder.toString();
	}

	public Opcode nextOp() throws IOException {
		byte dword = nextByte();
		Opcode op = new Opcode();
		op.op = Opcodes.from(dword);
		if (op.op == null)
			throw new BVMStreamException(String.format("Illegal opword: %X", dword));
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
		writeByte(op.op.opword);
		op.writeToStream(this);
	}

	public void writeQWord(QWord word) throws IOException {
		boolean longword = (word.value > 127);
		if (!longword) {
			byte bz = (byte) ((word.value & 0x7F) | ((word.flag_bit) ? 0x80 : 0x00));
			writeByte(bz);
		} else {
			byte[] bar = new byte[5];
			bar[0] = (byte) (0x7F | ((word.flag_bit) ? 0x80 : 0x00));
			bar[1] = (byte) (word.value >> 24);
			bar[2] = (byte) (word.value >> 16);
			bar[3] = (byte) (word.value >> 8);
			bar[4] = (byte) (word.value);
			writeBytes(bar);
		}
	}

}
