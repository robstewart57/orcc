/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.sf.orcc.cache;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc --> The <b>Package</b> for the model. It contains
 * accessors for the meta objects to represent
 * <ul>
 * <li>each class,</li>
 * <li>each feature of each class,</li>
 * <li>each enum,</li>
 * <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see net.sf.orcc.cache.CacheFactory
 * @model kind="package"
 * @generated
 */
public interface CachePackage extends EPackage {
	/**
	 * <!-- begin-user-doc --> Defines literals for the meta objects that
	 * represent
	 * <ul>
	 * <li>each class,</li>
	 * <li>each feature of each class,</li>
	 * <li>each enum,</li>
	 * <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '
		 * {@link net.sf.orcc.cache.impl.CacheImpl <em>Cache</em>}' class. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @see net.sf.orcc.cache.impl.CacheImpl
		 * @see net.sf.orcc.cache.impl.CachePackageImpl#getCache()
		 * @generated
		 */
		EClass CACHE = eINSTANCE.getCache();
		/**
		 * The meta object literal for the '<em><b>Type Map</b></em>' map feature.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @generated
		 */
		EReference CACHE__TYPE_MAP = eINSTANCE.getCache_TypeMap();
		/**
		 * The meta object literal for the '{@link net.sf.orcc.cache.impl.CacheManagerImpl <em>Manager</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see net.sf.orcc.cache.impl.CacheManagerImpl
		 * @see net.sf.orcc.cache.impl.CachePackageImpl#getCacheManager()
		 * @generated
		 */
		EClass CACHE_MANAGER = eINSTANCE.getCacheManager();
		/**
		 * The meta object literal for the '{@link net.sf.orcc.cache.impl.EStringToTypeMapEntryImpl <em>EString To Type Map Entry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see net.sf.orcc.cache.impl.EStringToTypeMapEntryImpl
		 * @see net.sf.orcc.cache.impl.CachePackageImpl#getEStringToTypeMapEntry()
		 * @generated
		 */
		EClass ESTRING_TO_TYPE_MAP_ENTRY = eINSTANCE.getEStringToTypeMapEntry();
		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ESTRING_TO_TYPE_MAP_ENTRY__KEY = eINSTANCE.getEStringToTypeMapEntry_Key();
		/**
		 * The meta object literal for the '<em><b>Value</b></em>' reference feature.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @generated
		 */
		EReference ESTRING_TO_TYPE_MAP_ENTRY__VALUE = eINSTANCE.getEStringToTypeMapEntry_Value();

	}

	/**
	 * The meta object id for the '{@link net.sf.orcc.cache.impl.CacheImpl <em>Cache</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see net.sf.orcc.cache.impl.CacheImpl
	 * @see net.sf.orcc.cache.impl.CachePackageImpl#getCache()
	 * @generated
	 */
	int CACHE = 0;

	/**
	 * The feature id for the '<em><b>Type Map</b></em>' map. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int CACHE__TYPE_MAP = 0;

	/**
	 * The number of structural features of the '<em>Cache</em>' class. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int CACHE_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link net.sf.orcc.cache.impl.CacheManagerImpl <em>Manager</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see net.sf.orcc.cache.impl.CacheManagerImpl
	 * @see net.sf.orcc.cache.impl.CachePackageImpl#getCacheManager()
	 * @generated
	 */
	int CACHE_MANAGER = 1;

	/**
	 * The number of structural features of the '<em>Manager</em>' class. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int CACHE_MANAGER_FEATURE_COUNT = 0;

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 */
	CachePackage eINSTANCE = net.sf.orcc.cache.impl.CachePackageImpl.init();

	/**
	 * The package name.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "cache";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "net.sf.orcc.cal.cache";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://orcc.sf.net/cache";

	/**
	 * The meta object id for the '{@link net.sf.orcc.cache.impl.EStringToTypeMapEntryImpl <em>EString To Type Map Entry</em>}' class.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see net.sf.orcc.cache.impl.EStringToTypeMapEntryImpl
	 * @see net.sf.orcc.cache.impl.CachePackageImpl#getEStringToTypeMapEntry()
	 * @generated
	 */
	int ESTRING_TO_TYPE_MAP_ENTRY = 2;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int ESTRING_TO_TYPE_MAP_ENTRY__KEY = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' reference. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int ESTRING_TO_TYPE_MAP_ENTRY__VALUE = 1;

	/**
	 * The number of structural features of the '<em>EString To Type Map Entry</em>' class.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ESTRING_TO_TYPE_MAP_ENTRY_FEATURE_COUNT = 2;

	/**
	 * Returns the meta object for class '{@link net.sf.orcc.cache.Cache <em>Cache</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Cache</em>'.
	 * @see net.sf.orcc.cache.Cache
	 * @generated
	 */
	EClass getCache();

	/**
	 * Returns the meta object for the map '
	 * {@link net.sf.orcc.cache.Cache#getTypeMap <em>Type Map</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the map '<em>Type Map</em>'.
	 * @see net.sf.orcc.cache.Cache#getTypeMap()
	 * @see #getCache()
	 * @generated
	 */
	EReference getCache_TypeMap();

	/**
	 * Returns the factory that creates the instances of the model. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	CacheFactory getCacheFactory();

	/**
	 * Returns the meta object for class '{@link net.sf.orcc.cache.CacheManager <em>Manager</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Manager</em>'.
	 * @see net.sf.orcc.cache.CacheManager
	 * @generated
	 */
	EClass getCacheManager();

	/**
	 * Returns the meta object for class '{@link java.util.Map.Entry <em>EString To Type Map Entry</em>}'.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @return the meta object for class '<em>EString To Type Map Entry</em>'.
	 * @see java.util.Map.Entry
	 * @model keyDataType="org.eclipse.emf.ecore.EString"
	 *        valueType="net.sf.orcc.ir.Type"
	 * @generated
	 */
	EClass getEStringToTypeMapEntry();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Key</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see java.util.Map.Entry
	 * @see #getEStringToTypeMapEntry()
	 * @generated
	 */
	EAttribute getEStringToTypeMapEntry_Key();

	/**
	 * Returns the meta object for the reference '{@link java.util.Map.Entry <em>Value</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Value</em>'.
	 * @see java.util.Map.Entry
	 * @see #getEStringToTypeMapEntry()
	 * @generated
	 */
	EReference getEStringToTypeMapEntry_Value();

} // CachePackage
