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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.soundpaint.rp2040pio.CmdOptions;
import org.soundpaint.rp2040pio.Constants;
import org.soundpaint.rp2040pio.Decoder;
import org.soundpaint.rp2040pio.IOUtils;
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
    "It is possible to specify other locations for pioasm.%n" +
    "%n" + 
    "If the \"-l\" load option is given, then after assembly,%n" +
    "it it loaded into pio 0, along with wrap and side set %n"+
    "commands for sm 0. To change the target pio/sm instance,%n" +
    "use \"-p\" and \"-s\" options.";

  private static final CmdOptions.StringOptionDeclaration optInput =
    CmdOptions.createStringOption("PATH", false, 'i', "input", null,
                                  "path of .pioasm source file to load");
  private static final CmdOptions.StringOptionDeclaration optOutput =
    CmdOptions.createStringOption("PATH", false, 'o', "output", null,
                                  "path of .hex file to write");
  private static final CmdOptions.StringOptionDeclaration optTool =
    CmdOptions.createStringOption("PATH", false, 't', "tool", null,
                                  "path to pioasm tool, if not on PATH");
  private static final CmdOptions.StringOptionDeclaration optProgram =
    CmdOptions.createStringOption("NAME", false, 'P', "program", null,
                                  "name of program to use, if any");
  private static final CmdOptions.BooleanOptionDeclaration optLoad =
    CmdOptions.createBooleanOption(false, 'l', "load", false,
                                  "If the .pioasm file should be loaded");
  private static final CmdOptions.IntegerOptionDeclaration optPio =
    CmdOptions.createIntegerOption("NUMBER", false, 'p', "pio", null,
                                   "PIO number, either 0 or 1");
  private static final CmdOptions.IntegerOptionDeclaration optSm =
    CmdOptions.createIntegerOption("NUMBER", false, 's', "sm", null,
                                   "SM number, one of 0, 1, 2 or 3");

  private final SDK sdk;

  public Assemble(final PrintStream console, final SDK sdk)
  {
    super(console, fullName, singleLineDescription, notes,
          new CmdOptions.OptionDeclaration<?>[]
          { optInput, optOutput, optTool, optLoad, optProgram, optPio, optSm });
    if (sdk == null) {
      throw new NullPointerException("sdk");
    }
    this.sdk = sdk;
  }


  @Override
  protected void checkValidity(final CmdOptions options)
    throws CmdOptions.ParseException
  {
    if (options.getValue(optHelp) != CmdOptions.Flag.ON) {
	    if (options.getValue(optInput) == null) {
	        throw new CmdOptions.
	          ParseException("input file \"-i\" must be specified");
	    }
      if (options.getValue(optLoad) && options.isDefined(optOutput)) {
        throw new CmdOptions.
          ParseException("output file can't be used with +l/--load");
      }
      if (!options.getValue(optLoad) && options.isDefined(optProgram)) {
        throw new CmdOptions.
          ParseException("program name can only be used with +l/--load");
      }
      if (!options.getValue(optLoad) && (options.isDefined(optPio) || options.isDefined(optSm))) {
        throw new CmdOptions.
          ParseException("PIO/SM number can only be used with +l/--load");
      }
	    if (options.isDefined(optPio)) {
        final int pioNum = options.getValue(optPio);
      	if ((pioNum < 0) || (pioNum > Constants.PIO_NUM - 1)) {
          throw new CmdOptions.
          	ParseException("PIO number must be either 0 or 1");
      	}
	    }
      if (options.isDefined(optSm)) {
        final int smNum = options.getValue(optSm);
        if ((smNum < 0) || (smNum > Constants.SM_COUNT - 1)) {
          throw new CmdOptions.
            ParseException("SM number must be one of 0, 1, 2 or 3");
        }
      }
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
	if (!options.getValue(optLoad))
	{
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
	else
	{
		var jsonOutput = File.createTempFile("rp2040pio", ".json");
		jsonOutput.deleteOnExit();
	    Process p = Runtime.getRuntime().exec(new String[] {pioasm, "-o", "json", input, jsonOutput.getAbsolutePath()});
	    try {
			int exitVal = p.waitFor();
			sdk.getConsole().println("pioasm exited with code " + exitVal);
			if (exitVal != 0)
				return false;
		} catch (InterruptedException e) {
			return false;
		}
		final int pioNum = options.isDefined(optPio) ? options.getValue(optPio) : 0;
		final int smNum = options.isDefined(optSm) ? options.getValue(optSm) : 0;
	    var parser = new JSONParser();
		try(var reader = new FileReader(jsonOutput))
		{
			var data = (JSONObject) parser.parse(reader);
			
			var all_programs = ((JSONArray)data.get("programs"));
			
			String requestedProgram = options.getValue(optProgram);

			if (all_programs.size() != 1 && requestedProgram == null)
			{
				sdk.getConsole().println("pioasm files must have exactly one program when being loaded without -P specified. Please specify -P");
				return false;
			}
			int progindex = 0;
			if (requestedProgram != null)
			{
				int i = 0;
				for (var progr : all_programs)
				{
					if (((JSONObject)progr).get("name").toString().equals(requestedProgram))
					{
						progindex = i;
						break;
					}
					i++;
				}
				if (progindex != i)
				{
					sdk.getConsole().println("pio program name not found");
					return false;
				}	
			}
			// load it. { "progr_name":{"instructions:[{"hex": "...."}]}}
			// load it. {programs: [ { "instructions:[{"hex": "...."}], name: "name"}] }
			var program = (JSONObject) all_programs.get(progindex);
			var program_name = program.get("name").toString();
			String hex = ((Stream<Object>)((JSONArray)program.get("instructions")).stream()).map(x -> ((JSONObject)x).get("hex").toString()).collect(Collectors.joining("\n"));
			
			// TODO: use ProgramParser supported directives?
			hex = "#.program " + program_name + "\n\n" + hex;
			var lineReader = new LineNumberReader(new StringReader(hex));
			var assignedAddress = new Load(sdk.getConsole(), sdk).loadHexDump(pioNum, lineReader, program_name, null);
			// code loaded, now 			
			// Use the JSON output to get set/side set options
			var wrap = new Wrap(sdk.getConsole(), sdk);
			wrap.setWrap(pioNum, smNum, sdk, assignedAddress + (int)(long)(Long)program.get("wrap"));
			wrap.setWrapTarget(pioNum, smNum, sdk, assignedAddress + (int)(long)(Long)program.get("wrapTarget"));
			var sideset_obj = (JSONObject)program.get("sideset");
			var sideset = new SideSet(sdk.getConsole(), sdk);
			sideset.setSideSetCount(pioNum, smNum, sdk, (int)(long)(Long)sideset_obj.get("size"));
			sideset.setSideSetOpt(pioNum, smNum, sdk, (Boolean)sideset_obj.get("optional"));
			sideset.setSideSetPinDirs(pioNum, smNum, sdk, (Boolean)sideset_obj.get("pindirs"));
			
			return true;
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
	}
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
