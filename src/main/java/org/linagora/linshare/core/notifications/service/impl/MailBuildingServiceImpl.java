/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2015-2018 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2018. Contribute to
 * Linshare R&D by subscribing to an Enterprise offer!” infobox and in the
 * e-mails sent with the Program, (ii) retain all hypertext links between
 * LinShare and linshare.org, between linagora.com and Linagora, and (iii)
 * refrain from infringing Linagora intellectual property rights over its
 * trademarks and commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for LinShare along with this program. If not,
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to LinShare software.
 */
package org.linagora.linshare.core.notifications.service.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.linagora.linshare.core.business.service.DomainBusinessService;
import org.linagora.linshare.core.business.service.MailActivationBusinessService;
import org.linagora.linshare.core.business.service.MailConfigBusinessService;
import org.linagora.linshare.core.dao.FileDataStore;
import org.linagora.linshare.core.domain.constants.Language;
import org.linagora.linshare.core.domain.constants.MailActivationType;
import org.linagora.linshare.core.domain.constants.MailContentType;
import org.linagora.linshare.core.domain.entities.AbstractDomain;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.Contact;
import org.linagora.linshare.core.domain.entities.MailActivation;
import org.linagora.linshare.core.domain.entities.MailConfig;
import org.linagora.linshare.core.domain.entities.UploadRequestUrl;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.domain.objects.MailContainerWithRecipient;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.notifications.config.LinShareStringTemplateResolver;
import org.linagora.linshare.core.notifications.context.EmailContext;
import org.linagora.linshare.core.notifications.dto.ContextMetadata;
import org.linagora.linshare.core.notifications.emails.impl.DriveWarnDeletedMemberEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.DriveWarnNewMemberEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.DriveWarnUpdatedMemberEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.EmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.FileWarnOwnerBeforeExpiryEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.GuestAccountNewCreationEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.GuestAccountResetPasswordEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.GuestWarnGuestAboutHisPasswordResetEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.JwtPermanentCreatedEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.JwtPermanentDeletedEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.ShareFileDownloadEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.ShareFileShareDeletedEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.ShareNewShareAcknowledgementEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.ShareNewShareEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.ShareWarnRecipientAboutExpiredSahreEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.ShareWarnRecipientBeforeExpiryEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.ShareWarnSenderAboutShareExpirationEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.ShareWarnUndownloadedFilesharesEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.UploadRequestActivationForOwnerEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.UploadRequestActivationForRecipientEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.UploadRequestCloseByOwnerEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.UploadRequestClosedByRecipientEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.UploadRequestCreatedEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.UploadRequestDeleteFileEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.UploadRequestFileDeletedByOwnerEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.UploadRequestPasswordRenewalEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.UploadRequestRecipientRemovedEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.UploadRequestReminderEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.UploadRequestUnavailableSpaceEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.UploadRequestUpdatedSettingsEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.UploadRequestUploadedFileEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.UploadRequestWarnBeforeExpiryEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.UploadRequestWarnExpiryEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.WarnOwnerAboutGuestExpirationEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.WorkGroupWarnDeletedMemberEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.WorkGroupWarnNewMemberEmailBuilder;
import org.linagora.linshare.core.notifications.emails.impl.WorkGroupWarnUpdatedMemberEmailBuilder;
import org.linagora.linshare.core.notifications.service.MailBuildingService;
import org.linagora.linshare.core.service.FunctionalityReadOnlyService;
import org.linagora.linshare.mongo.entities.UploadProposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;

import com.google.common.collect.Maps;

public class MailBuildingServiceImpl implements MailBuildingService {

	private final static Logger logger = LoggerFactory
			.getLogger(MailBuildingServiceImpl.class);

	private final TemplateEngine templateEngine;

	private final Map<MailContentType, EmailBuilder> emailBuilders;

	private final DomainBusinessService domainBusinessService;

	private final FunctionalityReadOnlyService functionalityReadOnlyService;

	private final MailActivationBusinessService mailActivationBusinessService;

	private class ContactRepresentation {
		private String mail;
		private String firstName;
		private String lastName;

