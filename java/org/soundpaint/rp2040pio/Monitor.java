/*
 * @(#)Monitor.java 1.00 21/02/02
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
package org.soundpaint.rp2040pio;

import java.io.IOException;
import org.soundpaint.rp2040pio.sdk.PIOSDK;
import org.soundpaint.rp2040pio.sdk.Program;
import org.soundpaint.rp2040pio.sdk.ProgramParser;

/**
 * Program Execution Monitor And Control
 */
public class Monitor
{
  private static final String about =
    "RP2040 PIO Emulator V0.1\n" +
    "\n" +
    "© 2021 by J. Reuter\n" +
    "Karlsruhe, Germany\n";

  private final PIO pio;
  private final PIOSDK pioSdk;

  public Monitor()
  {
    pio = PIO.PIO0;
    pioSdk = new PIOSDK(pio);
    System.out.println(about);
  }

  public void addProgram(final String programResourcePath)
    throws IOException
  {
    final Program program = ProgramParser.parse(programResourcePath);
    pioSdk.addProgram(program);
  }

  public void dumpProgram()
  {
    pio.getSM(0).dumpMemory();
  }

  public void setSideSetCount(final int count)
  {
    pio.setSideSetCount(count);
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
