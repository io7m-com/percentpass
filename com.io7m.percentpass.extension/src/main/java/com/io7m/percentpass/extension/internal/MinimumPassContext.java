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

package com.io7m.percentpass.extension.internal;

import com.io7m.percentpass.extension.MinimumPassing;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import java.util.Objects;

/**
 * The percent pass context.
 */

public final class MinimumPassContext implements TestTemplateInvocationContext
{
  private final MinimumPassing configuration;
  private final PercentPassDisplayNameFormatter formatter;
  private int invocations;
  private int failures;

  /**
   * The percent pass context.
   *
   * @param inConfiguration The configuration
   * @param inFormatter     The formatter
   */

  public MinimumPassContext(
    final MinimumPassing inConfiguration,
    final PercentPassDisplayNameFormatter inFormatter)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.formatter =
      Objects.requireNonNull(inFormatter, "formatter");
  }

  /**
   * @return The percent passing configuration
   */

  public MinimumPassing configuration()
  {
    return this.configuration;
  }

  /**
   * Add a new failure.
   */

  public void addFailure()
  {
    ++this.failures;
  }

  /**
   * @return {@code true} if all tests have been invoked
   */

  public boolean hasInvokedAll()
  {
    return this.invocations == this.configuration.executionCount();
  }

  @Override
  public String getDisplayName(final int invocationIndex)
  {
    return this.formatter.format(
      invocationIndex,
      this.configuration().executionCount());
  }

  /**
   * Add a new invocation.
   */

  public void addInvocation()
  {
    ++this.invocations;
  }

  /**
   * @return The number of successful executions
   */

  public int successCount()
  {
    return this.configuration.executionCount() - this.failures;
  }
}
