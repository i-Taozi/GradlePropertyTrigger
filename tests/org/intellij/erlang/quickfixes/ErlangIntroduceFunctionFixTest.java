/*
 * Copyright 2012-2015 Sergey Ignatov
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

package org.intellij.erlang.quickfixes;

import org.intellij.erlang.inspection.ErlangUnresolvedExportFunctionInspection;
import org.intellij.erlang.inspection.ErlangUnresolvedFunctionInspection;

public class ErlangIntroduceFunctionFixTest extends ErlangQuickFixTestBase {
  @SuppressWarnings("unchecked")
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(ErlangUnresolvedFunctionInspection.class);
    myFixture.enableInspections(ErlangUnresolvedExportFunctionInspection.class);
  }

  @Override
  protected String getTestDataPath() {
    return "testData/quickfixes/introduce_function/";
  }

  public void testFunctionCall()        { doTest("Create Function lll/6"); }
  public void testFunctionExpression()  { doTest("Create Function lll/6"); }
  public void testFunctionSpec()        { doTest("Create Function lll/6"); }
  public void testFunctionExport()      { doTest("Create Function lll/6"); }
  public void testQuote()               { doTest("Create Function 'q ote'/0"); }
}
