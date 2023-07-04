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
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.Objects;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.soundpaint.rp2040pio.CollapsiblePanel;
import org.soundpaint.rp2040pio.IOUtils;
import org.soundpaint.rp2040pio.SwingUtils;
import org.soundpaint.rp2040pio.monitor.Monitor;
import org.soundpaint.rp2040pio.monitor.commands.Script;
import org.soundpaint.rp2040pio.sdk.SDK;

public class ScriptLoadController 
{

  private final PrintStream console;
  private final SDK sdk;
  private final ScriptLoadPanel view;


  public ScriptLoadController(final PrintStream console, final SDK sdk)
    throws IOException
  {
    Objects.requireNonNull(console);
    Objects.requireNonNull(sdk);
    this.console = console;
    this.sdk = sdk;
    view = new ScriptLoadPanel();
    view.setReload(f -> {
    	// threads: any
    	this.console.println("Reloading monitor file " + f.toString());
    	
    	var optFileValue = f.getAbsolutePath();

    	try {
    		final LineNumberReader reader =          IOUtils.getReaderForResourcePath(optFileValue);
    	
    		var script = new Script(this.console, Monitor.getMiniMonitor(null, this.sdk));
			if (!script.executeScript(reader, optFileValue, false))
			{
				SwingUtilities.invokeLater(()->{
					JOptionPane.showMessageDialog(null, "Script loading returned failure, see console for details.");
				});
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			SwingUtilities.invokeLater(()->{
				JOptionPane.showMessageDialog(null, "Script loading failed, see console for details.\n" + e.getMessage());
			});
		}
    });
  }

  public ScriptLoadPanel getView() {
	return view;
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