		public ContactRepresentation(User user) {
			this.mail = StringUtils.trimToNull(user.getMail());
			this.firstName = StringUtils.trimToNull(user.getFirstName());
			this.lastName = StringUtils.trimToNull(user.getLastName());
		}

		public String getContactRepresentation() {
			return getContactRepresentation(false);
		}

		public String getContactRepresentation(boolean includeMail) {
			if (this.firstName == null || this.lastName == null)
				return this.mail;
			StringBuilder res = new StringBuilder();
			res.append(firstName);
			res.append(" ");
			res.append(lastName);
			if (includeMail) {
				res.append(" (");
				res.append(mail);
				res.append(")");
			}
			return res.toString();
		}
	}

	/**
	 * XXX HACK
	 * 
	 * Helper using LinkedHashMap to chain the Key/Value substitution
	 * in mail templates.
	 * 
	 * @author nbertrand
	 */
	private class MailContainerBuilder {

		@SuppressWarnings("serial")
		private class KeyValueChain extends LinkedHashMap<String, String> {
			public KeyValueChain add(String key, String value) {
				logger.debug("Adding K/V pair: [" + key + ", " + value
						+ "]");
				super.put(key, StringUtils.defaultString(value));
				return this;
			}
		}

		private KeyValueChain subjectChain;
		private KeyValueChain greetingsChain;
		private KeyValueChain bodyChain;

		public MailContainerBuilder() {
			super();
			subjectChain = new KeyValueChain();
			greetingsChain = new KeyValueChain();
			bodyChain = new KeyValueChain();
		}

		public KeyValueChain getSubjectChain() {
			return subjectChain;
		}

		public KeyValueChain getGreetingsChain() {
			return greetingsChain;
		}

		public KeyValueChain getBodyChain() {
			return bodyChain;
		}
	}

