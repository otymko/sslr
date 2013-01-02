/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.sslr.internal.text;

import org.sonar.sslr.text.Text;
import org.sonar.sslr.text.TextCursor;
import org.sonar.sslr.text.TextLocation;

public class SubText extends AbstractText {

  private final AbstractText text;
  private final int start;
  private final int length;

  public SubText(AbstractText text, int start, int end) {
    this.text = text;
    this.start = start;
    this.length = end - start;
  }

  public int length() {
    return length;
  }

  @Override
  public void toCharArray(int srcPos, char[] dest, int destPos, int length) {
    text.toCharArray(getOriginalIndex(srcPos), dest, destPos, length);
  }

  @Override
  protected int getTransformationDepth() {
    return text.getTransformationDepth();
  }

  public TextCursor cursor() {
    return new SubTextCursor();
  }

  private int getOriginalIndex(int index) {
    return start + index;
  }

  private class SubTextCursor implements TextCursor {

    private TextCursor innerCursor = text.cursor();

    public Text getText() {
      return SubText.this;
    }

    public int length() {
      return length;
    }

    public char charAt(int index) {
      return innerCursor.charAt(getOriginalIndex(index));
    }

    public TextCursor subSequence(int start, int end) {
      return subText(start, end).cursor();
    }

    public Text subText(int start, int end) {
      if (start == 0 && end == length) {
        return SubText.this;
      }
      return innerCursor.subText(getOriginalIndex(start), getOriginalIndex(end));
    }

    public TextLocation getLocation(int index) {
      return innerCursor.getLocation(getOriginalIndex(index));
    }

    @Override
    public String toString() {
      // contract of CharSequence
      return getText().toString();
    }

  }

}
