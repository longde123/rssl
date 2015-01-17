package net.allochie.vm.rssl.natives;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import net.allochie.vm.rssl.VMException;
import net.allochie.vm.rssl.VMUserCodeException;
import net.allochie.vm.rssl.global.Callout;
import net.allochie.vm.rssl.global.NativeMethod;
import net.allochie.vm.rssl.global.TypeRegistry;

public class NativeDisk {

	public static class FileWrapper {
		public final WeakReference<RandomAccessFile> file;

		public FileWrapper(RandomAccessFile file) {
			this.file = new WeakReference<RandomAccessFile>(file);
		}
	}

	static final NativeDisk diskEnv = new NativeDisk();

	private final ArrayList<RandomAccessFile> files = new ArrayList<RandomAccessFile>();

	private NativeDisk() {
		TypeRegistry.registerTypeWithClass("file", FileWrapper.class);
	}

	private FileWrapper openFile(String what) throws IOException {
		File whatFile = new File(what);
		RandomAccessFile file = new RandomAccessFile(whatFile, "rw");
		files.add(file);
		return new FileWrapper(file);
	}

	private void closeFile(FileWrapper what) throws IOException {
		RandomAccessFile ref = what.file.get();
		if (ref == null)
			return;
		files.remove(ref);
		ref.close();
	}

	@NativeMethod(name = "OpenFile")
	public FileWrapper openFile(Callout what, String filename) throws VMException {
		try {
			return diskEnv.openFile(filename);
		} catch (IOException ioex) {
			throw new VMUserCodeException(what.thread, ioex.getMessage());
		}
	}

	@NativeMethod(name = "CloseFile")
	public void closeFile(Callout what, FileWrapper wrapper) throws VMException {
		try {
			diskEnv.closeFile(wrapper);
		} catch (IOException ioex) {
			throw new VMUserCodeException(what.thread, ioex.getMessage());
		}
	}

	public String readLine(Callout what, FileWrapper wrapper) throws VMException {
		// TODO: Auto-generated method stub
		return "";
	}

	public void writeLine(Callout what, FileWrapper wrapper, String line) throws VMException {
		// TODO: Auto-generated method stub
	}

}
