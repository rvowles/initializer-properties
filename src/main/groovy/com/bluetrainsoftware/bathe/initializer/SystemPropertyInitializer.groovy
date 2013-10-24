package com.bluetrainsoftware.bathe.initializer

import bathe.BatheInitializer
import groovy.transform.CompileStatic

/**
 * Loads system properties from location specified by -p or -P flag, ensures there are no duplicates. This
 * needs to be done before almost anything else, so we give it a very high priority.
 *
 * author: Richard Vowles - http://gplus.to/Richard.Vowles
 */
@CompileStatic
class SystemPropertyInitializer implements BatheInitializer {
	private static final String MINUS_D = "-D";
	private static final String MINUS_P = "-P";

	@Override
	int getOrder() {
		return -1
	}

	@Override
	String getName() {
		return "bathe-system-property"
	}

	@Override
	String[] initialize(String[] args, String jumpClass) {
		List<String> appArguments = []

		for (String arg : args) {
			if (arg.startsWith(MINUS_D)) {
				String property = arg.substring(MINUS_D.length());
				int equals = property.indexOf('=');
				if (equals >= 0)
					System.setProperty(property.substring(0, equals), property.substring(equals + 1));
				else
					System.setProperty(property, Boolean.TRUE.toString());
			} else if (arg.startsWith(MINUS_P)) {
				File properties = new File(arg.substring(MINUS_P.length()));
				System.getProperties().putAll(loadProperties(properties));
			} else
				appArguments.add(arg);
		}

		return appArguments.toArray(new String[appArguments.size()])
	}

	protected Properties loadProperties(File file) {
		Properties values = new DuplicateProperties()

		if (file.exists()) {
			try {
				file.withInputStream { InputStream is ->
					values.load(is)
				}
			} catch (IOException e) {
				throw new RuntimeException(String.format("Failed to read properties file '%s'", file), e);
			}
		}

		return values;
	}

	/**
	 * This allows us to fail fast for duplicate system properties
	 */
	protected static final class DuplicateProperties extends Properties {

		@Override
		public synchronized Object put(Object key, Object value) {
			Object previous = super.put(key, value);

			if (previous != null) {
				throw new IllegalStateException(String.format("Key '%s' has duplicate values as %s and %s", key, previous.toString(), value.toString()));
			}

			return null;
		}
	}
}
