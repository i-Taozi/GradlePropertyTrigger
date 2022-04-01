/*
 * Copyright 2012-2014 Sergey Ignatov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.intellij.erlang;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.TokenType;

public class ErlangQuoteHandler extends SimpleTokenSetQuoteHandler {
  public ErlangQuoteHandler() {
    super(ErlangTypes.ERL_STRING, ErlangTypes.ERL_SINGLE_QUOTE, TokenType.BAD_CHARACTER);
  }

  @Override
  public boolean isOpeningQuote(HighlighterIterator iterator, int offset) {
    if(iterator.getTokenType() == ErlangTypes.ERL_SINGLE_QUOTE) {
      iterator.retreat();
      boolean isAtomBefore = !iterator.atEnd() && iterator.getTokenType() == ErlangTypes.ERL_ATOM_NAME;
      iterator.retreat();
      boolean atomStartsFromQuote = !iterator.atEnd() && iterator.getTokenType() == ErlangTypes.ERL_SINGLE_QUOTE;
      iterator.advance();
      iterator.advance();
      return !isAtomBefore || !atomStartsFromQuote;
    }

    return super.isOpeningQuote(iterator, offset);
  }
}
