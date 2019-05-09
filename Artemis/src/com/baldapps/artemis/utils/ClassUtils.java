package com.baldapps.artemis.utils;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;

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
}
