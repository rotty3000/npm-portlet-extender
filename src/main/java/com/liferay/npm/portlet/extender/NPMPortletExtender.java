package com.liferay.npm.portlet.extender;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.Portlet;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.namespace.extender.ExtenderNamespace;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.portlet.LiferayPortlet;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.StringUtil;

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
							URL jsonURL = bundle.getResource("META-INF/resources/package.json");

							URLConnection urlConnection = null;
							InputStream inputStream = null;
							JSONObject jsonObject = null;

							try {
								urlConnection = jsonURL.openConnection();

								inputStream = urlConnection.getInputStream();

								jsonObject = JSONFactoryUtil.createJSONObject(StringUtil.read(inputStream));
							}
							catch (IOException ioe) {
								_logger.error(ioe.getLocalizedMessage());

								return null;
							}
							catch (JSONException jsone) {
								_logger.error(jsone.getLocalizedMessage());

								return null;
							}

							System.out.println("json object: " + jsonObject.toString());

							// register portlet service
							Bundle extenderBundle = FrameworkUtil.getBundle(this.getClass());
							
							BundleContext extenderBundleContext = extenderBundle.getBundleContext();
							
							Dictionary<String, String> serviceProperties = new HashMapDictionary<>();
							
							JSONObject portletJSONObject = jsonObject.getJSONObject("portlet");
							
							if (portletJSONObject != null) {
								Iterator<String> keys = portletJSONObject.keys();
								
								while (keys.hasNext()) {
									String key = keys.next();

									serviceProperties.put(key, portletJSONObject.getString(key));
								}
							}
							
							Portlet portlet = new LiferayPortlet() {
								
								@Override
								public void render(RenderRequest request, RenderResponse response) {
									PrintWriter writer = null;;

									try {
										writer = response.getWriter();
									}
									catch (IOException ioe) {
										_logger.error(ioe.getLocalizedMessage());
									}
									
									writer.println("<p>Hello, world!</p>");
									
									URL jsonURL = bundle.getResource("META-INF/resources/div.txt");

									URLConnection urlConnection = null;
									InputStream inputStream = null;
									String div = null;
									
									try {
										urlConnection = jsonURL.openConnection();

										inputStream = urlConnection.getInputStream();

										div = StringUtil.read(inputStream);
									}
									catch (IOException ioe) {
										ioe.printStackTrace();
									}

									writer.write(div);
								}

							};

							ServiceRegistration<Portlet> serviceRegistration =
								extenderBundleContext.registerService(Portlet.class, portlet, serviceProperties);
							
							return serviceRegistration;
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

	private static final Logger _logger = LoggerFactory.getLogger(NPMPortletExtender.class);

	private BundleTracker<ServiceRegistration<Portlet>> _bundleTracker;

}