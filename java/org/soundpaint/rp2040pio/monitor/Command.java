/*
 * @(#)Command.java 1.00 21/03/28
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

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.soundpaint.rp2040pio.CmdOptions;

/**
 * Common abstract super class for all monitor commands.
 */
public abstract class Command
{
  private static final List<CmdOptions.OptionDeclaration<?>>
    EMPTY_OPTION_DECLARATIONS =
    new ArrayList<CmdOptions.OptionDeclaration<?>>();

  protected static final CmdOptions.FlagOptionDeclaration optHelp =
    CmdOptions.createFlagOption(false, 'h', "help", CmdOptions.Flag.OFF,
                                "display this help text and exit");

  protected final PrintStream out;
  private final String fullName;
  private final String singleLineDescription;
  private final List<CmdOptions.OptionDeclaration<?>> optionDeclarations;
  private final CmdOptions options;

  private Command()
  {
    throw new UnsupportedOperationException("unsupported empty constructor");
  }

  public Command(final PrintStream out, final String fullName,
                 final String singleLineDescription)
  {
    this(out, fullName, singleLineDescription, EMPTY_OPTION_DECLARATIONS);
  }

  public Command(final PrintStream out, final String fullName,
                 final String singleLineDescription,
                 final CmdOptions.OptionDeclaration<?>[] optionDeclarations)
  {
    this(out, fullName, singleLineDescription,
         Arrays.asList(optionDeclarations));
  }

  public Command(final PrintStream out, final String fullName,
                 final String singleLineDescription,
                 final List<CmdOptions.OptionDeclaration<?>> optionDeclarations)
  {
    if (out == null) {
      throw new NullPointerException("out");
    }
    if (fullName == null) {
      throw new NullPointerException("fullName");
    }
    if (fullName.length() == 0) {
      throw new IllegalArgumentException("empty fullName");
    }
    if (singleLineDescription == null) {
      throw new NullPointerException("singleLineDescription");
    }
    if (optionDeclarations == null) {
      throw new NullPointerException("optionDeclarations");
    }
    for (final CmdOptions.OptionDeclaration<?> decl : optionDeclarations) {
      if (decl  == null) {
        throw new NullPointerException("null value in optionDeclarations");
      }
    }
    this.out = out;
    this.fullName = fullName;
    this.singleLineDescription = singleLineDescription;
    this.optionDeclarations = new ArrayList<CmdOptions.OptionDeclaration<?>>();
    this.optionDeclarations.addAll(optionDeclarations);
    this.optionDeclarations.add(optHelp);
    try {
      options =
        new CmdOptions(fullName, singleLineDescription,
                       this.optionDeclarations);
    } catch (final CmdOptions.ParseException e) {
      throw new InternalError("unexpected command configuration error");
    }
  }

  public String getFullName()
  {
    return fullName;
  }

  public String getSingleLineDescription()
  {
    return singleLineDescription;
  }

  public Iterator<CmdOptions.OptionDeclaration<?>>
    getOptionDeclarationsIterator()
  {
    return optionDeclarations.iterator();
  }

  /**
   * Returns true if no error occurred and the command has been
   * executed.
   */
  public boolean parseAndExecute(final String argv[])
  {
    try {
      options.parse(argv);
      checkValidity(options);
      if (options.getValue(optHelp) == CmdOptions.Flag.ON) {
        out.println(options.getFullInfo());
        return false;
      }
      return execute(options);
    } catch (final CmdOptions.ParseException e) {
      out.println(e.getMessage());
      return false;
    } catch (final IOException e) {
      out.println(e.getMessage());
      return false;
    }
  }

  /**
   * Returns true if no error occurred and the command has been
   * executed.
   */
  protected abstract boolean execute(final CmdOptions options)
    throws IOException;

  /**
   * Subclasses that implement this abstract class should override
   * this method and throw a CmdOptions.ParseException, if they
   * require additional constraints that the supplied options do not
   * fulfill.  An example for a possible constraint is a combination
   * of two options that both are valid if specified alone, but that
   * can not be specified together.  Another example is a value
   * constraint, e.g. if a string parameter must be from a fixed set
   * of strings or may not contain specific special characters.
   */
  protected void checkValidity(final CmdOptions options)
    throws CmdOptions.ParseException
  {
    /**
     * Example code for illegal combination of silent flag and verbose
     * flag:
     *
     *    if ((options.getValue(optSilent) == CmdOptions.Flag.ON) &amp;&amp;
     *    (options.getValue(optVerbose) == CmdOptions.Flag.ON)) {
     *      throw new CmdOptions.
     *        ParseException("at most one of 'silent', 'verbose' allowed");
     *    }
     */
  }

  public String getHelp()
  {
    return options.getFullInfo();
  }

  @Override
  public String toString()
  {
    return "command \"" + fullName + "\"";
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
