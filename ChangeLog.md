# 1.02.02 #
  * ([issue 3](https://code.google.com/p/flaka/issues/detail?id=3)) `<let />` to overwrite user properties. Patched by gr.
  * ([issue 8](https://code.google.com/p/flaka/issues/detail?id=8)) EL-function `matches()` added
  * ([issue 9](https://code.google.com/p/flaka/issues/detail?id=9)) EL-function `glob()` added

# 1.02.01 #
  * ([issue 5](https://code.google.com/p/flaka/issues/detail?id=5)) rebuilding flaka with Ant 1.8 to avoid PropertyHelper12.class problem. No changes in source code. Patch counter bumped to avoid confusion.
  * build.xml: updated to generate Java 1.5 compatible code
  * build.xml: use package-dist to generate a release build. Ensures that 1.8 is used. Would be better though to test that all class files are packaged.

# 1.02.00 #
  * updated to JUEL 2.2.1 (was 2.1.1-rc4)
  * using ant-antunit (1.1) to run Ant based unit tests
  * refactoring ant unit tests (test/test-`*`.xml)
  * `<switch />` redesigned to have elements re, glob, cmp and matches, attributes find, not and literally (legacy, deprecated)
  * fixed bug in `<switch/matches[glob]/>` to avoid partial matches.
  * new distributable `ant-flaka-bare-${version}.jar` introduced - does not contain any inlined dependencies (as ant-flaka-${version}.jar) does.
  * `<trycatch />` extended with `<else />` clause (warning: conflicting exceptions)

# 1.01.01 #
  * [issue 1](https://code.google.com/p/flaka/issues/detail?id=1), remove properties from all property handlers.

# 1.00.00 #
  * initial version (withdrawn)