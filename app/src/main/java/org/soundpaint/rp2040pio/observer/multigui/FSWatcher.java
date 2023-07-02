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

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Objects;

public class FSWatcher implements AutoCloseable {
	
	private final WatchService watchService;
	private File file;
	private Thread thread;
	private volatile boolean running = true;
	private Runnable notify;
	private WatchKey key;
	private Long lastStats;

	public FSWatcher(File f, Runnable onNew) throws IOException {
		watchService  = FileSystems.getDefault().newWatchService();
		this.file = f;
		this.notify = onNew;
		lastStats = stat(f);
		// What is this key used for?
		this.key = f.toPath().getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW, StandardWatchEventKinds.ENTRY_CREATE);
		thread = new Thread(this::run);
		thread.setDaemon(true);
		thread.setName("File Watcher: " + f.toString());
		thread.start();
	}
	
	private Long stat(File f) {
		if (f.exists())
			return f.lastModified();
		else
			return null;
	}

	/**
	 * NOTE: this doesn't deal with symlinks or deleting the parent dir
	 */
	private void run()
	{
		var  ourFile = file.toPath();
		var dir = ourFile.getParent();
	//	while (running)
		try
		{
			boolean valid = true;
			while (valid && running) {
				WatchKey key;
					key = watchService.take();
				boolean triggering = false;
				boolean checking = false;
				  for (var event : key.pollEvents())
				  {
					  if (event.kind() == StandardWatchEventKinds.OVERFLOW)
					  {
						  checking = true;
					  }
					  else
					  {
						  var we = (WatchEvent<Path>)event;
						  var target = dir.resolve(we.context());
						  if (we.kind() == ENTRY_DELETE && target.equals(ourFile))
						  {
							  lastStats = null;
						  }
						  else if (target.equals(ourFile))//Files.isSameFile(target, ourFile))
						  {
							  triggering = true;
						  }
					  }
				  }
				  if (checking && !triggering)
				  {
					  triggering = !Objects.equals(lastStats, stat(file));
				  }
				  if (triggering)
				  {
					  // TODO: delay better
					  Thread.sleep(100);
					  lastStats = stat(file);
					  try
					  {
						  if (running)
							  this.notify.run();
					  } catch (Exception e)
					  {
						  e.printStackTrace();
					  }
				  }
				  valid = key.reset();
			}
		}
		catch(InterruptedException | ClosedWatchServiceException e)
		{
			return;
			// TODO: log?
		}
	}

	@Override
	public void close() throws Exception {
		running = false;
		key.cancel();
	}
}
