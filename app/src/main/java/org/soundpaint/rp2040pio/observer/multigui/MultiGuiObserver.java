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
package org.soundpaint.rp2040pio.observer.multigui;

import java.awt.Component;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.ComponentInputMap;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import org.soundpaint.rp2040pio.Constants;
import org.soundpaint.rp2040pio.observer.GUIObserver;
import org.soundpaint.rp2040pio.observer.code.CodeObserver;
import org.soundpaint.rp2040pio.observer.code.CodeViewPanel;
import org.soundpaint.rp2040pio.observer.fifo.FifoObserver;
import org.soundpaint.rp2040pio.observer.fifo.FifoViewPanel;
import org.soundpaint.rp2040pio.observer.gpio.GPIOViewPanel;

/**
 * Emulation Code Status Observation
 */
public class MultiGuiObserver extends GUIObserver
{
  private static final long serialVersionUID = -8266192991979578046L;
  private static final String APP_TITLE = "GUI Observers";
  private static final String APP_FULL_NAME =
    "Muti-GUI Observer Version 0.1";

  private CodeViewPanel codeViewPanel;
private GPIOViewPanel gpioViewPanel;
private FifoViewPanel fifoViewPanel;
private ScriptLoadController scriptLoad;

  private MultiGuiObserver(final PrintStream console, final String[] argv)
    throws IOException
  {
    super(APP_TITLE, APP_FULL_NAME, console, argv);
    add(makeLayout(console));
    pack();
    setVisible(true);
    startUpdating();
  }
  
  private Component makeLayout(final PrintStream console) throws IOException
  {
	  var contentPane = new JPanel();
		contentPane.setLayout(new GridLayout(0, 2, 6, 6));
		
		fifoViewPanel = new FifoViewPanel(console, getSDK(), FifoObserver.APP_TITLE);
		
		gpioViewPanel = new GPIOViewPanel(console, getSDK());

		var panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fifoViewPanel, gpioViewPanel);
		
		scriptLoad = new ScriptLoadController(console, getSDK());
		
		var leftpan = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panel, scriptLoad.getView());
		
		codeViewPanel = new CodeViewPanel(console, getSDK(), CodeObserver.APP_TITLE);
		var panel2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftpan, codeViewPanel);
		
		return panel2;
  }

  @Override
  protected void updateView()
  {
	    codeViewPanel.updateView();
	    gpioViewPanel.updateView();
	    fifoViewPanel.updateView();
  }

  public static void main(final String argv[])
  {


    try {
            // Set System L&F
        UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName());
    } 
    catch (UnsupportedLookAndFeelException e) {
       // handle exception
    }
    catch (ClassNotFoundException e) {
       // handle exception
    }
    catch (InstantiationException e) {
       // handle exception
    }
    catch (IllegalAccessException e) {
       // handle exception
    }
	  
    final PrintStream console = System.out;
    try {
      new MultiGuiObserver(console, argv);
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
