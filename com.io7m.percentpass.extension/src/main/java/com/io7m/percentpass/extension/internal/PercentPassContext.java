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

package com.io7m.percentpass.extension.internal;

import com.io7m.percentpass.extension.PercentPassing;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import java.util.Objects;

public final class PercentPassContext implements TestTemplateInvocationContext
{
  private final PercentPassing configuration;
  private final PercentPassDisplayNameFormatter formatter;
  private int invocations;
  private int failures;

  public PercentPassContext(
    final PercentPassing inConfiguration,
    final PercentPassDisplayNameFormatter inFormatter)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.formatter =
      Objects.requireNonNull(inFormatter, "formatter");
  }

  public PercentPassing configuration()
  {
    return this.configuration;
  }

  public PercentPassDisplayNameFormatter formatter()
  {
    return this.formatter;
  }

  public void addFailure()
  {
    ++this.failures;
  }

  public double successPercent()
  {
    final var success = this.configuration.executionCount() - this.failures;
    return 100.0 * ((double) success / (double) this.invocations);
  }

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

  public void addInvocation()
  {
    ++this.invocations;
  }
}