	/**
	 * Constructor
	 */
	public MailBuildingServiceImpl(
			final MailConfigBusinessService mailConfigBusinessService,
			final DomainBusinessService domainBusinessService,
			final FunctionalityReadOnlyService functionalityReadOnlyService,
			final MailActivationBusinessService mailActivationBusinessService,
			FileDataStore fileDataStore,
			boolean insertLicenceTerm,
			String urlTemplateForReceivedShares,
			String urlTemplateForDocuments,
			String urlTemplateForGuestReset,
			String urlTemplateForAnonymousUrl,
			boolean templatingStrictMode,
			boolean templatingSubjectPrefix,
			String urlFragmentQueryParamFileUuid,
			String urlTemplateForWorkgroup,
			String urlTemplateForUploadRequestEntries
			) throws Exception {
		this.domainBusinessService = domainBusinessService;
		this.functionalityReadOnlyService = functionalityReadOnlyService;
		this.mailActivationBusinessService = mailActivationBusinessService;
		this.templateEngine = new TemplateEngine();
		LinShareStringTemplateResolver templateResolver = new LinShareStringTemplateResolver(insertLicenceTerm, templatingSubjectPrefix);
		if (templatingStrictMode) {
			templateResolver.setTemplateMode(TemplateMode.XML);
		}
		templateEngine.setTemplateResolver(templateResolver);

		emailBuilders = Maps.newHashMap();
		emailBuilders.put(MailContentType.SHARE_NEW_SHARE_FOR_RECIPIENT, new ShareNewShareEmailBuilder());

		GuestAccountNewCreationEmailBuilder newGuestBuilder = new GuestAccountNewCreationEmailBuilder();
		newGuestBuilder.setUrlTemplateForGuestReset(urlTemplateForGuestReset);
		emailBuilders.put(MailContentType.GUEST_ACCOUNT_NEW_CREATION, newGuestBuilder);

		emailBuilders.put(MailContentType.SHARE_FILE_DOWNLOAD, new ShareFileDownloadEmailBuilder());

		GuestAccountResetPasswordEmailBuilder resetGuestBuilder = new GuestAccountResetPasswordEmailBuilder();
		resetGuestBuilder.setUrlTemplateForGuestReset(urlTemplateForGuestReset);
		emailBuilders.put(MailContentType.GUEST_ACCOUNT_RESET_PASSWORD_LINK, resetGuestBuilder);
		emailBuilders.put(MailContentType.GUEST_WARN_GUEST_ABOUT_HIS_PASSWORD_RESET, new GuestWarnGuestAboutHisPasswordResetEmailBuilder());

		emailBuilders.put(MailContentType.SHARE_NEW_SHARE_ACKNOWLEDGEMENT_FOR_SENDER,
				new ShareNewShareAcknowledgementEmailBuilder());
		emailBuilders.put(MailContentType.SHARE_FILE_SHARE_DELETED, new ShareFileShareDeletedEmailBuilder());
		emailBuilders.put(MailContentType.SHARE_WARN_RECIPIENT_BEFORE_EXPIRY, new ShareWarnRecipientBeforeExpiryEmailBuilder());
		emailBuilders.put(MailContentType.SHARE_WARN_UNDOWNLOADED_FILESHARES, new ShareWarnUndownloadedFilesharesEmailBuilder());
		emailBuilders.put(MailContentType.SHARE_WARN_SENDER_ABOUT_SHARE_EXPIRATION_WITHOUT_DOWNLOAD, new ShareWarnSenderAboutShareExpirationEmailBuilder());
		emailBuilders.put(MailContentType.SHARE_WARN_RECIPIENT_ABOUT_EXPIRED_SHARE, new ShareWarnRecipientAboutExpiredSahreEmailBuilder());

		emailBuilders.put(MailContentType.FILE_WARN_OWNER_BEFORE_FILE_EXPIRY, new FileWarnOwnerBeforeExpiryEmailBuilder());

		emailBuilders.put(MailContentType.UPLOAD_REQUEST_UPLOADED_FILE, new UploadRequestUploadedFileEmailBuilder());
		emailBuilders.put(MailContentType.UPLOAD_REQUEST_WARN_EXPIRY, new UploadRequestWarnExpiryEmailBuilder());
		emailBuilders.put(MailContentType.UPLOAD_REQUEST_WARN_BEFORE_EXPIRY, new UploadRequestWarnBeforeExpiryEmailBuilder());
		emailBuilders.put(MailContentType.UPLOAD_REQUEST_CLOSED_BY_RECIPIENT, new UploadRequestClosedByRecipientEmailBuilder());
		emailBuilders.put(MailContentType.UPLOAD_REQUEST_FILE_DELETED_BY_RECIPIENT, new UploadRequestDeleteFileEmailBuilder());
		emailBuilders.put(MailContentType.UPLOAD_REQUEST_UNAVAILABLE_SPACE, new UploadRequestUnavailableSpaceEmailBuilder());

		emailBuilders.put(MailContentType.UPLOAD_REQUEST_ACTIVATED_FOR_RECIPIENT, new UploadRequestActivationForRecipientEmailBuilder());
		emailBuilders.put(MailContentType.UPLOAD_REQUEST_ACTIVATED_FOR_OWNER, new UploadRequestActivationForOwnerEmailBuilder());
		emailBuilders.put(MailContentType.UPLOAD_REQUEST_REMINDER, new UploadRequestReminderEmailBuilder());
		emailBuilders.put(MailContentType.UPLOAD_REQUEST_PASSWORD_RENEWAL, new UploadRequestPasswordRenewalEmailBuilder());
		emailBuilders.put(MailContentType.UPLOAD_REQUEST_CREATED, new UploadRequestCreatedEmailBuilder());
		emailBuilders.put(MailContentType.UPLOAD_REQUEST_CLOSED_BY_OWNER, new UploadRequestCloseByOwnerEmailBuilder());
		emailBuilders.put(MailContentType.UPLOAD_REQUEST_RECIPIENT_REMOVED, new UploadRequestRecipientRemovedEmailBuilder());
		emailBuilders.put(MailContentType.UPLOAD_REQUEST_UPDATED_SETTINGS, new UploadRequestUpdatedSettingsEmailBuilder());
		emailBuilders.put(MailContentType.UPLOAD_REQUEST_FILE_DELETED_BY_OWNER, new UploadRequestFileDeletedByOwnerEmailBuilder());

		emailBuilders.put(MailContentType.GUEST_WARN_OWNER_ABOUT_GUEST_EXPIRATION, new WarnOwnerAboutGuestExpirationEmailBuilder());

		emailBuilders.put(MailContentType.WORKGROUP_WARN_NEW_MEMBER, new WorkGroupWarnNewMemberEmailBuilder());
		emailBuilders.put(MailContentType.WORKGROUP_WARN_UPDATED_MEMBER, new WorkGroupWarnUpdatedMemberEmailBuilder());
		emailBuilders.put(MailContentType.WORKGROUP_WARN_DELETED_MEMBER, new WorkGroupWarnDeletedMemberEmailBuilder());

		emailBuilders.put(MailContentType.ACCOUNT_OWNER_WARN_JWT_PERMANENT_TOKEN_CREATED, new JwtPermanentCreatedEmailBuilder());
		emailBuilders.put(MailContentType.ACCOUNT_OWNER_WARN_JWT_PERMANENT_TOKEN_DELETED, new JwtPermanentDeletedEmailBuilder());

		emailBuilders.put(MailContentType.DRIVE_WARN_NEW_MEMBER, new DriveWarnNewMemberEmailBuilder());
		emailBuilders.put(MailContentType.DRIVE_WARN_UPDATED_MEMBER, new DriveWarnUpdatedMemberEmailBuilder());
		emailBuilders.put(MailContentType.DRIVE_WARN_DELETED_MEMBER, new DriveWarnDeletedMemberEmailBuilder());

		initMailBuilders(insertLicenceTerm, domainBusinessService, functionalityReadOnlyService,
				mailActivationBusinessService, fileDataStore, urlTemplateForReceivedShares, urlTemplateForDocuments,
				urlTemplateForAnonymousUrl, urlFragmentQueryParamFileUuid, urlTemplateForWorkgroup, urlTemplateForUploadRequestEntries);
		Set<MailContentType> keySet = emailBuilders.keySet();
		logger.debug("mail content loaded : size : {}", keySet.size());
		for (MailContentType mailContentType : keySet) {
			logger.debug(" mailContentType : {}", mailContentType );
		}
		logger.debug("end");
	}

