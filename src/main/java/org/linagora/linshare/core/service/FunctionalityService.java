package org.linagora.linshare.core.service;

import java.util.Set;

import org.linagora.linshare.core.domain.entities.AbstractDomain;
import org.linagora.linshare.core.domain.entities.Functionality;

public interface FunctionalityService {
	
	public Set<Functionality> getAllFunctionalities(AbstractDomain domain);
	
	public Set<Functionality> getAllFunctionalities(String domain);

}
