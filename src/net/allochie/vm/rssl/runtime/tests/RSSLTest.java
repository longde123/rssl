package net.allochie.vm.rssl.runtime.tests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import net.allochie.vm.rssl.ast.RSSLFile;
import net.allochie.vm.rssl.parser.ParseException;
import net.allochie.vm.rssl.parser.RSSLParser;
import net.allochie.vm.rssl.runtime.RSSLMachine;
import net.allochie.vm.rssl.runtime.RSSLThread;
import net.allochie.vm.rssl.runtime.VMException;
import net.allochie.vm.rssl.runtime.api.IDebugger;
import net.allochie.vm.rssl.runtime.api.NativeMethodRegistry;
import net.allochie.vm.rssl.runtime.api.ThreadSchedule;
import net.allochie.vm.rssl.runtime.api.TypeRegistry;
import net.allochie.vm.rssl.runtime.api.ThreadSchedule.Schedule;
import net.allochie.vm.rssl.runtime.api.debug.VoidDebugger;
import net.allochie.vm.rssl.runtime.natives.BooleanFuncWrapper;
import net.allochie.vm.rssl.runtime.natives.NativeThreading;
import net.allochie.vm.rssl.runtime.natives.NativeTypeCasts;
import net.allochie.vm.rssl.runtime.natives.NativeUtils;
import net.allochie.vm.rssl.runtime.value.VMFunctionPointer;

public class RSSLTest {

	public static void main(String[] args) {
		try {
			RSSLParser parse = new RSSLParser(new FileInputStream("rt.jass"));
			RSSLFile file = parse.file();

			NativeMethodRegistry.registerNativeMethodProvider(NativeMethodDemo.class);

			TypeRegistry.registerTypeWithClass("boolexpr", BooleanFuncWrapper.class);
			NativeMethodRegistry.registerNativeMethodProvider(BooleanFuncWrapper.class);

			TypeRegistry.registerTypeWithClass("thread", RSSLThread.class);
			NativeMethodRegistry.registerNativeMethodProvider(NativeThreading.class);
			NativeMethodRegistry.registerNativeMethodProvider(NativeTypeCasts.class);
			NativeMethodRegistry.registerNativeMethodProvider(NativeUtils.class);

			ThreadSchedule schedule = new ThreadSchedule(Schedule.FIXED_PER_MACHINE, 10);
			IDebugger debug = new VoidDebugger();
			RSSLMachine machine = new RSSLMachine("Demo machine", debug, schedule);
			try {
				RSSLThread main = machine.allocateThread("main", new VMFunctionPointer("main"));
				main.doFile(file);
				main.runThread();
				machine.putThread(main);
				machine.start();
			} catch (VMException e) {
				e.printStackTrace();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
