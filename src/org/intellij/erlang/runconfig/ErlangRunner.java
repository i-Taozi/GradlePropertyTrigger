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

package org.intellij.erlang.runconfig;

import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import org.intellij.erlang.application.ErlangApplicationConfiguration;
import org.intellij.erlang.eunit.ErlangUnitRunConfiguration;
import org.jetbrains.annotations.NotNull;

public class ErlangRunner extends DefaultProgramRunner {
  private static final String ERLANG_RUNNER_ID = "ErlangRunner";

  @NotNull
  @Override
  public String getRunnerId() {
    return ERLANG_RUNNER_ID;
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) &&
      (profile instanceof ErlangApplicationConfiguration || profile instanceof ErlangUnitRunConfiguration);
  }
}