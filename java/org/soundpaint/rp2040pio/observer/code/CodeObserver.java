/*
 * @(#)CodeObserver.java 1.00 21/04/24
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
package org.soundpaint.rp2040pio.observer.code;

import java.io.IOException;
import java.io.PrintStream;
import org.soundpaint.rp2040pio.Constants;
import org.soundpaint.rp2040pio.observer.GUIObserver;

/**
 * Emulation Code Status Observation
 */
public class CodeObserver extends GUIObserver
{
  private static final long serialVersionUID = -8266192991979578046L;
  private static final String APP_TITLE = "Code Observer";
  private static final String APP_FULL_NAME =
    "Emulation Code Observer Version 0.1";

  private final CodeViewPanel codeViewPanel;

  public CodeObserver(final PrintStream console, final String[] argv)
    throws IOException
  {
    super(APP_TITLE, APP_FULL_NAME, console, argv);
    add(codeViewPanel = new CodeViewPanel(console, getSDK(), APP_TITLE));
    pack();
    setVisible(true);
    startUpdating();
  }

  @Override
  public void updateView()
  {
    codeViewPanel.updateView();
  }

  public static void main(final String argv[])
  {
    final PrintStream console = System.out;
    try {
      new CodeObserver(console, argv);
    } catch (final IOException e) {
      console.printf("initialization failed: %s%n", e.getMessage());
    }
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