	private void initMailBuilders(boolean insertLicenceTerm,
		DomainBusinessService domainBusinessService,
		FunctionalityReadOnlyService functionalityReadOnlyService,
		MailActivationBusinessService mailActivationBusinessService,
		FileDataStore fileDataStore,
		String urlTemplateForReceivedShares,
		String urlTemplateForDocuments,
		String urlTemplateForAnonymousUrl,
		String paramFilesUuid,
		String urlTemplateForWorkgroup,
		String urlTemplateForUploadRequestEntries
	) {
		Collection<EmailBuilder> values = emailBuilders.values();
		for (EmailBuilder emailBuilder : values) {
			emailBuilder.setTemplateEngine(templateEngine);
			emailBuilder.setInsertLicenceTerm(insertLicenceTerm);
			emailBuilder.setMailActivationBusinessService(mailActivationBusinessService);
			emailBuilder.setFunctionalityReadOnlyService(functionalityReadOnlyService);
			emailBuilder.setDomainBusinessService(domainBusinessService);
			emailBuilder.setFileDataStore(fileDataStore);
			emailBuilder.setUrlTemplateForDocuments(urlTemplateForDocuments);
			emailBuilder.setUrlTemplateForReceivedShares(urlTemplateForReceivedShares);
			emailBuilder.setUrlTemplateForAnonymousUrl(urlTemplateForAnonymousUrl);
			emailBuilder.setUrlFragmentQueryParamFileUuid(paramFilesUuid);
			emailBuilder.setUrlTemplateForWorkgroup(urlTemplateForWorkgroup);
			emailBuilder.setUrlTemplateForUploadRequestEntries(urlTemplateForUploadRequestEntries);
		}
	}

