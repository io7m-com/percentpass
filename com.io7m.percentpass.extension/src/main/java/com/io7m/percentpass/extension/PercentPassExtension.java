/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> http://io7m.com
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
    return isAnnotated(context.getTestMethod(), PercentPassing.class);
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
    final ExtensionContext context)
  {
    final var passing =
      context.getRequiredTestMethod()
        .getAnnotation(PercentPassing.class);

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
      String.format(
        "%s:%s",
        context.getRequiredTestClass().getCanonicalName(),
        context.getRequiredTestMethod().getName()
      );

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

  @Override
  public void interceptTestTemplateMethod(
    final Invocation<Void> invocation,
    final ReflectiveInvocationContext<Method> invocationContext,
    final ExtensionContext extensionContext)
    throws Throwable
  {
    final var store =
      extensionContext.getStore(ExtensionContext.Namespace.create(
        PercentPassExtension.class));

    final var name =
      String.format(
        "%s:%s",
        extensionContext.getRequiredTestClass().getCanonicalName(),
        extensionContext.getRequiredTestMethod().getName()
      );

    final var percentContext =
      store.get(name, PercentPassContext.class);

    percentContext.addInvocation();

    try {
      invocation.proceed();
    } catch (final Throwable e) {
      percentContext.addFailure();
      throw e;
    }
  }

  @Override
  public void handleTestExecutionException(
    final ExtensionContext context,
    final Throwable throwable)
  {
    final var store =
      context.getStore(ExtensionContext.Namespace.create(PercentPassExtension.class));

    final var name =
      String.format(
        "%s:%s",
        context.getRequiredTestClass().getCanonicalName(),
        context.getRequiredTestMethod().getName()
      );

    final var percentContext =
      store.get(name, PercentPassContext.class);

    if (percentContext.hasInvokedAll()) {
      final var received = percentContext.successPercent();
      final var expected = percentContext.configuration().passPercent();

      LOG.info(
        "{}: {}% success in {} invocations",
        name,
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
      LOG.error("{}: ", name, throwable);
      throw new TestAbortedException();
    }
  }
}
