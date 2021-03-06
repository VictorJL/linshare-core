/*
 * LinShare is an open source filesharing software developed by LINAGORA.
 * 
 * Copyright (C) 2020 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display in the interface of the “LinShare™”
 * trademark/logo, the "Libre & Free" mention, the words “You are using the Free
 * and Open Source version of LinShare™, powered by Linagora © 2009–2020.
 * Contribute to Linshare R&D by subscribing to an Enterprise offer!”. You must
 * also retain the latter notice in all asynchronous messages such as e-mails
 * sent with the Program, (ii) retain all hypertext links between LinShare and
 * http://www.linshare.org, between linagora.com and Linagora, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linshare.org/licenses/LinShare-License_AfferoGPL-v3.pdf> for more
 * details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for LinShare along with this program. If not,
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License
 * version 3 and
 * <http://www.linshare.org/licenses/LinShare-License_AfferoGPL-v3.pdf> for the
 * Additional Terms applicable to LinShare software.
 */

package org.linagora.linshare.core.dao.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.Validate;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.linagora.linshare.core.dao.JcloudObjectStorageFileDataStore;
import org.linagora.linshare.core.dao.impl.DefaultJcloudFileDataStoreImpl;
import org.linagora.linshare.core.dao.impl.FileSystemJcloudFileDataStoreImpl;
import org.linagora.linshare.core.dao.impl.OpenStackSwiftJcloudFileDataStoreImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Module;

public class JcloudObjectStorageFileDataStoreFactory {

	protected static final Logger logger = LoggerFactory.getLogger(JcloudObjectStorageFileDataStoreFactory.class);
	protected static String FILESYSTEM = "filesystem";
	protected static String OPENSTACK_SWIFT = "openstack-swift";
	// Do not support object storage region
	protected static String SWIFT_KEYSTONE = "swift-keystone";
	protected static String S3 = "s3";

	protected String provider;
	protected String supportedProviders;
	protected List<String> supportedProvidersList;
	protected BlobStoreContext context;
	protected String baseDirectory;
	protected String credential;
	protected String endpoint;
	protected String regionId;
	protected String bucketIdentifier;
	protected boolean multipartUpload;

	protected IdentityBuilder identityBuilder = IdentityBuilder.New();
	protected String projectName;
	protected String keystoneVersion;
	protected Iterable<Module> modules = ImmutableSet.<Module> of(new SLF4JLoggingModule());
	protected Map<String, String> jcloudProperties = Maps.newHashMap();

	public JcloudObjectStorageFileDataStore getDefault() {
		this.supportedProvidersList = Arrays.asList(supportedProviders.split(","));
		Validate.notEmpty(provider, "Missing provider");
		logger.debug("Defined provider: {}", provider);
		if (!this.supportedProviders.contains(provider)) {
			throw new IllegalArgumentException("Supported providers: " + this.supportedProviders.toString());
		}
		Properties properties = new Properties();
		jcloudProperties.forEach((k, v) ->  { logger.debug("jcloudProperties: {}={}", k, v);properties.setProperty(k, v);});
		if (provider.equals(FILESYSTEM)) {
			return new FileSystemJcloudFileDataStoreImpl(modules, properties, bucketIdentifier, baseDirectory);
		} else if (provider.equals(OPENSTACK_SWIFT)) {
			ContextBuilder contextBuilder = getContextBuilder();
			return new OpenStackSwiftJcloudFileDataStoreImpl(contextBuilder, properties, bucketIdentifier, regionId,
					keystoneVersion, projectName, multipartUpload);
		} else {
			ContextBuilder contextBuilder = getContextBuilder();
			return new DefaultJcloudFileDataStoreImpl(contextBuilder, properties, bucketIdentifier, multipartUpload);
		}
	}

	public ContextBuilder getContextBuilder() {
		Validate.notEmpty(credential, "Missing credential");
		Validate.notEmpty(endpoint, "Missing endpoint");
		ContextBuilder contextBuilder = ContextBuilder.newBuilder(provider);
		contextBuilder.endpoint(endpoint)
			.credentials(identityBuilder.build(), credential)
			.modules(modules);
		return contextBuilder;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public void setSupportedProviders(String supportedProviders) {
		this.supportedProviders = supportedProviders;
	}

	public void setBaseDirectory(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public void setIdentity(String identity) {
		identityBuilder.identity(identity);
	}

	public void setUserName(String userName) {
		identityBuilder.userName(userName);
	}

	public void setDomainName(String userDomainName) {
		identityBuilder.userDomainName(userDomainName);
	}

	public void setTenantName(String tenantName) {
		identityBuilder.tenantName(tenantName);
	}

	public void setCredential(String credential) {
		this.credential = credential;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public void setBucketIdentifier(String bucketIdentifier) {
		this.bucketIdentifier = bucketIdentifier;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public void setKeystoneVersion(String keystoneVersion) {
		this.keystoneVersion = keystoneVersion;
	}

	public void setMultipartUpload(boolean multipartUpload) {
		this.multipartUpload = multipartUpload;
	}

	public void setJcloudProperties(Map<String, String> jcloudProperties) {
		this.jcloudProperties = jcloudProperties;
	}
}
