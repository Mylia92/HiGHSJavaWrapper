# HiGHS Java Wrapper

The scope of this project is to provide a Java wrapper for the open-source [HiGHS solver](https://highs.dev/), which is
capable of solving linear (LP), mixed-integer (MIP) and quadratic programming (QP) problems.

The project relies on [SWIG](https://swig.org/) to generate the JNI classes to be able to communicate with the HiGHS
shared library file, `libhighs.so`.

Additional wrap functions can be added and help can be provided on demand.

Some examples on how to use the wrapper are provided in `src/test/java/wrapper/model/examples`.

## Dependencies / Prerequisites

### HiGHS

`HiGHS` must be built following [the instructions](https://github.com/ERGO-Code/HiGHS/) on its repository page. Once
`HiGHS`is built, the environment variable `HIGHS_HOME` must be defined.

### Java

A JDK 23 or later is required. `JAVA_HOME` must be defined.

### SWIG

`SWIG` must be installed. On Ubuntu systems, one can use `sudo apt install swig`.

### GCC

`gcc` must be installed. Other compilers could be used. In this case, `generate_jni_classes` should be updated
accordingly.

### Build the JNI classes and shared libraries

To build the JNI classes required by the wrapper, `generate_jni_classes` should be used:

- It builds the JNI classes in `src/main/java/highs`,
- It automatically creates object files in `src/main/java/highs` and copies the shared libraries `libhighs.so` and
  `libhighswrap.so` in the base directory.

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
`UnsatisfiedLinkError` will be thrown. Note that `libhighs.so` must be loaded before `libhighswrap.so`.
