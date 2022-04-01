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

package org.intellij.erlang.debugger.node.events;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;
import org.intellij.erlang.debugger.node.ErlangDebuggerEventListener;
import org.intellij.erlang.debugger.node.ErlangDebuggerNode;
import org.jetbrains.annotations.NotNull;

class SetBreakpointResponseEvent extends ErlangDebuggerEvent {
  public static final String NAME = "set_breakpoint_response";

  private final String myModule;
  private final int myLine;
  private final String myError;

  public SetBreakpointResponseEvent(@NotNull OtpErlangTuple message) throws DebuggerEventFormatException {
    myModule = OtpErlangTermUtil.getAtomText(message.elementAt(1));
    if (myModule == null) throw new DebuggerEventFormatException();

    Integer line = OtpErlangTermUtil.getIntegerValue(message.elementAt(2));
    if (line == null) throw new DebuggerEventFormatException();
    myLine = line.intValue() - 1;

    OtpErlangObject statusObject = message.elementAt(3);
    if (OtpErlangTermUtil.isOkAtom(statusObject)) {
      myError = null;
    }
    else if (statusObject instanceof OtpErlangTuple) {
      OtpErlangTuple errorTuple = (OtpErlangTuple) statusObject;
      if (!OtpErlangTermUtil.isErrorAtom(errorTuple.elementAt(0))) throw new DebuggerEventFormatException();
      myError = OtpErlangTermUtil.toString(errorTuple.elementAt(1));
    }
    else {
      throw new DebuggerEventFormatException();
    }
  }

  @Override
  public void process(ErlangDebuggerNode debuggerNode, @NotNull ErlangDebuggerEventListener eventListener) {
    if (myError == null) {
      eventListener.breakpointIsSet(myModule, myLine);
    }
    else {
      eventListener.failedToSetBreakpoint(myModule, myLine, myError);
    }
  }
}
