// This is a generated file. Not intended for manual editing.
package org.intellij.erlang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ErlangFunTypeSigs extends ErlangCompositeElement {

  @Nullable
  ErlangModuleRef getModuleRef();

  @NotNull
  ErlangSpecFun getSpecFun();

  @NotNull
  List<ErlangTypeSig> getTypeSigList();

  @Nullable
  PsiElement getColon();

  @Nullable
  PsiElement getColonColon();

}