	@Override
	public MailContainerWithRecipient build(EmailContext context) throws BusinessException {
		Validate.notNull(context, "Email context can't be null");
		MailContentType type = context.getType();
		EmailBuilder builder = emailBuilders.get(type);
		Validate.notNull(builder, "Missing email builder!");
		return builder.build(context);
	}

	@Override
	public boolean fakeBuildIsSupported(MailContentType type) throws BusinessException {
		Validate.notNull(type, "MailContentType can't be null");
		EmailBuilder builder = emailBuilders.get(type);
		if (builder == null) {
			return false;
		}
		return true;
	}

	@Override
	public MailContainerWithRecipient fakeBuild(MailContentType type, MailConfig cfg, Language language, Integer flavor) throws BusinessException {
		Validate.notNull(type, "MailContentType can't be null");
		if (cfg == null) {
			cfg = this.domainBusinessService.getUniqueRootDomain().getCurrentMailConfiguration();
		}
		if (language == null) {
			language = Language.ENGLISH;
		}
		EmailBuilder builder = emailBuilders.get(type);
		cfg.findContent(language, type);
		return builder.fakeBuild(cfg, language, flavor);
	}

	@Override
	public List<ContextMetadata> getAvailableVariables(MailContentType type) {
		Validate.notNull(type, "MailContentType can't be null");
		EmailBuilder builder = emailBuilders.get(type);
		if (builder == null) {
			throw new BusinessException(BusinessErrorCode.TEMPLATE_MISSING_TEMPLATE_BUILDER, "Missing template builder");
		}
		return builder.getAvailableVariables();
	}

	/**
	 * Old and ugly code, to be removed.
	 */

	@Override
	public MailContainerWithRecipient buildCreateUploadProposition(User recipient, UploadProposition proposition)
			throws BusinessException {
//		if (isDisable(recipient, MailActivationType.UPLOAD_PROPOSITION_CREATED)) {
		if (isDisable(recipient, null)) {
			return null;
		}
		MailConfig cfg = recipient.getDomain().getCurrentMailConfiguration();
		MailContainerWithRecipient container = new MailContainerWithRecipient(
				recipient.getExternalMailLocale());
		MailContainerBuilder builder = new MailContainerBuilder();

		builder.getSubjectChain()
				.add("actorRepresentation", proposition.getContact().getMail())
				.add("subject", proposition.getLabel());
		builder.getGreetingsChain()
				.add("firstName", recipient.getFirstName())
				.add("lastName", recipient.getLastName());
		builder.getBodyChain()
				.add("subject", proposition.getLabel())
				.add("body", proposition.getBody())
				.add("firstName", proposition.getContact().getFirstName())
				.add("lastName", proposition.getContact().getLastName())
				.add("mail", proposition.getContact().getMail())
				.add("uploadPropositionUrl", getUploadPropositionUrl(recipient));
		container.setRecipient(recipient.getMail());
		container.setFrom(getFromMailAddress(recipient));

		return buildMailContainer(cfg, container, null, MailContentType.UPLOAD_PROPOSITION_CREATED, builder);
	}

	@Override
	public MailContainerWithRecipient buildRejectUploadProposition(User sender, UploadProposition proposition)
			throws BusinessException {
		// MailActivationType.UPLOAD_PROPOSITION_REJECTED
		if (isDisable(sender, null)) {
			return null;
		}
		MailConfig cfg = sender.getDomain().getCurrentMailConfiguration();
		MailContainerWithRecipient container = new MailContainerWithRecipient(
				sender.getExternalMailLocale());
		MailContainerBuilder builder = new MailContainerBuilder();

		builder.getSubjectChain()
				.add("actorRepresentation", new ContactRepresentation(sender).getContactRepresentation())
				.add("subject", proposition.getLabel());
		builder.getGreetingsChain()
				.add("firstName", proposition.getContact().getFirstName())
				.add("lastName", proposition.getContact().getLastName());
		builder.getBodyChain()
				.add("subject", proposition.getLabel())
				.add("body", proposition.getBody())
				.add("firstName", sender.getFirstName())
				.add("lastName", sender.getLastName())
				.add("mail", proposition.getContact().getMail());

		container.setRecipient(proposition.getContact().getMail());
		container.setFrom(getFromMailAddress(sender));

		return buildMailContainer(cfg, container, null, MailContentType.UPLOAD_PROPOSITION_REJECTED, builder);
	}

