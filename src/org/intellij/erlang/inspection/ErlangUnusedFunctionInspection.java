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

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.erlang.ErlangFileType;
import org.intellij.erlang.psi.ErlangFile;
import org.intellij.erlang.psi.ErlangFunction;
import org.intellij.erlang.psi.ErlangSpecification;
import org.intellij.erlang.psi.impl.ErlangPsiImplUtil;
import org.intellij.erlang.quickfixes.ErlangExportFunctionFix;
import org.intellij.erlang.quickfixes.ErlangRemoveFunctionFix;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ErlangUnusedFunctionInspection extends ErlangInspectionBase {
  @Override
  protected boolean canRunOn(@NotNull ErlangFile file) {
    return !file.getName().endsWith(ErlangFileType.HEADER.getDefaultExtension()) && !file.isExportedAll();
  }

  @Override
  protected void checkFile(@NotNull ErlangFile file, @NotNull ProblemsHolder holder) {
    for (ErlangFunction function : file.getFunctions()) {
      if (!isUnusedFunction(file, function)) continue;

      PsiElement identifier = function.getNameIdentifier();
      String message = "Unused function " + "'" + function.getName() + "/" + function.getArity() + "'";
      LocalQuickFix[] fixes = {new ErlangRemoveFunctionFix(), new ErlangExportFunctionFix(function)};

      holder.registerProblem(identifier, message, ProblemHighlightType.LIKE_UNUSED_SYMBOL, fixes);
    }
  }

  private static boolean isUnusedFunction(@NotNull ErlangFile file, @NotNull final ErlangFunction function) {
    if (ErlangPsiImplUtil.isEunitImported(file) && ErlangPsiImplUtil.isEunitTestFunction(function)) return false;
    LocalSearchScope scope = new LocalSearchScope(file);
    // filtered specs out
    List<PsiReference> refs = ContainerUtil.filter(ReferencesSearch.search(function, scope).findAll(), psiReference -> {
      PsiElement element = psiReference.getElement();
      return PsiTreeUtil.getParentOfType(element, ErlangSpecification.class) == null && !ErlangPsiImplUtil.isRecursiveCall(element, function);
    });
    return ContainerUtil.getFirstItem(refs) == null;
  }
}
