package com.liferay.npm.portlet.extender;

import java.util.List;
import java.util.Map;

import javax.portlet.Portlet;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.namespace.extender.ExtenderNamespace;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NPMPortletExtender implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		_bundleTracker = new BundleTracker<>(context, Bundle.ACTIVE,
				new BundleTrackerCustomizer<ServiceRegistration<Portlet>>() {

					@Override
					public ServiceRegistration<Portlet> addingBundle(Bundle bundle, BundleEvent event) {
						if (_optIn(bundle)) {
							System.out.println("Found bundle with opt-in: " + bundle.getSymbolicName());
							
							// read package.json metadata
							
							// register portlet service
						}

						return null;
					}

					@Override
					public void modifiedBundle(Bundle bundle, BundleEvent event, ServiceRegistration<Portlet> registration) {

					}

					@Override
					public void removedBundle(Bundle bundle, BundleEvent event, ServiceRegistration<Portlet> registration) {
						if (registration != null) {
							registration.unregister();
						}
					}

				});
		
		_bundleTracker.open();
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		_bundleTracker.close();
	}

	private boolean _optIn(Bundle bundle) {
		BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
		
		List<BundleWire> bundleWires = bundleWiring.getRequiredWires(ExtenderNamespace.EXTENDER_NAMESPACE);
		
		for (BundleWire bundleWire : bundleWires) {
			BundleCapability bundleCapability = bundleWire.getCapability();
			
			Map<String, Object> attributes = bundleCapability.getAttributes();
			
			Object value = attributes.get(ExtenderNamespace.EXTENDER_NAMESPACE);
			
			if ((value != null) && value.equals("liferay.npm.portlet")) {
				return true;
			}
		}
		
		return false;
	}

	private NPMPortletMetadata _getMetaData() {
		// TODO Auto-generated method stub
		return null;
	}

	private static final Logger _logger = LoggerFactory.getLogger(NPMPortletExtender.class);

	private BundleTracker<ServiceRegistration<Portlet>> _bundleTracker;

}