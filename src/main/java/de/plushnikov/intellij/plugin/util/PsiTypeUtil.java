package de.plushnikov.intellij.plugin.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiKeyword;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.PsiWildcardType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PsiTypeUtil {

  @NotNull
  public static PsiType extractOneElementType(@NotNull PsiType psiType, @NotNull PsiManager psiManager) {
    return extractOneElementType(psiType, psiManager, CommonClassNames.JAVA_LANG_ITERABLE, 0);
  }

  @NotNull
  public static PsiType extractOneElementType(@NotNull PsiType psiType, @NotNull PsiManager psiManager, final String superClass, final int paramIndex) {
    PsiType oneElementType = PsiUtil.substituteTypeParameter(psiType, superClass, paramIndex, true);
    if (oneElementType instanceof PsiWildcardType) {
      oneElementType = ((PsiWildcardType) oneElementType).getBound();
    }
    if (null == oneElementType) {
      oneElementType = PsiType.getJavaLangObject(psiManager, GlobalSearchScope.allScope(psiManager.getProject()));
    }
    return oneElementType;
  }

  @NotNull
  public static PsiType extractAllElementType(@NotNull PsiType psiType, @NotNull PsiManager psiManager) {
    return extractAllElementType(psiType, psiManager, CommonClassNames.JAVA_LANG_ITERABLE, 0);
  }

  @NotNull
  public static PsiType extractAllElementType(@NotNull PsiType psiType, @NotNull PsiManager psiManager, final String superClass, final int paramIndex) {
    PsiType oneElementType = PsiUtil.substituteTypeParameter(psiType, superClass, paramIndex, true);
    if (oneElementType instanceof PsiWildcardType) {
      oneElementType = ((PsiWildcardType) oneElementType).getBound();
    }

    PsiType result;
    final PsiClassType javaLangObject = PsiType.getJavaLangObject(psiManager, GlobalSearchScope.allScope(psiManager.getProject()));
    if (null == oneElementType || Comparing.equal(javaLangObject, oneElementType)) {
      result = PsiWildcardType.createUnbounded(psiManager);
    } else {
      result = PsiWildcardType.createExtends(psiManager, oneElementType);
    }

    return result;
  }

  @NotNull
  public static PsiType createCollectionType(@NotNull PsiManager psiManager, final String collectionQualifiedName, @NotNull PsiType... psiTypes) {
    final Project project = psiManager.getProject();
    final GlobalSearchScope globalsearchscope = GlobalSearchScope.allScope(project);
    final JavaPsiFacade facade = JavaPsiFacade.getInstance(project);

    PsiClass genericClass = facade.findClass(collectionQualifiedName, globalsearchscope);

    return JavaPsiFacade.getElementFactory(project).createType(genericClass, psiTypes);
  }


  @NotNull
  public static PsiType[] extractTypeParameters(@NotNull PsiType psiType, @NotNull PsiManager psiManager) {
    if (!(psiType instanceof PsiClassType)) {
      return PsiType.EMPTY_ARRAY;
    }

    final PsiClassType classType = (PsiClassType) psiType;
    final PsiClassType.ClassResolveResult classResolveResult = classType.resolveGenerics();
    final PsiClass psiClass = classResolveResult.getElement();
    if (psiClass == null) {
      return PsiType.EMPTY_ARRAY;
    }
    final PsiSubstitutor psiSubstitutor = classResolveResult.getSubstitutor();

    final PsiTypeParameter[] typeParameters = psiClass.getTypeParameters();

    final PsiType[] psiTypes = PsiType.createArray(typeParameters.length);
    for (int i = 0; i < typeParameters.length; i++) {
      PsiType psiSubstituteKeyType = psiSubstitutor.substitute(typeParameters[i]);
      if (null == psiSubstituteKeyType) {
        psiSubstituteKeyType = PsiType.getJavaLangObject(psiManager, GlobalSearchScope.allScope(psiManager.getProject()));
      }
      psiTypes[i] = psiSubstituteKeyType;
    }
    return psiTypes;
  }

  @NotNull
  public static PsiClassType getGenericCollectionClassType(@NotNull PsiType psiType, @NotNull Project project, @NotNull String qualifiedName) {
    final PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
    final JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
    final GlobalSearchScope globalsearchscope = GlobalSearchScope.allScope(project);

    PsiClass genericClass = facade.findClass(qualifiedName, globalsearchscope);
    if (null != genericClass) {
      PsiSubstitutor genericSubstitutor = PsiSubstitutor.EMPTY.putAll(genericClass, new PsiType[]{psiType});
      return elementFactory.createType(genericClass, genericSubstitutor);
    }
    return elementFactory.createTypeByFQClassName(qualifiedName, globalsearchscope);
  }

  @NotNull
  public static PsiClassType getCollectionClassType(@NotNull PsiClassType psiType, @NotNull Project project, @NotNull String qualifiedName) {
    final PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
    final JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
    final GlobalSearchScope globalsearchscope = GlobalSearchScope.allScope(project);

    PsiClass genericClass = facade.findClass(qualifiedName, globalsearchscope);
    if (null != genericClass) {
      final PsiClassType.ClassResolveResult classResolveResult = psiType.resolveGenerics();
      final PsiSubstitutor derivedSubstitutor = classResolveResult.getSubstitutor();

      final List<PsiType> typeList = new ArrayList<PsiType>(2);
      final Map<String, PsiType> nameTypeMap = new HashMap<String, PsiType>();
      for (Map.Entry<PsiTypeParameter, PsiType> entry : derivedSubstitutor.getSubstitutionMap().entrySet()) {
        final PsiType entryValue = entry.getValue();
        if (null != entryValue) {
          nameTypeMap.put(entry.getKey().getName(), entryValue);
          typeList.add(entryValue);
        }
      }

      PsiSubstitutor genericSubstitutor = PsiSubstitutor.EMPTY;
      final PsiTypeParameter[] typeParameters = genericClass.getTypeParameters();
      for (int i = 0; i < typeParameters.length; i++) {
        final PsiTypeParameter psiTypeParameter = typeParameters[i];
        PsiType mappedType = nameTypeMap.get(psiTypeParameter.getName());
        if (null == mappedType && typeList.size() > i) {
          mappedType = typeList.get(i);
        }
        if (null == mappedType) {
          mappedType = PsiType.getJavaLangObject(PsiManager.getInstance(project), globalsearchscope);
        }

        if (mappedType instanceof PsiWildcardType) {
          mappedType = ((PsiWildcardType) mappedType).getBound();
        }
        genericSubstitutor = genericSubstitutor.put(psiTypeParameter, mappedType);
      }
      return elementFactory.createType(genericClass, genericSubstitutor);
    }
    return elementFactory.createTypeByFQClassName(qualifiedName, globalsearchscope);
  }

  @Nullable
  public static String getQualifiedName(@NotNull PsiType psiType) {
    final PsiClass psiFieldClass = PsiUtil.resolveClassInType(psiType);
    return psiFieldClass != null ? psiFieldClass.getQualifiedName() : null;
  }

  @NotNull
  public static String getReturnValueOfType(@Nullable PsiType type) {
    if (type instanceof PsiPrimitiveType) {
      if (PsiType.BOOLEAN.equals(type)) {
        return PsiKeyword.FALSE;
      } else {
        return "0";
      }
    } else if (PsiType.VOID.equals(type)) {
      return "";
    } else {
      return PsiKeyword.NULL;
    }
  }
}
