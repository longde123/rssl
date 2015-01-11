package net.allochie.vm.rssl.tests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import net.allochie.vm.rssl.RSSLMachine;
import net.allochie.vm.rssl.RSSLThread;
import net.allochie.vm.rssl.VMException;
import net.allochie.vm.rssl.VMFunctionPointer;
import net.allochie.vm.rssl.api.IDebugger;
import net.allochie.vm.rssl.api.ThreadSchedule;
import net.allochie.vm.rssl.api.ThreadSchedule.Schedule;
import net.allochie.vm.rssl.ast.JASSFile;
import net.allochie.vm.rssl.debug.AllDebugger;
import net.allochie.vm.rssl.global.NativeMethodRegistry;
import net.allochie.vm.rssl.global.TypeRegistry;
import net.allochie.vm.rssl.natives.NativeThreading;
import net.allochie.vm.rssl.natives.NativeTypeCasts;
import net.allochie.vm.rssl.natives.NativeUtils;
import net.allochie.vm.rssl.parser.RSSLParser;
import net.allochie.vm.rssl.parser.ParseException;
import net.allochie.vm.rssl.types.BooleanFuncWrapper;

public class RSSLTest {

	public static void main(String[] args) {
		try {
			RSSLParser parse = new RSSLParser(new FileInputStream("rt.jass"));
			JASSFile file = parse.file();

			NativeMethodRegistry.registerNativeMethodProvider(NativeMethodDemo.class);

			TypeRegistry.registerTypeWithClass("boolexpr", BooleanFuncWrapper.class);
			NativeMethodRegistry.registerNativeMethodProvider(BooleanFuncWrapper.class);

			TypeRegistry.registerTypeWithClass("thread", RSSLThread.class);
			NativeMethodRegistry.registerNativeMethodProvider(NativeThreading.class);
			NativeMethodRegistry.registerNativeMethodProvider(NativeTypeCasts.class);
			NativeMethodRegistry.registerNativeMethodProvider(NativeUtils.class);

			ThreadSchedule schedule = new ThreadSchedule(Schedule.FIXED_PER_MACHINE, 10);
			IDebugger debug = new AllDebugger();
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
