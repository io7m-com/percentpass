percentpass
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.percentpass/com.io7m.percentpass.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.percentpass%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/com.io7m.percentpass/com.io7m.percentpass?server=https%3A%2F%2Fs01.oss.sonatype.org&style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/percentpass/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/percentpass.svg?style=flat-square)](https://codecov.io/gh/io7m-com/percentpass)
![Java Version](https://img.shields.io/badge/21-java?label=java&color=e6c35c)

![com.io7m.percentpass](./src/site/resources/percentpass.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/percentpass/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/percentpass/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/percentpass/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/percentpass/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/percentpass/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/percentpass/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/percentpass/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/percentpass/actions?query=workflow%3Amain.windows.temurin.lts)|


# percentpass

A minimal [Junit 5](https://junit.org/junit5/) extension that allows for
executing tests multiple times, and requiring a minimum number of successful
executions.

This can be useful when a particular test in a test suite is timing sensitive
and cannot be reliably corrected for some reason. Perhaps you decide that
working correctly 98% of the time is acceptable.

### Features

  * Percentage passing; specify that a percentage of the iterations of a test must succeed.
  * Minimum passing: specify that an integral number of the iterations of a test must succeed.
  * Written in pure Java 21.
  * [OSGi](https://www.osgi.org/) ready
  * [JPMS](https://en.wikipedia.org/wiki/Java_Platform_Module_System) ready
  * ISC license
  * High-coverage automated test suite

### Usage

Annotate tests with `@MinimumPassing` or `@PercentPassing`:


```
  @PercentPassing(executionCount = 1000, passPercent = 98.0)
  public void testPercentOK()
  {
    // Do something that fails 1% of the time.
  }

  @MinimumPassing(executionCount = 1000, passMinimum = 980)
  public void testMinimumOK()
  {
    // Do something that fails 1% of the time.
  }
```

