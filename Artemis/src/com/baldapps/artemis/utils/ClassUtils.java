package com.baldapps.artemis.utils;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.SemanticQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;

@SuppressWarnings("restriction")
public class ClassUtils {

	/**
	 * Returns all direct and indirect base classes with
	 * public or protected inheritance
	 *
	 * @param classType a class
	 * @return An array of base classes in arbitrary order.
	 */
	public static ICPPClassType[] getAllVisibleBases(ICPPClassType classType) {
		Set<ICPPClassType> result = new HashSet<>();
		result.add(classType);
		getAllVisibileBases(classType, result);
		result.remove(classType);
		return result.toArray(new ICPPClassType[result.size()]);
	}

	private static void getAllVisibileBases(ICPPClassType classType, Set<ICPPClassType> result) {
		ICPPBase[] bases = classType.getBases();
		for (ICPPBase base : bases) {
			if (base.getVisibility() == ICPPBase.v_private)
				continue;
			IBinding b = base.getBaseClass();
			if (b instanceof ICPPClassType) {
				final ICPPClassType baseClass = (ICPPClassType) b;
				if (result.add(baseClass)) {
					getAllVisibileBases(baseClass, result);
				}
			}
		}
	}

	/**
	 * Checks that specified declaration is a class constructor
	 * (it is a class member and its name is equal to the class name)
	 */
	public static ICPPConstructor getConstructor(IASTDeclaration decl) {
		if (decl instanceof ICPPASTFunctionDefinition) {
			ICPPASTFunctionDefinition functionDefinition = (ICPPASTFunctionDefinition) decl;
			if (functionDefinition.isDeleted())
				return null;
			IBinding binding = functionDefinition.getDeclarator().getName().resolveBinding();
			if (binding instanceof ICPPConstructor) {
				ICPPConstructor constructor = (ICPPConstructor) binding;
				// Skip defaulted copy and move constructors.
				if (functionDefinition.isDefaulted() && SemanticQueries.isCopyOrMoveConstructor(constructor))
					return null;
				if (constructor.getClassOwner().getKey() == ICompositeType.k_union)
					return null;
				// Skip delegating constructors.
				for (ICPPASTConstructorChainInitializer memberInitializer : functionDefinition
						.getMemberInitializers()) {
					IASTName memberName = memberInitializer.getMemberInitializerId();
					if (memberName != null) {
						IBinding memberBinding = memberName.resolveBinding();
						ICPPClassType classType = null;
						if (memberBinding instanceof ICPPConstructor) {
							classType = ((ICPPConstructor) memberBinding).getClassOwner();
						}
						if (classType instanceof ICPPDeferredClassInstance) {
							classType = ((ICPPDeferredClassInstance) classType).getClassTemplate();
						}
						if (classType != null && classType.isSameType(constructor.getClassOwner()))
							return null;
					}
				}
				return constructor;
			}
		}

		return null;
	}

	/**
	 * Checks that specified declaration is a class destructor
	 */
	public static ICPPMethod getDestructor(IASTDeclaration decl) {
		if (decl instanceof ICPPASTFunctionDefinition) {
			ICPPASTFunctionDefinition functionDefinition = (ICPPASTFunctionDefinition) decl;
			if (functionDefinition.isDeleted())
				return null;
			IBinding binding = functionDefinition.getDeclarator().getName().resolveBinding();
			if (binding instanceof ICPPMethod) {
				ICPPMethod method = (ICPPMethod) binding;
				if (method.isDestructor())
					return method;
			}
		}
		return null;
	}
}
