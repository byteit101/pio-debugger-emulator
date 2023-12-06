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

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.*;

import org.soundpaint.rp2040pio.SwingUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.awt.event.ActionEvent;
import java.util.*;

import javax.swing.border.TitledBorder;

public class ScriptLoadPanel extends JPanel {
	private JComboBox<String> comboBox;

	/**
	 * Create the panel.
	 */
	public ScriptLoadPanel() {
		setBorder(new TitledBorder(null, "Script Loader", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0};
		gridBagLayout.rowWeights = new double[]{0.0};
		setLayout(gridBagLayout);
		
		comboBox = new JComboBox<String>();
		comboBox.setToolTipText("File path to the .mon script");
		comboBox.setEditable(true);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.insets = new Insets(6, 6, 6, 6);
		gbc_comboBox.anchor = GridBagConstraints.WEST;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 0;
		add(comboBox, gbc_comboBox);
		addSetCombo(null);
		
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			    JFileChooser fileChooser = SwingUtils.makeFileChooser();
			    var lst = listedItems();
			    if (!lst.isEmpty())
			    	fileChooser.setCurrentDirectory(new File(lst.get(0)).getParentFile());
			    fileChooser.setFileFilter(new FileNameExtensionFilter("Monitor files (*.mon)", "mon"));
			    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
			    if (fileChooser.showOpenDialog(ScriptLoadPanel.this) ==JFileChooser.APPROVE_OPTION)
			    {
			    	addSetCombo(fileChooser.getSelectedFile().toString());
			    }
			    
			}
		});
		btnBrowse.setToolTipText("Browse for a .mon script");
		GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
		gbc_btnBrowse.insets = new Insets(6, 6, 6, 6);
		gbc_btnBrowse.anchor = GridBagConstraints.WEST;
		gbc_btnBrowse.gridx = 1;
		gbc_btnBrowse.gridy = 0;
		add(btnBrowse, gbc_btnBrowse);
		
		JButton btnReload = new JButton("Reload");
		btnReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				triggerReload();
			}
		});
		btnReload.setToolTipText("Load or re-load the selected monitor script");
		GridBagConstraints gbc_btnReload = new GridBagConstraints();
		gbc_btnReload.insets = new Insets(6, 6, 6, 6);
		gbc_btnReload.anchor = GridBagConstraints.WEST;
		gbc_btnReload.gridx = 2;
		gbc_btnReload.gridy = 0;
		add(btnReload, gbc_btnReload);
		
		JCheckBox chckbxAutoreload = new JCheckBox("Auto-reload");
		chckbxAutoreload.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateInotify(chckbxAutoreload.isSelected());
			}
		});
		GridBagConstraints gbc_chckbxAutoreload = new GridBagConstraints();
		gbc_chckbxAutoreload.anchor = GridBagConstraints.WEST;
		gbc_chckbxAutoreload.gridx = 3;
		gbc_chckbxAutoreload.gridy = 0;
		add(chckbxAutoreload, gbc_chckbxAutoreload);

	}

	protected void addSetCombo(String string) {
		var model = new DefaultComboBoxModel<String>();
		if (string != null)
			saveSort(string);
		model.addAll(listedItems());
		model.setSelectedItem(model.getElementAt(0));
		comboBox.setModel(model);
		
		// reload inotify watcher
		updateInotify(inotify != null);
		
	}

	List<String> items = null;
	private List<String> listedItems() {
		if (items == null)
			return items = getList(Preferences.userNodeForPackage(getClass()).node("scriptLoadPath"));
		else
			return items;
	}

	private void setList(Preferences p, Collection<String> values)
	{
		try {
			p.clear();
		} catch (BackingStoreException e) {
			e.printStackTrace();
			// just... ignore this? idk
		}
		var list = new ArrayList<>(values);
		p.putInt("length", list.size());
		for (int i = 0; i < list.size(); i++)
		{
			p.put("i" + i, list.get(i));
		}
	}

	private List<String> getList(Preferences p)
	{
		var list = new ArrayList<String>();
		int size = p.getInt("length", 0);
		for (int i = 0; i < size; i++)
		{
			list.add(p.get("i" + i, ""));
		}
		return list;
	}

	private void saveSort(String string) {
		var col = new ArrayList<>(listedItems());
		if (col.contains(string))
		{
			col.remove(string);
		}
		else while (col.size() > 5)
		{
			col.remove(col.size()-1);
		}
		col.add(0, string);
		setList(Preferences.userNodeForPackage(getClass()).node("scriptLoadPath"), col);
		items = col;
		
	}
	
	private FSWatcher inotify = null;

	protected void updateInotify(boolean install) {
		try {
		if (inotify != null)
			inotify.close();
		if (install)
				inotify = new FSWatcher(getFileString(), ()->{
					triggerReload();
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		
	}
	
	Consumer<File> reload;
	

	public void setReload(Consumer<File> reload) {
		this.reload = reload;
	}

	protected void triggerReload() {
		if (reload != null)
			reload.accept(getFileString());
	}


	public File getFileString() {
		return new File(comboBox.getSelectedItem().toString());
	}
}
