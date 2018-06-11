package com.liferay.npm.portlet.extender;

import java.util.Dictionary;
import java.util.Enumeration;

import javax.portlet.Portlet;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NPMPortletExtender extends BundleTracker<ServiceRegistration<Portlet>> implements BundleActivator {

	public NPMPortletExtender(
			BundleContext context, int stateMask,
			BundleTrackerCustomizer<ServiceRegistration<Portlet>> customizer) {

		super(context, stateMask, customizer);
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		open();
		
		Bundle[] bundles = getBundles();
		
		for (Bundle bundle : bundles) {
			Dictionary<String, String> headers = bundle.getHeaders();
			
			Enumeration<String> keys = headers.keys();
			
			while(keys.hasMoreElements()) {
				String key = keys.nextElement();
				
				if (key.equals("Require-Capability")) {
					String value = headers.get(key);
					
					if (value.contains("osgi.extender;filter:=\"(osgi.extender=liferay.npm.portlet)")) {
						NPMPortletMetadata metadata = _getMetaData();
						
						System.out.println("Registering NPM portlet...");
						
						// Register portlet service using metadata
						
						break;
					}
				}
			}
			
			// p = createPortlet(md)
			// registerPortlet(p, md)
		}
	}

	private NPMPortletMetadata _getMetaData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		close();
	}

	private static final Logger _logger = LoggerFactory.getLogger(NPMPortletExtender.class);

}
