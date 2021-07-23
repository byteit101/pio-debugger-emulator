/*
 * @(#)BitSignal.java 1.00 21/02/12
 *
 * Copyright (C) 2021 Jürgen Reuter
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * For updates and more info or contacting the author, visit:
 * <https://github.com/soundpaint/rp2040pio>
 *
 * Author's web site: www.juergen-reuter.de
 */
package org.soundpaint.rp2040pio.observer.diagram;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.soundpaint.rp2040pio.Bit;
import org.soundpaint.rp2040pio.sdk.SDK;

public class BitSignal extends ValuedSignal<Bit>
{
  private static final double SIGNAL_HEIGHT = 16.0;

  private final SDK sdk;
  private final int address;
  private final int bit;

  public BitSignal(final SDK sdk,
                   final String label,
                   final int address,
                   final int bit)
  {
    this(sdk, label, address, bit, null);
  }

  public BitSignal(final SDK sdk,
                   final String label,
                   final int address,
                   final int bit,
                   final Supplier<Boolean> changeInfoGetter)
  {
    super(label, changeInfoGetter);
    Objects.requireNonNull(sdk);
    this.sdk = sdk;
    this.address = address;
    this.bit = bit;
  }

  public int getAddress()
  {
    return address;
  }

  public int getBit()
  {
    return bit;
  }

  @Override
  protected Bit sampleValue() throws IOException
  {
    return Bit.fromValue(sdk.readAddress(address, bit, bit));
  }

  public Boolean asBoolean(final int cycle)
  {
    final Bit value = getValue(cycle);
    return value != null ? (value == Bit.HIGH) : null;
  }

  @Override
  public double getDisplayHeight()
  {
    return SIGNAL_HEIGHT + 16.0;
  }

  @Override
  public void paintCycle(final List<ToolTip> toolTips,
                         final Graphics2D g, final double zoom,
                         final double xStart, final double yBottom,
                         final int cycle,
                         final boolean firstCycle, final boolean lastCycle)
  {
    if (!next(cycle - 1)) return;
    final double xStable = xStart + Constants.SIGNAL_SETUP_X;
    final double xStop = xStart + zoom;
    final double yStable =
      yBottom - (asBoolean(cycle) ? SIGNAL_HEIGHT : 0.0);
    final double yPrev =
      firstCycle ?
      yStable :
      yBottom - (asBoolean(cycle - 1) ? SIGNAL_HEIGHT : 0.0);
    g.draw(new Line2D.Double(xStart, yPrev, xStable, yStable));
    g.draw(new Line2D.Double(xStable, yStable, xStop, yStable));
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
