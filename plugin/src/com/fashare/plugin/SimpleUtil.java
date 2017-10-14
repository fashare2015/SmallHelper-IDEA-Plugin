package com.fashare.plugin;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SimpleUtil {
  public static final String TAG = "SimpleUtil: ";

  public static <T extends PsiElement> List<T> findProperties(Project project, String key, Class<T> clazz) {
    List<T> result = null;
    Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project));
    for (VirtualFile virtualFile : virtualFiles) {
      PsiJavaFile psiJavaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(virtualFile);

      if(psiJavaFile.getName().equals("R.java"))
        continue;

//      System.out.println(TAG + "=======> " + psiJavaFile.getName());
      if (psiJavaFile != null) {
        Collection<T> properties = PsiTreeUtil.findChildrenOfType(psiJavaFile, clazz);
        if (properties != null) {
          for (T property : properties) {
//            System.out.println(TAG + property.getText() + ", " + key);
            if (property.getText().startsWith(key)) {
//              System.out.println(TAG + "find !!!\n");
              if (result == null) {
                result = new ArrayList<>();
              }
              result.add(property);
            }
          }
        }
      }

//      System.out.println(TAG + "<======= " + psiJavaFile.getName() + "\n\n\n\n");
    }
    return result != null ? result : Collections.emptyList();
  }



  public static <T extends PsiElement> List<T> findProperties2(Project project, String key, Class<T> clazz) {
    List<T> result = null;
    Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, JavaFileType.INSTANCE, GlobalSearchScope.allScope(project));
    for (VirtualFile virtualFile : virtualFiles) {
      PsiJavaFile psiJavaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(virtualFile);
      if (psiJavaFile != null) {
        T[] properties = PsiTreeUtil.getChildrenOfType(psiJavaFile, clazz);
        if (properties != null) {
          for (T property : properties) {
            if (property.getText().contains(key)) {
              if (result == null) {
                result = new ArrayList<>();
              }
              result.add(property);
            }
          }
        }
      }
    }
    return result != null ? result : Collections.emptyList();
  }
}
