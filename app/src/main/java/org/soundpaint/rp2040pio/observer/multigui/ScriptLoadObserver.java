/*
 * Copyright (C) 2023 Patrick Plenefisch
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
 */
package org.soundpaint.rp2040pio.observer.multigui;

import java.io.IOException;
import java.io.PrintStream;
import org.soundpaint.rp2040pio.Constants;
import org.soundpaint.rp2040pio.observer.GUIObserver;

/**
 * Script Loader Control
 */
public class ScriptLoadObserver extends GUIObserver
{
  public static final String APP_TITLE = "Script Load Observer";
  private static final String APP_FULL_NAME =
    "Script Load Observer Version 0.1";

  private final ScriptLoadController scriptLoader;

  private ScriptLoadObserver(final PrintStream console, final String[] argv)
    throws IOException
  {
    super(APP_TITLE, APP_FULL_NAME, console, argv);
    scriptLoader = new ScriptLoadController(console, getSDK());
    add(scriptLoader.getView());
    pack();
    setVisible(true);
    startUpdating();
    
  }

  @Override
  protected void updateView()
  {
	  // nothing to update
  }

  public static void main(final String argv[])
  {
    final PrintStream console = System.out;
    try {
      new ScriptLoadObserver(console, argv);
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
