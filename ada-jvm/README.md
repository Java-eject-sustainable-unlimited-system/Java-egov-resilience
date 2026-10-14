# Ada JVM

This is a separate Ada project alongside the existing Java watchdog. It is an
early JVM implementation project, **not** a Java 25-compatible JVM yet.

## Current prototype

The prototype interprets a small subset of JVM method bytecode. It supports
`iconst_m1` through `iconst_5`, `bipush`, `iadd`, `isub`, `imul`, `idiv`, and
`ireturn`. It can also parse a Java class file and extract a selected static
method's `Code` attribute before interpreting it. Class loading, method
invocation, and the JVM runtime or Java standard library are not implemented.

The interpreter uses a bounded operand stack and Java `int` arithmetic
semantics for the supported operations. Input files are raw method-code bytes,
not Java class files, and are limited to 64 KiB by the command-line runner.

## Build and run

Install GNAT and `gprbuild`, then from this directory:

```sh
gprbuild -P ada_jvm.gpr
./build/bin/ada_jvm --self-test
./build/bin/ada_jvm method.code
```

`method.code` must contain a supported instruction sequence ending in
`ireturn`. To execute a supported method from a class file, provide its name
and JVM descriptor, for example:

```sh
./build/bin/ada_jvm --class-file Example.class calculate '()I'
```

Class files are limited to 16 MiB, and method bytecode to 65535 bytes. The
reader accepts class-file versions 45 through 69 and extracts static methods
only. It does not verify bytecode, resolve classes, or execute the complete JVM
instruction set. The runner prints the returned integer. `--self-test` runs
built-in interpreter checks.

## Java 25 compatibility roadmap

Full Java 25 compatibility is a substantial runtime implementation, not a
single feature. Beyond this initial class-file reader, the prototype will need
verification, class loading and linking, the complete instruction set and
runtime data areas, exceptions, object and array semantics, native/platform
interfaces, garbage collection, and the Java 25 standard library. These are
not implemented here. Each milestone should add conformance tests before
claiming support.
