/*
 * @(#)Unassemble.java 1.00 21/03/28
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
package org.soundpaint.rp2040pio.monitor.commands;

import java.io.IOException;
import java.io.PrintStream;
import org.soundpaint.rp2040pio.CmdOptions;
import org.soundpaint.rp2040pio.Constants;
import org.soundpaint.rp2040pio.Decoder;
import org.soundpaint.rp2040pio.PIOEmuRegisters;
import org.soundpaint.rp2040pio.PIORegisters;
import org.soundpaint.rp2040pio.monitor.Command;
import org.soundpaint.rp2040pio.sdk.PIOSDK;
import org.soundpaint.rp2040pio.sdk.SDK;

/**
 * Monitor command "assemble" compiles .pio or .pioasm files
 * into .hex files by utilizing the external pioasm tool 
 * (must be installed seperately) 
 */
public class Assemble extends Command
{
  private static final String fullName = "assemble";
  private static final String singleLineDescription =
    "assemble pio instructions to a hex file for loading";
  private static final String notes =
    "Note that this command assumes that pioasm is installed%n" +
    "on your PATH and simply shells out to it for now.%n" +
    "It is possible to specify other locations for pioasm.";

  private static final CmdOptions.StringOptionDeclaration optInput =
    CmdOptions.createStringOption("PATH", true, 'i', "input", null,
                                  "path of .pioasm source file to load");
  private static final CmdOptions.StringOptionDeclaration optOutput =
    CmdOptions.createStringOption("PATH", false, 'o', "output", null,
                                  "path of .hex file to write");
  private static final CmdOptions.StringOptionDeclaration optTool =
    CmdOptions.createStringOption("PATH", false, 't', "tool", null,
                                  "path to pioasm tool, if not on PATH");

  private final SDK sdk;

  public Assemble(final PrintStream console, final SDK sdk)
  {
    super(console, fullName, singleLineDescription, notes,
          new CmdOptions.OptionDeclaration<?>[]
          { optInput, optOutput, optTool });
    if (sdk == null) {
      throw new NullPointerException("sdk");
    }
    this.sdk = sdk;
  }


  @Override
  protected void checkValidity(final CmdOptions options)
    throws CmdOptions.ParseException
  {
    if (options.getValue(optInput) == null) {
        throw new CmdOptions.
          ParseException("input file \"-i\" must be specified");
    }
  }

  /**
   * Returns true if no error occurred and the command has been
   * executed.
   */
  @Override
  protected boolean execute(final CmdOptions options) throws IOException
  {
	// pioasm -o hex input output
	String pioasm = options.getValue(optTool);
	if (pioasm == null)
		pioasm = "pioasm";
	String input = options.getValue(optInput);
	String output = options.getValue(optOutput);
	if (output == null || output.isEmpty())
	{
		if (input.toUpperCase().endsWith(".PIO") || input.toUpperCase().endsWith(".ASM") || input.toUpperCase().endsWith(".PIOASM"))
			output = input.substring(0,input.lastIndexOf(".")) + ".hex";
		else
			output = input + ".hex";
	}
	sdk.getConsole().println("Assembling PIO code to " + output);
    Process p = Runtime.getRuntime().exec(new String[] {pioasm, "-o", "hex", input, output});
    try {
		int exitVal = p.waitFor();
		sdk.getConsole().println("pioasm exited with code " + exitVal);
		return exitVal == 0;
	} catch (InterruptedException e) {
		return false;
	}
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
