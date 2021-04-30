/*
 * @(#)CodeViewPanel.java 1.00 21/04/24
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

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import org.soundpaint.rp2040pio.Constants;
import org.soundpaint.rp2040pio.PicoEmuRegisters;
import org.soundpaint.rp2040pio.SwingUtils;
import org.soundpaint.rp2040pio.sdk.SDK;

public class CodeViewPanel extends JPanel
{
  private static final long serialVersionUID = -2394791187467829359L;

  private final PrintStream console;
  private final SDK sdk;
  private final int refresh;
  private final CodeSmViewPanel codeSmViewPanel;
  private final JProgressBar pbDelay;
  private int pioNum;
  private int smNum;

  private CodeViewPanel()
  {
    throw new UnsupportedOperationException("unsupported empty constructor");
  }

  public CodeViewPanel(final PrintStream console, final SDK sdk,
                       final int refresh)
    throws IOException
  {
    Objects.requireNonNull(console);
    Objects.requireNonNull(sdk);
    this.console = console;
    this.sdk = sdk;
    this.refresh = refresh;
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    setBorder(BorderFactory.createTitledBorder("Code View"));
    pbDelay = new JProgressBar(0, 1000);
    codeSmViewPanel = new CodeSmViewPanel(console, sdk, pbDelay);

    final Box pioSelection = new Box(BoxLayout.X_AXIS);
    add(pioSelection);
    final JLabel lbPio = new JLabel("PIO");
    pioSelection.add(lbPio);
    pioSelection.add(Box.createHorizontalStrut(15));
    addPioButtons(pioSelection);
    pioSelection.add(Box.createHorizontalGlue());
    SwingUtils.setPreferredHeightAsMaximum(pioSelection);

    final Box smSelection = new Box(BoxLayout.X_AXIS);
    add(smSelection);
    final JLabel lbSm = new JLabel("SM");
    lbSm.setMinimumSize(lbPio.getPreferredSize());
    lbSm.setPreferredSize(lbPio.getPreferredSize());
    smSelection.add(lbSm);
    smSelection.add(Box.createHorizontalStrut(15));
    addSmButtons(smSelection);
    smSelection.add(Box.createHorizontalGlue());
    SwingUtils.setPreferredHeightAsMaximum(smSelection);

    pbDelay.setStringPainted(true);
    add(codeSmViewPanel);
    add(pbDelay);
    codeSmViewPanel.smChanged(pioNum, smNum);
    new Thread(() -> updateLoop()).start();
  }

  private void addPioButtons(final Box pioSelection)
  {
    final ButtonGroup bgPio = new ButtonGroup();
    for (int pioNum = 0; pioNum < Constants.PIO_NUM; pioNum++) {
      if (pioNum != 0) pioSelection.add(Box.createHorizontalStrut(10));
      final JRadioButton rbPio = new JRadioButton(String.valueOf(pioNum));
      rbPio.setSelected(pioNum == 0);
      final int finalPioNum = pioNum;
      rbPio.addActionListener((event) -> {
          this.pioNum = finalPioNum;
          codeSmViewPanel.smChanged(finalPioNum, smNum);
        });
      bgPio.add(rbPio);
      pioSelection.add(rbPio);
    }
  }

  private void addSmButtons(final Box smSelection)
  {
    final ButtonGroup bgSm = new ButtonGroup();
    for (int smNum = 0; smNum < Constants.SM_COUNT; smNum++) {
      if (smNum != 0) smSelection.add(Box.createHorizontalStrut(10));
      final JRadioButton rbSm = new JRadioButton(String.valueOf(smNum));
      rbSm.setSelected(smNum == 0);
      rbSm.setMnemonic(KeyEvent.VK_0 + smNum);
      final int finalSmNum = smNum;
      rbSm.addActionListener((event) -> {
          this.smNum = finalSmNum;
          codeSmViewPanel.smChanged(pioNum, finalSmNum);
        });
      bgSm.add(rbSm);
      smSelection.add(rbSm);
    }
  }

  public void updateLoop()
  {
    final int addressPhase0 =
      PicoEmuRegisters.getAddress(PicoEmuRegisters.Regs.
                                  MASTERCLK_TRIGGER_PHASE0);
    final int addressPhase1 =
      PicoEmuRegisters.getAddress(PicoEmuRegisters.Regs.
                                  MASTERCLK_TRIGGER_PHASE1);
    final int expectedValue = 0x1; // update upon stable cycle phase 1
    final int mask = 0xffffffff;
    final int cyclesTimeout = 0;
    final int millisTimeout = 1000; // but at least every second
    while (true) {
      try {
        while (true) {
          sdk.wait(addressPhase1, expectedValue, mask,
                   cyclesTimeout, millisTimeout);
          codeSmViewPanel.smChanged(pioNum, smNum);
          codeSmViewPanel.repaintLater();
          sdk.wait(addressPhase0, expectedValue, mask,
                   cyclesTimeout, millisTimeout);
        }
      } catch (final IOException e) {
        console.printf("update loop: %s%n", e.getMessage());
        try {
          Thread.sleep(1000); // limit CPU load in case of persisting
                              // error
        } catch (final InterruptedException e2) {
          // ignore
        }
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
