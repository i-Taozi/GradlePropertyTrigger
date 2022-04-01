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

package org.intellij.erlang.index;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ErlangApplicationIndex extends ScalarIndexExtension<String> {
  private static final ID<String, Void> ERLANG_APPLICATION_INDEX = ID.create("ErlangApplicationIndex");

  private static final FileBasedIndex.InputFilter INPUT_FILTER = new ErlangApplicationInputFilter();
  private static final int INDEX_VERSION = 1;
  private static final KeyDescriptor<String> KEY_DESCRIPTOR = new EnumeratorStringDescriptor();
  private static final DataIndexer<String, Void, FileContent> DATA_INDEXER = new ErlangApplicationDataIndexer();
  private static final String DOT_APP_SRC = ".app.src";
  private static final String DOT_APP = ".app";

  @NotNull
  @Override
  public ID<String, Void> getName() {
    return ERLANG_APPLICATION_INDEX;
  }

  @NotNull
  @Override
  public DataIndexer<String, Void, FileContent> getIndexer() {
    return DATA_INDEXER;
  }

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return KEY_DESCRIPTOR;
  }

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return INPUT_FILTER;
  }

  @Override
  public boolean dependsOnFileContent() {
    return false;
  }

  @Override
  public int getVersion() {
    return INDEX_VERSION;
  }

  @Nullable
  public static VirtualFile getApplicationDirectoryByName(@NotNull String appName, @NotNull GlobalSearchScope searchScope) {
    ApplicationPathExtractingProcessor processor = new ApplicationPathExtractingProcessor();
    FileBasedIndex.getInstance().processValues(ERLANG_APPLICATION_INDEX, appName, null, processor, searchScope);
    Project project = searchScope.getProject();
    if (project != null) {
      processAppFiles(getAppFilesFromEbinDirectories(project, appName), appName, processor);
    }

    return processor.getApplicationPath();
  }

  public static List<VirtualFile> getAllApplicationDirectories(@NotNull Project project, @NotNull final GlobalSearchScope searchScope) {
    final ArrayList<VirtualFile> result = new ArrayList<>();
    final FileBasedIndex index = FileBasedIndex.getInstance();
    final List<VirtualFile> appFilesFromEbinDirectories = getAppFilesFromEbinDirectories(project, null);

    index.processAllKeys(ERLANG_APPLICATION_INDEX, appName -> {
      ApplicationPathExtractingProcessor processor = new ApplicationPathExtractingProcessor();
      index.processValues(ERLANG_APPLICATION_INDEX, appName, null, processor, searchScope);
      processAppFiles(appFilesFromEbinDirectories, appName, processor);
      //TODO examine: processor does not get called for some appNames when running
      //              ErlangSmallIdeHighlightingTest.testIncludeFromOtpIncludeDirResolve()
      //              it seems, that index is reused for different tests, thus we obtain keys (appNames)
      //              which are not valid anymore...
      ContainerUtil.addIfNotNull(result, processor.getApplicationPath());
      return true;
    }, project);

    return result;
  }

  private static void processAppFiles(List<VirtualFile> appFiles, String appName, FileBasedIndex.ValueProcessor<Void> processor) {
    for (VirtualFile appFile : appFiles) {
      if (appName.equals(getApplicationName(appFile))) {
        processor.process(appFile, null);
      }
    }
  }

  private static List<VirtualFile> getAppFilesFromEbinDirectories(@NotNull Project project, @Nullable String appName) {
    List<VirtualFile> appFiles = new ArrayList<>();
    for (Module m : ModuleManager.getInstance(project).getModules()) {
      CompilerModuleExtension moduleExtension = ModuleRootManager.getInstance(m).getModuleExtension(CompilerModuleExtension.class);
      VirtualFile outputDir = moduleExtension != null ? moduleExtension.getCompilerOutputPath() : null;
      if (outputDir == null || !outputDir.isDirectory() || !"ebin".equals(outputDir.getName())) continue;

      if (appName != null) {
        VirtualFile appFile = outputDir.findChild(appName + DOT_APP);
        if (appFile != null && !appFile.isDirectory()) {
          appFiles.add(appFile);
        }
        continue;
      }

      for (VirtualFile file : outputDir.getChildren()) {
        if (ErlangApplicationInputFilter.isApplicationFile(file)) {
          appFiles.add(file);
        }
      }
    }
    return appFiles;
  }

  @Nullable
  private static VirtualFile getLibraryDirectory(VirtualFile appFile) {
    VirtualFile parent = appFile.getParent();
    return parent != null ? parent.getParent() : null;
  }

  private static class ApplicationPathExtractingProcessor implements FileBasedIndex.ValueProcessor<Void> {
    private VirtualFile myPath = null;

    @Override
    public boolean process(@NotNull VirtualFile appFile, @Nullable Void value) {
      VirtualFile libDir = getLibraryDirectory(appFile);
      if (libDir == null) return true;
      String appName = getApplicationName(appFile);
      //applications with no version specification have higher priority
      if (myPath == null || appName.equals(libDir.getName())) {
        myPath = libDir;
        return true;
      }
      if (appName.equals(myPath.getName())) return true;
      myPath = myPath.getName().compareTo(libDir.getName()) < 0 ? libDir : myPath;
      return true;
    }

    public VirtualFile getApplicationPath() {
      return myPath;
    }
  }

  private static class ErlangApplicationInputFilter implements FileBasedIndex.InputFilter {
    @Override
    public boolean acceptInput(@NotNull VirtualFile file) {
      return isApplicationFile(file) && isInsideEbinOrSrcDirectory(file);
    }

    private static boolean isApplicationFile(@NotNull VirtualFile file) {
      String fileName = file.isDirectory() ? "" : file.getName();
      return fileName.endsWith(DOT_APP) || fileName.endsWith(DOT_APP_SRC);
    }
    
    private static boolean isInsideEbinOrSrcDirectory(VirtualFile file) {
      VirtualFile parentDirectory = file.getParent();
      String parentDirectoryName = parentDirectory != null ? parentDirectory.getName() : null;
      return "src".equals(parentDirectoryName) || "ebin".equals(parentDirectoryName);
    }
  }

  private static class ErlangApplicationDataIndexer implements DataIndexer<String, Void, FileContent> {
    @NotNull
    @Override
    public Map<String, Void> map(@NotNull FileContent inputData) {
      return Collections.singletonMap(getApplicationName(inputData.getFile()), null);
    }
  }

  @NotNull
  private static String getApplicationName(VirtualFile appFile) {
    String filename = appFile.getName();
    return filename.endsWith(DOT_APP_SRC) ? StringUtil.trimEnd(filename, DOT_APP_SRC) : appFile.getNameWithoutExtension();
  }
}
