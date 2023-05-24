/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.percentpass.extension;

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.percentpass.extension.internal.MinimumPassContext;
import com.io7m.percentpass.extension.internal.PercentPassContext;
import com.io7m.percentpass.extension.internal.PercentPassDisplayNameFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

/**
 * The main extension.
 */

public final class PercentPassExtension
  implements TestTemplateInvocationContextProvider,
  TestExecutionExceptionHandler, InvocationInterceptor
{
  private static final Logger LOG =
    LoggerFactory.getLogger(PercentPassExtension.class);

  /**
   * Create an extension.
   */

  public PercentPassExtension()
  {

  }

  @Override
  public boolean supportsTestTemplate(
    final ExtensionContext context)
  {
    return isAnnotated(context.getTestMethod(), PercentPassing.class)
      || isAnnotated(context.getTestMethod(), MinimumPassing.class);
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
    final ExtensionContext context)
  {
    return Stream.concat(
      invocationsForPercentPassing(context),
      invocationsForMinimumPassing(context)
    );
  }

  private static Stream<TestTemplateInvocationContext> invocationsForPercentPassing(
    final ExtensionContext context)
  {
    final var passing =
      context.getRequiredTestMethod()
        .getAnnotation(PercentPassing.class);

    if (passing == null) {
      return Stream.of();
    }

    Preconditions.checkPreconditionV(
      Integer.valueOf(passing.executionCount()),
      passing.executionCount() > 1,
      "Execution count must be greater than 1"
    );
    Preconditions.checkPreconditionV(
      Double.valueOf(passing.passPercent()),
      passing.passPercent() > 0.0,
      "Pass percent must be > 0"
    );
    Preconditions.checkPreconditionV(
      Double.valueOf(passing.passPercent()),
      passing.passPercent() <= 100.0,
      "Pass percent must be ≤ 100"
    );

    final var store =
      context.getStore(ExtensionContext.Namespace.create(PercentPassExtension.class));

    final var name =
      createPercentName(context);

    final var existing =
      store.get(name, PercentPassContext.class);
    if (existing != null) {
      throw new IllegalStateException(
        String.format("Context %s already registered", name)
      );
    }

    final var formatter =
      new PercentPassDisplayNameFormatter(context.getDisplayName());
    final var container =
      new PercentPassContext(passing, formatter);

    store.put(name, container);
    return IntStream.rangeClosed(1, passing.executionCount())
      .mapToObj(invocation -> container);
  }

  private static Stream<TestTemplateInvocationContext> invocationsForMinimumPassing(
    final ExtensionContext context)
  {
    final MinimumPassing passing =
      context.getRequiredTestMethod()
        .getAnnotation(MinimumPassing.class);

    if (passing == null) {
      return Stream.of();
    }

    Preconditions.checkPreconditionV(
      Integer.valueOf(passing.executionCount()),
      passing.executionCount() > 1,
      "Execution count must be greater than 1"
    );
    Preconditions.checkPreconditionV(
      Integer.valueOf(passing.passMinimum()),
      passing.passMinimum() > 1,
      "Pass count must be greater than 0"
    );
    Preconditions.checkPreconditionV(
      Integer.valueOf(passing.passMinimum()),
      passing.passMinimum() <= passing.executionCount(),
      "Pass count must be less than or equal to %d",
      Integer.valueOf(passing.executionCount())
    );

    final var store =
      context.getStore(ExtensionContext.Namespace.create(PercentPassExtension.class));

    final var name =
      createMinimumName(context);

    final var existing =
      store.get(name, PercentPassContext.class);
    if (existing != null) {
      throw new IllegalStateException(
        String.format("Context %s already registered", name)
      );
    }

    final var formatter =
      new PercentPassDisplayNameFormatter(context.getDisplayName());
    final var container =
      new MinimumPassContext(passing, formatter);

    store.put(name, container);
    return IntStream.rangeClosed(1, passing.executionCount())
      .mapToObj(invocation -> container);
  }


  @Override
  public void interceptTestTemplateMethod(
    final Invocation<Void> invocation,
    final ReflectiveInvocationContext<Method> invocationContext,
    final ExtensionContext extensionContext)
    throws Throwable
  {
    final var store =
      extensionContext.getStore(
        ExtensionContext.Namespace.create(PercentPassExtension.class));

    final var minimumName =
      createMinimumName(extensionContext);
    final var percentName =
      createPercentName(extensionContext);

    final var percentContext =
      store.get(percentName, PercentPassContext.class);

    if (percentContext != null) {
      percentContext.addInvocation();
    }

    final var minimumContext =
      store.get(minimumName, MinimumPassContext.class);

    if (minimumContext != null) {
      minimumContext.addInvocation();
    }

    try {
      invocation.proceed();
    } catch (final Throwable e) {
      if (percentContext != null) {
        percentContext.addFailure();
      }
      if (minimumContext != null) {
        minimumContext.addFailure();
      }
      throw e;
    }
  }

  @Override
  public void handleTestExecutionException(
    final ExtensionContext context,
    final Throwable throwable)
  {
    final var store =
      context.getStore(
        ExtensionContext.Namespace.create(PercentPassExtension.class));

    checkPercentExecution(context, throwable, store);
    checkMinimumExecution(context, throwable, store);
  }

  private static void checkMinimumExecution(
    final ExtensionContext context,
    final Throwable throwable,
    final ExtensionContext.Store store)
  {
    final var minimumName =
      createMinimumName(context);
    final var minimumContext =
      store.get(minimumName, MinimumPassContext.class);

    if (minimumContext == null) {
      return;
    }

    if (minimumContext.hasInvokedAll()) {
      final var received = minimumContext.successCount();
      final var expected = minimumContext.configuration().passMinimum();

      LOG.info(
        "{}: {} successes in {} invocations",
        minimumName,
        Integer.valueOf(received),
        Integer.valueOf(minimumContext.configuration().executionCount())
      );
      if (received < expected) {
        Assertions.fail(
          new StringBuilder(128)
            .append("Too few test invocations succeeded without an error.")
            .append(System.lineSeparator())
            .append("  Expected: ")
            .append(expected)
            .append(System.lineSeparator())
            .append("  Received: ")
            .append(received)
            .append(System.lineSeparator())
            .toString()
        );
      }
    } else {
      LOG.error("{}: ", minimumName, throwable);
      throw new TestAbortedException();
    }
  }

  private static void checkPercentExecution(
    final ExtensionContext context,
    final Throwable throwable,
    final ExtensionContext.Store store)
  {
    final var percentName =
      createPercentName(context);
    final var percentContext =
      store.get(percentName, PercentPassContext.class);

    if (percentContext == null) {
      return;
    }

    if (percentContext.hasInvokedAll()) {
      final var received = percentContext.successPercent();
      final var expected = percentContext.configuration().passPercent();

      LOG.info(
        "{}: {}% success in {} invocations",
        percentName,
        Double.valueOf(received),
        Integer.valueOf(percentContext.configuration().executionCount())
      );
      if (received < expected) {
        Assertions.fail(
          new StringBuilder(128)
            .append("Too few test invocations succeeded without an error.")
            .append(System.lineSeparator())
            .append("  Expected: ")
            .append(expected)
            .append("%")
            .append(System.lineSeparator())
            .append("  Received: ")
            .append(received)
            .append("%")
            .append(System.lineSeparator())
            .toString()
        );
      }
    } else {
      LOG.error("{}: ", percentName, throwable);
      throw new TestAbortedException();
    }
  }

  private static String createPercentName(
    final ExtensionContext context)
  {
    return String.format(
      "Percent %s:%s",
      context.getRequiredTestClass().getCanonicalName(),
      context.getRequiredTestMethod().getName()
    );
  }

  private static String createMinimumName(
    final ExtensionContext extensionContext)
  {
    return String.format(
      "Minimum %s:%s",
      extensionContext.getRequiredTestClass().getCanonicalName(),
      extensionContext.getRequiredTestMethod().getName()
    );
  }
}
