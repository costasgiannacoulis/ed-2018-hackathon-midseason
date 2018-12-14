package org.acme.dvdstore.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLogEntity {
	protected Logger log = LoggerFactory.getLogger(getClass());
}
