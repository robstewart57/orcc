/**
 * <copyright>
 * Copyright (c) 2009-2012, IETR/INSA of Rennes
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the IETR/INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * </copyright>
 */
package net.sf.orcc.backends.tta.architecture.impl;

import java.util.Collection;

import net.sf.dftools.graph.Edge;
import net.sf.dftools.graph.Vertex;
import net.sf.dftools.graph.impl.GraphImpl;
import net.sf.orcc.backends.tta.architecture.ArchitecturePackage;
import net.sf.orcc.backends.tta.architecture.Component;
import net.sf.orcc.backends.tta.architecture.Design;
import net.sf.orcc.backends.tta.architecture.DesignConfiguration;
import net.sf.orcc.backends.tta.architecture.ExternalPort;
import net.sf.orcc.backends.tta.architecture.Fifo;
import net.sf.orcc.backends.tta.architecture.Processor;
import net.sf.orcc.backends.tta.architecture.Signal;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Design</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link net.sf.orcc.backends.tta.architecture.impl.DesignImpl#getName <em>Name</em>}</li>
 *   <li>{@link net.sf.orcc.backends.tta.architecture.impl.DesignImpl#getComponents <em>Components</em>}</li>
 *   <li>{@link net.sf.orcc.backends.tta.architecture.impl.DesignImpl#getProcessors <em>Processors</em>}</li>
 *   <li>{@link net.sf.orcc.backends.tta.architecture.impl.DesignImpl#getFifos <em>Fifos</em>}</li>
 *   <li>{@link net.sf.orcc.backends.tta.architecture.impl.DesignImpl#getSignals <em>Signals</em>}</li>
 *   <li>{@link net.sf.orcc.backends.tta.architecture.impl.DesignImpl#getPorts <em>Ports</em>}</li>
 *   <li>{@link net.sf.orcc.backends.tta.architecture.impl.DesignImpl#getConfiguration <em>Configuration</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DesignImpl extends GraphImpl implements Design {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;
	/**
	 * The cached value of the '{@link #getComponents() <em>Components</em>}' reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getComponents()
	 * @generated
	 * @ordered
	 */
	protected EList<Component> components;
	/**
	 * The cached value of the '{@link #getProcessors() <em>Processors</em>}' reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getProcessors()
	 * @generated
	 * @ordered
	 */
	protected EList<Processor> processors;
	/**
	 * The cached value of the '{@link #getFifos() <em>Fifos</em>}' reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getFifos()
	 * @generated
	 * @ordered
	 */
	protected EList<Fifo> fifos;
	/**
	 * The cached value of the '{@link #getSignals() <em>Signals</em>}' reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getSignals()
	 * @generated
	 * @ordered
	 */
	protected EList<Signal> signals;
	/**
	 * The cached value of the '{@link #getPorts() <em>Ports</em>}' reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getPorts()
	 * @generated
	 * @ordered
	 */
	protected EList<ExternalPort> ports;
	/**
	 * The default value of the '{@link #getConfiguration() <em>Configuration</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getConfiguration()
	 * @generated
	 * @ordered
	 */
	protected static final DesignConfiguration CONFIGURATION_EDEFAULT = DesignConfiguration.DIRECT_MAPPING;
	/**
	 * The cached value of the '{@link #getConfiguration() <em>Configuration</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getConfiguration()
	 * @generated
	 * @ordered
	 */
	protected DesignConfiguration configuration = CONFIGURATION_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected DesignImpl() {
		super();
	}

	@Override
	public void add(Edge edge) {
		if (edge instanceof Component) {
			getFifos().add((Fifo) edge);
		} else if (edge instanceof Processor) {
			getSignals().add((Signal) edge);
		}
		getEdges().add(edge);
	}

	@Override
	public void add(Vertex vertex) {
		if (vertex instanceof Component) {
			getComponents().add((Component) vertex);
		} else if (vertex instanceof Processor) {
			getProcessors().add((Processor) vertex);
		} else if (vertex instanceof ExternalPort) {
			getPorts().add((ExternalPort) vertex);
		}
		getVertices().add(vertex);
	}

	@Override
	public Edge add(Vertex source, Vertex target) {
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case ArchitecturePackage.DESIGN__NAME:
			return getName();
		case ArchitecturePackage.DESIGN__COMPONENTS:
			return getComponents();
		case ArchitecturePackage.DESIGN__PROCESSORS:
			return getProcessors();
		case ArchitecturePackage.DESIGN__FIFOS:
			return getFifos();
		case ArchitecturePackage.DESIGN__SIGNALS:
			return getSignals();
		case ArchitecturePackage.DESIGN__PORTS:
			return getPorts();
		case ArchitecturePackage.DESIGN__CONFIGURATION:
			return getConfiguration();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case ArchitecturePackage.DESIGN__NAME:
			return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT
					.equals(name);
		case ArchitecturePackage.DESIGN__COMPONENTS:
			return components != null && !components.isEmpty();
		case ArchitecturePackage.DESIGN__PROCESSORS:
			return processors != null && !processors.isEmpty();
		case ArchitecturePackage.DESIGN__FIFOS:
			return fifos != null && !fifos.isEmpty();
		case ArchitecturePackage.DESIGN__SIGNALS:
			return signals != null && !signals.isEmpty();
		case ArchitecturePackage.DESIGN__PORTS:
			return ports != null && !ports.isEmpty();
		case ArchitecturePackage.DESIGN__CONFIGURATION:
			return configuration != CONFIGURATION_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case ArchitecturePackage.DESIGN__NAME:
			setName((String) newValue);
			return;
		case ArchitecturePackage.DESIGN__COMPONENTS:
			getComponents().clear();
			getComponents().addAll((Collection<? extends Component>) newValue);
			return;
		case ArchitecturePackage.DESIGN__PROCESSORS:
			getProcessors().clear();
			getProcessors().addAll((Collection<? extends Processor>) newValue);
			return;
		case ArchitecturePackage.DESIGN__FIFOS:
			getFifos().clear();
			getFifos().addAll((Collection<? extends Fifo>) newValue);
			return;
		case ArchitecturePackage.DESIGN__SIGNALS:
			getSignals().clear();
			getSignals().addAll((Collection<? extends Signal>) newValue);
			return;
		case ArchitecturePackage.DESIGN__PORTS:
			getPorts().clear();
			getPorts().addAll((Collection<? extends ExternalPort>) newValue);
			return;
		case ArchitecturePackage.DESIGN__CONFIGURATION:
			setConfiguration((DesignConfiguration) newValue);
			return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ArchitecturePackage.Literals.DESIGN;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					ArchitecturePackage.DESIGN__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case ArchitecturePackage.DESIGN__NAME:
			setName(NAME_EDEFAULT);
			return;
		case ArchitecturePackage.DESIGN__COMPONENTS:
			getComponents().clear();
			return;
		case ArchitecturePackage.DESIGN__PROCESSORS:
			getProcessors().clear();
			return;
		case ArchitecturePackage.DESIGN__FIFOS:
			getFifos().clear();
			return;
		case ArchitecturePackage.DESIGN__SIGNALS:
			getSignals().clear();
			return;
		case ArchitecturePackage.DESIGN__PORTS:
			getPorts().clear();
			return;
		case ArchitecturePackage.DESIGN__CONFIGURATION:
			setConfiguration(CONFIGURATION_EDEFAULT);
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Component> getComponents() {
		if (components == null) {
			components = new EObjectResolvingEList<Component>(Component.class,
					this, ArchitecturePackage.DESIGN__COMPONENTS);
		}
		return components;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public DesignConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Fifo> getFifos() {
		if (fifos == null) {
			fifos = new EObjectResolvingEList<Fifo>(Fifo.class, this,
					ArchitecturePackage.DESIGN__FIFOS);
		}
		return fifos;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EList<ExternalPort> getPorts() {
		if (ports == null) {
			ports = new EObjectResolvingEList<ExternalPort>(ExternalPort.class,
					this, ArchitecturePackage.DESIGN__PORTS);
		}
		return ports;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Processor> getProcessors() {
		if (processors == null) {
			processors = new EObjectResolvingEList<Processor>(Processor.class,
					this, ArchitecturePackage.DESIGN__PROCESSORS);
		}
		return processors;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Signal> getSignals() {
		if (signals == null) {
			signals = new EObjectResolvingEList<Signal>(Signal.class, this,
					ArchitecturePackage.DESIGN__SIGNALS);
		}
		return signals;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public void setConfiguration(DesignConfiguration newConfiguration) {
		DesignConfiguration oldConfiguration = configuration;
		configuration = newConfiguration == null ? CONFIGURATION_EDEFAULT
				: newConfiguration;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					ArchitecturePackage.DESIGN__CONFIGURATION,
					oldConfiguration, configuration));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy())
			return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (name: ");
		result.append(name);
		result.append(", configuration: ");
		result.append(configuration);
		result.append(')');
		return result.toString();
	}

} // DesignImpl
