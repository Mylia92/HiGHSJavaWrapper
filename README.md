# HiGHS Java Wrapper

The scope of this project is to provide a Java wrapper for the open-source [HiGHS solver](https://highs.dev/), which is
capable of solving linear (LP), mixed-integer (MIP) and quadratic programming (QP) problems.

The project relies on [SWIG](https://swig.org/) to generate the JNI classes to be able to communicate with the HiGHS
shared library file, `libhighs.so`.

Additional wrap functions can be added and help can be provided on demand.

Some examples on how to use the wrapper are provided in `src/test/java/wrapper/model/examples`.

## Dependencies / Prerequisites

### HiGHS

`HiGHS` must be built following [the instructions](https://github.com/ERGO-Code/HiGHS/) on its repository page. For now,
the wrapper expects version `v1.12.0`.

Note that the wrapper is currently incompatible with the HiGHS compilation option `HIGHSINT64=on`.

### Java

A JDK 23 or later is required.

### SWIG

`SWIG` [must be installed](https://www.swig.org/). On Ubuntu systems, one can use `sudo apt install swig`.

### Compiler

`gcc` or `clang` must be installed. Note that `HiGHS` must have been installed with the same compiler. The environment
variables `CC` or `CXX` must be defined.

### Build the JNI classes

To build the JNI classes required by the wrapper, `generate_jni_classes` should be used. It builds the JNI classes in
`src/main/java/highs`,

### Build the shared libraries

`generate_shared_libraries` should be used to build the shared libraries required by the wrapper. It automatically
creates the required shared libraries, `libhighs.so` and
`libhighswrap.so`, in the base directory.

The following environment variables must be defined for the script to work:

- `HIGHS_HOME`,
- `JAVA_HOME`.

## Use

To run the tests or use the wrapper for another project, the JVM argument `-Djava.library.path` must be filled. The
referred path must contain `libhighs.so` and `libhighswrap.so`. The relevant classes (relying on calls to
`HiGHS`) then must also contain (or something equivalent):

```
    static {
        System.loadLibrary("highs");
        System.loadLibrary("highswrap");
    }
```

If the shared libraries `libhighs.so` and `libhighswrap.so` cannot be found at run time, then exceptions of type
`UnsatisfiedLinkError` or type `ClassNotFound` will be thrown. Note that `libhighs.so` must be loaded before
`libhighswrap.so`.
