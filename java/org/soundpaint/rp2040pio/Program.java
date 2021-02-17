/*
 * @(#)Program.java 1.00 21/02/06
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
package org.soundpaint.rp2040pio;

import java.util.Arrays;
import java.io.InputStream;
import java.io.IOException;

/**
 * This class is for compatibility with the Raspberry Pi Pico SDK.
 * It represents C struct pio_program_t and all functions that
 * manipulate this struct.
 */
public class Program
{
  private final int origin;
  private final short[] instructions;
  private final int allocationMask;

  public static Program fromBinResource(final String resourcePath)
    throws IOException
  {
    final InputStream in = Main.class.getResourceAsStream(resourcePath);
    if (in == null) {
      throw new IOException("failed loading code: resource not found: " +
                            resourcePath);
    }
    final int available = in.available();
    if (available > Memory.SIZE * 2) {
      throw new IOException("failed loading code: size too large: " +
                            available + " > " + Memory.SIZE * 2);
    }
    if ((available & 0x3) != 0) {
      throw new IOException("failed loading code: " +
                            "size must be multiple of 4: " + available);
    }
    final short[] code = new short[Memory.SIZE];
    for (int address = 0; address < available / 4; address ++) {
      short value = 0;
      for (int byteCount = 0; byteCount < 2; byteCount++) {
        value <<= 0x8;
        value |= (in.read() & 0xff);
      }
      code[address] = value;
    }
    System.out.println("loaded " + (available / 4) + " PIO SM instructions");
    return new Program(code, -1);
  }

  public static Program fromHexResource(final String resourcePath)
    throws IOException
  {
    return ProgramParser.parse(resourcePath);
  }

  private Program()
  {
    throw new UnsupportedOperationException("unsupported empty constructor");
  }

  /**
   * @param instructions Array with all instructions of the program.
   * If the array is null or the length of the array is greater than
   * 32, an exception is thrown.
   * @param origin Either the fixed origin of the program (values
   * 0..31), or -1, if the program is relocatable, such that it does
   * not matter where it will be loaded into memory.
   */
  public Program(final short[] instructions, final int origin)
  {
    if (instructions == null) {
      throw new NullPointerException("instructions");
    }
    final int length = instructions.length;
    if (length > Memory.SIZE) {
      throw new IllegalArgumentException("instructions length > " +
                                         Memory.SIZE + ": " + length);
    }
    if (origin < -1) {
      throw new IllegalArgumentException("origin < -1: " + origin);
    }
    if (origin > Memory.SIZE - 1) {
      throw new IllegalArgumentException("origin > " + (Memory.SIZE - 1) +
                                         ": " + origin);
    }
    this.instructions = Arrays.copyOf(instructions, length);
    this.origin = origin;
    final int mask = (0x1 << length) - 1;
    allocationMask =
      origin >= 0 ? mask << origin | (mask << (origin - Memory.SIZE)) : mask;
  }

  public int getLength()
  {
    return instructions.length;
  }

  public short getInstruction(final int index)
  {
    if (index < 0) {
      throw new IllegalArgumentException("index < 0: " + index);
    }
    if (index > instructions.length) {
      throw new IllegalArgumentException("index > instructions length: " +
                                         index);
    }
    return instructions[index];
  }

  /**
   * @return The origin of the program.  Either a fixed values in the
   * range 0..31, or -1, if the program is relocatable, such that it
   * does not matter where it will be loaded into memory.
   */
  public int getOrigin()
  {
    return origin;
  }

  /**
   * Helper function that returns an allocation mask for the program.
   * If the program origin is -1, the allocation mask is 0-based.
   */
  public int getAllocationMask()
  {
    return allocationMask;
  }

  @Override
  public String toString()
  {
    return
      String.format("Program{origin=%02x,length=%02x}",
                    origin, instructions.length);
  }
}

/*
 * Local Variables:
 *   coding:utf-8
 *   mode:Java
 * End:
 */