	private String getFromMailAddress(User owner) {
		String fromMail = functionalityReadOnlyService
				.getDomainMailFunctionality(owner.getDomain()).getValue();
		return fromMail;
	}

	// TODO : to be used ?
	@Override
	public MailContainerWithRecipient buildFilterUploadRequest(User owner, UploadRequestUrl request)
			throws BusinessException {
		// MailActivationType.UPLOAD_REQUEST_AUTO_FILTER
		if (isDisable(request.getContact(), owner, null)) {
			return null;
		}
		MailConfig cfg = owner.getDomain().getCurrentMailConfiguration();
		MailContainerWithRecipient container = new MailContainerWithRecipient(
				request.getLocale());
		MailContainerBuilder builder = new MailContainerBuilder();

		builder.getSubjectChain()
				.add("subject", request.getUploadRequest().getUploadRequestGroup().getSubject());
		builder.getGreetingsChain()
				.add("firstName", owner.getFirstName())
				.add("lastName", owner.getLastName());
		builder.getBodyChain()
				.add("subject", request.getUploadRequest().getUploadRequestGroup().getSubject())
				.add("body", request.getUploadRequest().getUploadRequestGroup().getBody());
		container.setRecipient(request.getContact());
		container.setFrom(getFromMailAddress(owner));
		container.setReplyTo(owner);

		return buildMailContainer(cfg, container, null, MailContentType.UPLOAD_REQUEST_AUTO_FILTER, builder);
	}

	/*
	 * Helpers
	 */

	private String getUploadPropositionUrl(Account recipient) {
		String baseUrl = getLinShareUrlForAUserRecipient(recipient);
		StringBuffer uploadPropositionUrl = new StringBuffer();
		uploadPropositionUrl.append(baseUrl);
		if (!baseUrl.endsWith("/")) {
			uploadPropositionUrl.append('/');
		}
		uploadPropositionUrl.append("uploadrequest/proposition");
		return uploadPropositionUrl.toString();
	}

	private String getLinShareUrlForAUserRecipient(Account recipient) {
		String value = functionalityReadOnlyService
				.getCustomNotificationUrlFunctionality(recipient.getDomain())
				.getValue();
		if (!value.endsWith("/")) {
			return value + "/";
		}
		return value;
	}

	/*
	 * MAIL CONTAINER BUILDER SECTION
	 */

	private MailContainerWithRecipient buildMailContainer(MailConfig cfg,
			final MailContainerWithRecipient input, String pm,
			MailContentType type, MailContainerBuilder builder)
			throws BusinessException {
		MailContainerWithRecipient container = new MailContainerWithRecipient(input);
		String layout = cfg.getMailLayoutHtml().getLayout();
		container.setContent(layout);
		// Message IDs from Web service API (ex Plugin Thunderbird)
		container.setInReplyTo(input.getInReplyTo());
		container.setReferences(input.getReferences());
		return container;
	}

	private boolean isDisable(Contact contact, Account sender, MailActivationType type) {
		AbstractDomain recipientDomain = domainBusinessService.findGuestDomain(sender.getDomain());
		// guest domain could be inexistent into the database.
		if (recipientDomain == null) {
			recipientDomain = sender.getDomain();
		}
		MailActivation mailActivation = mailActivationBusinessService
				.findForInternalUsage(recipientDomain, type);
		boolean enable = mailActivation.isEnable();
		return !enable;
	}

	@SuppressWarnings("unused")
	private boolean isDisable(Account recipient, MailActivationType type) {
		// Disable old deprecated notifications !!
		if (true) {
			return true;
		}
		MailActivation mailActivation = mailActivationBusinessService
				.findForInternalUsage(recipient.getDomain(), type);
		boolean enable = mailActivation.isEnable();
		return !enable;
	}

}
