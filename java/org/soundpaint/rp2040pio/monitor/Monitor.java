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
package org.soundpaint.rp2040pio.monitor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import org.soundpaint.rp2040pio.Constants;
import org.soundpaint.rp2040pio.CmdOptions;
import org.soundpaint.rp2040pio.IOUtils;
import org.soundpaint.rp2040pio.PIOEmuRegisters;
import org.soundpaint.rp2040pio.RegisterClient;
import org.soundpaint.rp2040pio.sdk.GPIOSDK;
import org.soundpaint.rp2040pio.sdk.Panic;
import org.soundpaint.rp2040pio.sdk.PIOSDK;
import org.soundpaint.rp2040pio.sdk.SDK;
import org.soundpaint.rp2040pio.sdk.Program;
import org.soundpaint.rp2040pio.sdk.ProgramParser;

/**
 * Program Execution Monitor And Control
 */
public class Monitor
{
  private static final String PROGRAM_ID = "Monitor Control Program";
  private static final String PRG_NAME = "Monitor";
  private static final String PRG_ID_AND_VERSION =
    "Emulation Monitor Version 0.1 for " + Constants.getProgramAndVersion();
  private static final CmdOptions.FlagOptionDeclaration optVersion =
    CmdOptions.createFlagOption(false, 'V', "version", CmdOptions.Flag.OFF,
                                "display version information and exit");
  private static final CmdOptions.FlagOptionDeclaration optHelp =
    CmdOptions.createFlagOption(false, 'h', "help", CmdOptions.Flag.OFF,
                                "display this help text and exit");
  private static final CmdOptions.IntegerOptionDeclaration optPort =
    CmdOptions.createIntegerOption("PORT", false, 'p', "port",
                                   Constants.
                                   REGISTER_SERVER_DEFAULT_PORT_NUMBER,
                                   "use PORT as server port number");
  private static final CmdOptions.StringOptionDeclaration optScript =
    CmdOptions.createStringOption("PATH", false, 's', "script", null,
                                  "path of monitor script file to execute");
  private static final List<CmdOptions.OptionDeclaration<?>>
    optionDeclarations =
    Arrays.asList(new CmdOptions.OptionDeclaration<?>[]
                  { optVersion, optHelp, optPort, optScript });

  private final BufferedReader in;
  private final PrintStream console;
  private final SDK sdk;
  private final PIOSDK pioSdk;
  private final GPIOSDK gpioSdk;
  private final CmdOptions options;
  private final CommandRegistry commands;

  private Monitor()
  {
    throw new UnsupportedOperationException("unsupported empty constructor");
  }

  public Monitor(final BufferedReader in, final PrintStream console,
                 final String[] argv)
    throws IOException
  {
    if (in == null) {
      throw new NullPointerException("in");
    }
    if (console == null) {
      throw new NullPointerException("console");
    }
    this.in = in;
    this.console = console;
    if ((options = parseArgs(argv)) != null) {
      printAbout();
      sdk = new SDK(console, connect());
      pioSdk = sdk.getPIO0SDK();
      gpioSdk = sdk.getGPIOSDK();
      commands = new CommandRegistry(console, in, sdk);
    } else {
      sdk = null;
      pioSdk = null;
      gpioSdk = null;
      commands = null;
    }
  }

  private CmdOptions parseArgs(final String argv[]) throws IOException
  {
    final CmdOptions options;
    try {
      options = new CmdOptions(PRG_NAME, PRG_ID_AND_VERSION, null,
                               optionDeclarations);
      options.parse(argv);
      checkValidity(options);
    } catch (final CmdOptions.ParseException e) {
      final String message =
        String.format("parsing command line failed: %s", e.getMessage());
      throw new IOException(message);
    }
    if (options.getValue(optVersion) == CmdOptions.Flag.ON) {
      console.println(PRG_ID_AND_VERSION);
      return null;
    }
    if (options.getValue(optHelp) == CmdOptions.Flag.ON) {
      console.println(options.getFullInfo());
      return null;
    }
    return options;
  }

  private void checkValidity(final CmdOptions options)
    throws CmdOptions.ParseException
  {
    final int port = options.getValue(optPort);
    if ((port < 0) || (port > 65535)) {
      throw new CmdOptions.
        ParseException("PORT must be in the range 0…65535");
    }
  }

  private void printAbout()
  {
    console.printf("%s%n%s%n%s%n",
                   PROGRAM_ID, Constants.getAbout(),
                   String.format(Command.commandHint));
  }

  private RegisterClient connect() throws IOException
  {
    final int port = options.getValue(optPort);
    try {
      console.printf("connecting to emulation server at port %d…%n", port);
      return new RegisterClient(console, port);
    } catch (final IOException e) {
      final String message =
        String.format("failed to connect to emulation server: %s%n" +
                      "check that emulation server runs at port address %d%n",
                      e.getMessage(), port);
      throw new IOException(message);
    }
  }

  private int run(final boolean localEcho)
  {
    if (sdk == null) return 0;
    final String optScriptValue = options.getValue(optScript);
    final BufferedReader scriptIn;
    if (optScriptValue != null) {
      try {
        scriptIn = IOUtils.getReaderForResourcePath(optScriptValue);
      } catch (final IOException e) {
        console.println(e.getMessage());
        return -1;
      }
    } else {
      scriptIn = null;
    }
    return
      (scriptIn != null) ?
      session(scriptIn, false, true, "script> ") :
      session(in, false, localEcho, "> ");
  }

  private int session(final BufferedReader in,
                      final boolean dryRun,
                      final boolean localEcho,
                      final String prompt) {
    try {
      while (true) {
        console.print(prompt);
        try {
          final String line = in.readLine();
          if (line == null) break;
          if (localEcho) console.println(line);
          if (commands.parseAndExecute(line, dryRun)) break;
        } catch (final Panic | IOException e) {
          console.println(e.getMessage());
          if (e instanceof Panic) {
            console.printf(Command.panicNotes);
            console.println();
          }
        }
      }
      console.println("bye");
      return 0;
    } catch (final RuntimeException e) {
      console.printf("fatal error: %s%n", e.getMessage());
      console.println();
      console.println("detailed debug information:");
      e.printStackTrace(console);
      return -1;
    }
  }

  public static int main(final String argv[],
                         final InputStream in,
                         final PrintStream out,
                         final boolean localEcho)
  {
    final BufferedReader reader =
      new BufferedReader(new InputStreamReader(in));
    try {
      final int exitCode = new Monitor(reader, out, argv).run(localEcho);
      return exitCode;
    } catch (final IOException e) {
      out.println(e.getMessage());
      return -1;
    }
  }

  public static void main(final String argv[])
  {
    final int exitCode = main(argv, System.in, System.out, false);
    System.exit(exitCode);
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
