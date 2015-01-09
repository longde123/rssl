package net.allochie.vm.jass.tests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import net.allochie.vm.jass.JASSMachine;
import net.allochie.vm.jass.JASSThread;
import net.allochie.vm.jass.VMException;
import net.allochie.vm.jass.VMFunctionPointer;
import net.allochie.vm.jass.api.IDebugger;
import net.allochie.vm.jass.api.ThreadSchedule;
import net.allochie.vm.jass.api.ThreadSchedule.Schedule;
import net.allochie.vm.jass.ast.JASSFile;
import net.allochie.vm.jass.debug.AllDebugger;
import net.allochie.vm.jass.global.NativeMethodRegistry;
import net.allochie.vm.jass.global.TypeRegistry;
import net.allochie.vm.jass.natives.NativeThreading;
import net.allochie.vm.jass.natives.NativeTypeCasts;
import net.allochie.vm.jass.parser.JASSParser;
import net.allochie.vm.jass.parser.ParseException;
import net.allochie.vm.jass.types.BooleanFuncWrapper;

public class JASSTest {

	public static void main(String[] args) {
		try {
			JASSParser parse = new JASSParser(new FileInputStream("rt.jass"));
			JASSFile file = parse.file();

			NativeMethodRegistry.registerNativeMethodProvider(NativeMethodDemo.class);

			TypeRegistry.registerTypeWithClass("boolexpr", BooleanFuncWrapper.class);
			NativeMethodRegistry.registerNativeMethodProvider(BooleanFuncWrapper.class);

			TypeRegistry.registerTypeWithClass("thread", JASSThread.class);
			NativeMethodRegistry.registerNativeMethodProvider(NativeThreading.class);
			NativeMethodRegistry.registerNativeMethodProvider(NativeTypeCasts.class);

			ThreadSchedule schedule = new ThreadSchedule(Schedule.FIXED_PER_MACHINE, 10);
			IDebugger debug = new AllDebugger();
			JASSMachine machine = new JASSMachine("Demo machine", debug, schedule);
			try {
				JASSThread main = machine.allocateThread("main", new VMFunctionPointer("main"));
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
