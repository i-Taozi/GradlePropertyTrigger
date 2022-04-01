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

import com.ericsson.otp.erlang.*;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class OtpErlangTermUtil {
  private OtpErlangTermUtil() {
  }

  @Nullable
  public static Integer getIntegerValue(@Nullable OtpErlangObject integerObject) {
    OtpErlangLong otpLong = integerObject instanceof OtpErlangLong ? (OtpErlangLong) integerObject : null;
    try {
      return otpLong != null ? otpLong.intValue() : null;
    } catch (OtpErlangRangeException e) {
      return null;
    }
  }

  @Nullable
  public static OtpErlangList getListValue(@Nullable OtpErlangObject listObject) {
    if (listObject instanceof OtpErlangList) {
      return (OtpErlangList)listObject;
    }
    if (listObject instanceof OtpErlangString) {
      OtpErlangString string = (OtpErlangString) listObject;
      return new OtpErlangList(string.stringValue());
    }
    return null;
  }

  @Nullable
  public static OtpErlangTuple getTupleValue(@Nullable OtpErlangObject tupleObject) {
    return tupleObject instanceof OtpErlangTuple ? (OtpErlangTuple) tupleObject : null;
  }

  @Nullable
  public static OtpErlangPid getPidValue(@Nullable OtpErlangObject pidObject) {
    return pidObject instanceof OtpErlangPid ? (OtpErlangPid) pidObject : null;
  }

  @Nullable
  public static String getAtomText(@Nullable OtpErlangObject atomObject) {
    OtpErlangAtom atom = atomObject instanceof OtpErlangAtom ? (OtpErlangAtom) atomObject : null;
    return atom != null ? atom.atomValue() : null;
  }

  @Nullable
  public static String getStringText(@Nullable OtpErlangObject stringObject) {
    if (stringObject instanceof OtpErlangString) {
      return ((OtpErlangString) stringObject).stringValue();
    }
    else if (stringObject instanceof OtpErlangList) {
      try {
        return ((OtpErlangList) stringObject).stringValue();
      } catch (OtpErlangException ignore) {
      }
    }
    return null;
  }

  @Nullable
  public static OtpErlangObject elementAt(@Nullable OtpErlangTuple tuple, int idx) {
    return tuple != null ? tuple.elementAt(idx) : null;
  }

  public static boolean isOkAtom(@Nullable OtpErlangObject okObject) {
    return isAtom("ok", okObject);
  }

  public static boolean isErrorAtom(@Nullable OtpErlangObject errorObject) {
    return isAtom("error", errorObject);
  }

  private static boolean isAtom(@NotNull String expectedAtom, @Nullable OtpErlangObject atomObject) {
    return StringUtil.equals(expectedAtom, getAtomText(atomObject));
  }

  @NotNull
  public static String toString(@Nullable OtpErlangObject object) {
    return null != object ? object.toString() : "";
  }
}
