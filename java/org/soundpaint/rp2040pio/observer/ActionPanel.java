/*
 * @(#)ActionPanel.java 1.00 21/04/10
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
package org.soundpaint.rp2040pio.observer;

import java.awt.event.KeyEvent;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;

public class ActionPanel<T extends GUIObserver> extends Box
{
  private static final long serialVersionUID = -3674776627922144842L;

  public ActionPanel(final T observer)
  {
    super(BoxLayout.X_AXIS);
    Objects.requireNonNull(observer);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    addAdditionalButtons(observer);
    add(Box.createHorizontalGlue());
    final JButton btClose = new JButton("Close");
    btClose.setMnemonic(KeyEvent.VK_C);
    btClose.addActionListener((event) -> { observer.close(); });
    add(btClose);
  }

  /**
   * Override this method to add additional buttons to appear to the
   * left side of the close button.  The default implementation of
   * this method is empty.
   */
  protected void addAdditionalButtons(final T observer)
  {
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
