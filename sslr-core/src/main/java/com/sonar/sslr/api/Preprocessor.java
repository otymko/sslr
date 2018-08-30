/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.sonar.sslr.api;

import java.util.List;

/**
 * <p>
 * A preprocessor is a component which can alter the stream of Token and Trivia.<br />
 * The supported operations are injection and deletion. As Token are immutable, modification can be achieved by performing a deletion and an
 * injection.
 * </p>
 *
 * <p>
 * Without any preprocessor, the components of SSLR are chained as follows:
 *
 * <pre>
 * ---------------    ---------    ----------    -------
 * | Source file | -> | Lexer | -> | Parser | -> | AST |
 * ---------------    ---------    ----------    -------
 * </pre>
 * </p>
 *
 * <p>
 * A preprocessor sits between the Lexer and the Parser, thus components are chained as follows:
 *
 * <pre>
 * ---------------    ---------    ----------------    ----------    -------
 * | Source file | -> | Lexer | -> | Preprocessor | -> | Parser | -> | AST |
 * ---------------    ---------    ----------------    ----------    -------
 * </pre>
 * </p>
 *
 * <p>
 * Preprocessors can also be chained, in which case the stream of tokens seen by the second preprocessor is the stream of token which was
 * generated by the first one:
 *
 * <pre>
 *        ------------------    ------------------
 * ... -> | Preprocessor 1 | -> | Preprocessor 2 | -> ...
 *        ------------------    ------------------
 * </pre>
 * </p>
 *
 * <p>
 * The first preprocessor sees the tokens as they were generated by the Lexer. The Parser sees the tokens as they were generated by the last
 * preprocessor.
 * </p>
 *
 * @deprecated in 1.20, use your own preprocessor API instead.
 */
@Deprecated
public abstract class Preprocessor {

  /**
   * Method called before the lexing starts which can be overridden to initialize a state for instance.
   */
  public void init() {
  }

  /**
   * <p>
   * Method called on each token seen by the current preprocessor.
   * </p>
   *
   * <p>
   * On each invocation of this method, all the remaining tokens are available. It is therefore possible to launch a Parser on the remaining
   * input, and perform actions depending on the result of that parse. At the same time, a preprocessor can keep track of a state to keep
   * track of the previous tokens which were seen, together with the init method.
   * </p>
   *
   * <p>
   * As an example, if a no-operation preprocessor (i.e. one which does not perform any injection nor deletion) is put after a Lexer which
   * returned the tokens "a", "b", "c" and "EOF", this method will be called 4 times with different values of the tokens parameter:
   *
   * <ol>
   * <li>"a", "b", "c" and "EOF"</li>
   * <li>"b", "c" and "EOF"</li>
   * <li>"c" and "EOF"</li>
   * <li>"EOF"</li>
   * </ol>
   * </p>
   *
   * <p>
   * All the operations to be performed by the preprocessor should be encapsulated in the PreprocessorAction return value object. The tokens
   * list parameter is immutable.
   * </p>
   *
   * @param tokens
   *          An unmodifiable list of the remaining tokens.
   * @return
   *         A preprocessor action, containing the Token/Trivia injections and deletions to perform. See PreprocessorAction for details.
   */
  public abstract PreprocessorAction process(List<Token> tokens);

}
