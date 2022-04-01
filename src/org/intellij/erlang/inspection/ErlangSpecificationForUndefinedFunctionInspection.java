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

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiReference;
import org.intellij.erlang.ErlangFileType;
import org.intellij.erlang.psi.ErlangFile;
import org.intellij.erlang.psi.ErlangFunTypeSigs;
import org.intellij.erlang.psi.ErlangSpecification;
import org.jetbrains.annotations.NotNull;

public class ErlangSpecificationForUndefinedFunctionInspection extends ErlangInspectionBase {
  @Override
  protected boolean canRunOn(@NotNull ErlangFile file) {
    return StringUtil.endsWith(file.getName(), ErlangFileType.MODULE.getDefaultExtension());
  }

  @Override
  protected void checkFile(@NotNull ErlangFile file, @NotNull final ProblemsHolder problemsHolder) {
    for (ErlangSpecification spec : file.getSpecifications()) {
      //supported functions without modules only for now
      ErlangFunTypeSigs signature = spec.getSignature();
      if (signature != null) {
        PsiReference reference = signature.getReference();
        if (reference != null && reference.resolve() == null) {
          problemsHolder.registerProblem(spec, "Specification for undefined function '" + signature.getSpecFun().getText() + "'");
        }
      }
    }
  }
}
