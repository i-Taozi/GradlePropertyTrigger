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

package org.intellij.erlang.inspection;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiReference;
import org.intellij.erlang.psi.ErlangAtom;
import org.intellij.erlang.psi.ErlangExportFunction;
import org.intellij.erlang.psi.ErlangVisitor;
import org.intellij.erlang.psi.impl.ErlangPsiImplUtil;
import org.intellij.erlang.quickfixes.ErlangCreateFunctionQuickFix;
import org.jetbrains.annotations.NotNull;

public class ErlangUnresolvedExportFunctionInspection extends ErlangInspectionBase {
  @NotNull
  @Override
  protected ErlangVisitor buildErlangVisitor(@NotNull final ProblemsHolder holder,
                                             @NotNull LocalInspectionToolSession session) {
    return new ErlangVisitor() {
      @Override
      public void visitExportFunction(@NotNull ErlangExportFunction o) {
        PsiReference reference = o.getReference();
        if (reference.resolve() != null) return;

        int arity = ErlangPsiImplUtil.getArity(o.getInteger());
        ErlangAtom atom = o.getQAtom().getAtom();
        if (arity < 0 || atom == null) return;

        String name = atom.getName();
        String functionPresentation = ErlangPsiImplUtil.createFunctionPresentation(name, arity);
        String fixMessage = "Create Function " + functionPresentation;

        registerProblem(holder, o, "Unresolved function " + functionPresentation,
                        new ErlangCreateFunctionQuickFix(fixMessage, name, arity));
      }
    };
  }
}
