jass-java
=========

jass-java is a pure Java virtual-machine for a custom language, based on [JASS (Just Another Scripting Syntax)]
(http://en.wikipedia.org/wiki/JASS) by Blizzard Entertainment. JASS is, of course, the property of Blizzard 
Entertainment.

jass-java uses many of the syntatic patterns from the original JASS specification. jass-java includes several syntax 
and behavior changes to make it more flexible and to increase the usability of the language.

Differences between jass and jass-java
--------------------------------------

* jass-java allows `handle`s to be cast upwards (ie, from `file` to `handle`).
* jass-java allows `handle`s to be cast back to their type if the type is actually what is specified (ie, `handle` 
  to `file` but only when the value is actually of type `file`).
* jass-java and JASS don't allow native types to be converted by casting. They must be converted using native functions.
* jass-java allows functions to be redeclared, including `native` functions.
* The use of `constant` has a different meaning in jass-java. `constant` in JASS meant that only other constant 
  functions or globals could be used in a constant scope. `constant` in jass-java means that variables, functions or 
  globals may not be redeclared or have their value changed.
* jass-java allows `code` and `handle` arrays
* jass-java has no restriction on the size of an array

Native Java & jass-java interoperability
----------------------------------------

### Types and values
jass-java allows Java objects to interact with JASS scripts and allows JASS scripts to work with Java objects as if 
they were JASS native values.

jass-java represents all non-JASS native types as `handle`. Programmers whom are providing objects to JASS should 
notify the `TypeRegistry` of their object's class and the name of the type so that jass-java can allow the user to 
perform operations on the type (as if it were being accessed in Java).

If jass-java does not know the native name of an object's type provided, jass-java will treat the object as a `handle` 
and will not allow the user to cast the object or perform actions with the `handle` unless functions in JASS permit 
the passing of `handle`-typed values.

### Native methods
jass-java allows Java classes to provide native functions to jass-java VM's. Native functions behave as one would 
expect - they represent references to JVM methods. jass-java requires the user know about the native function's 
signature. The user must have declared the native function in their source before a native function can be used. The 
native function signature is not immutable.

When the user invokes a native function, the VM asks the `NativeMethodRegistry` for a JVM method which matches the 
name and signature of the invoked method. If the method's signature matches, the method will be invoked.

If a native method matching the signature cannot be found, an exception will be generated and passed to the user's 
script.

If a native method throws an exception, the exception will be passed to the user. If the native method returns a value, 
the value will be transformed to a value which jass-java can pass back to the user's script.

The `NativeMethodRegistry` provides an `registerNativeMethodProvider` method, which allows developers to 
register classes which provide methods with the `NativeMethod` annotation attached. Native methods must accept at 
least one parameter; a `Callout` object is provided to the native method so that the method can read and write local 
variables or access the virtual machine's instance.

Stack frames and Closures
-------------------------

jass-java uses `Closure`s to contain it's upvalues. The order of reference in a Closure is as follows:

1. The local closure (the function scope)
2. The run closure (the thread scope)
3. The global closure (the globals)

All statements and expressions are evaluated into one or more sub-types of the `VMStackFrame` class. The use of 
stack-frames in such a manner allows the virtual machine to step forwards with absolute precision and allows the 
interruption of stepping without the use of Java threads.

User code cannot access the stack frame of the current thread. Java code can read and write to the stack frame of any 
thread, but any such behavior may result in unexpected and unspecified behavior.

Threading
---------
jass-java allows the creation, destruction and running of user threads. User threads consist of functions which take 
`nothing` and return `nothing`. User threads are executed alongside the main thread until either:

1. The thread ends gracefully;
2. The code in the thread throws an exception; or
3. The VM decides that it is no longer possible or viable to run the thread (such as consuming too many resources, 
   being interrupted or cancelled by another thread or taking too much 'cpu time').

Any thread may create a child thread and commit it to the thread heap, and any thread can cancel the execution of any 
thread - with the exception of the main thread. The user can also ask the system for the status of any thread object.

Once a thread has ended, it (and it's object representation) may not be restarted.

### Thread safety

jass-java has no native concept of thread safety. Reading and writing variables should be considered an atomic 
(immediate) operation and thus any changes to values take place instantaneously.

As only one thread actually executes concurrently, there is no requirement for common synchronization objects (mutex 
or otherwise). Programmers working in threaded contexts should be aware of the state of their objects (ie, not 
accessing null variables, etc).