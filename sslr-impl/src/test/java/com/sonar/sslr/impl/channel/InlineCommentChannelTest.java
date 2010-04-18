/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.sslr.impl.channel;

import org.junit.Test;
import org.sonar.channel.CodeReader;

import com.sonar.sslr.api.Comments;
import com.sonar.sslr.impl.LexerOutput;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class InlineCommentChannelTest {

  private LexerOutput output = new LexerOutput();
  private InlineCommentChannel channel;

  @Test
  public void testConsumCommentStartingWithOneCharacter() {
    channel = new InlineCommentChannel("'");
    assertTrue(channel.consum(new CodeReader("' my comment\n toto"), output));
    Comments comments = output.getComments();
    assertThat(comments.getCommentAtLine(1).getValue(), is("' my comment"));
  }
  
  @Test
  public void testConsumCppComment() {
    channel = new InlineCommentChannel("//");
    assertTrue(channel.consum(new CodeReader("// my comment\r lkjd"), output));
    Comments comments = output.getComments();
    assertThat(comments.getCommentAtLine(1).getValue(), is("// my comment"));
  }

  @Test
  public void testNotConsumWord() {
    channel = new InlineCommentChannel("'");
    assertFalse(channel.consum(new CodeReader("word"), output));
  }
}