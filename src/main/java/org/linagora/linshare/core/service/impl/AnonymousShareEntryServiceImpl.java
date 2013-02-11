package org.linagora.linshare.core.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.linagora.linshare.core.business.service.AnonymousShareEntryBusinessService;
import org.linagora.linshare.core.business.service.DocumentEntryBusinessService;
import org.linagora.linshare.core.domain.constants.LogAction;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.AnonymousShareEntry;
import org.linagora.linshare.core.domain.entities.AnonymousUrl;
import org.linagora.linshare.core.domain.entities.Contact;
import org.linagora.linshare.core.domain.entities.DocumentEntry;
import org.linagora.linshare.core.domain.entities.MailContainer;
import org.linagora.linshare.core.domain.entities.ShareLogEntry;
import org.linagora.linshare.core.domain.entities.SystemAccount;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.service.AnonymousShareEntryService;
import org.linagora.linshare.core.service.FunctionalityService;
import org.linagora.linshare.core.service.LogEntryService;
import org.linagora.linshare.core.service.MailContentBuildingService;
import org.linagora.linshare.core.service.NotifierService;
import org.linagora.linshare.core.service.ShareExpiryDateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnonymousShareEntryServiceImpl implements AnonymousShareEntryService {

	private final FunctionalityService functionalityService;
	
	private final AnonymousShareEntryBusinessService anonymousShareEntryBusinessService;
	
	private final ShareExpiryDateService shareExpiryDateService;
	
	private final LogEntryService logEntryService;
	
	private final NotifierService notifierService;
    
    private final MailContentBuildingService mailContentBuildingService;
    
    private final DocumentEntryBusinessService documentEntryBusinessService;
    
    private static final Logger logger = LoggerFactory.getLogger(AnonymousShareEntryServiceImpl.class);
    
	public AnonymousShareEntryServiceImpl(FunctionalityService functionalityService, AnonymousShareEntryBusinessService anonymousShareEntryBusinessService,
			ShareExpiryDateService shareExpiryDateService, LogEntryService logEntryService, NotifierService notifierService, MailContentBuildingService mailElementsFactory,
			DocumentEntryBusinessService documentEntryBusinessService) {
		super();
		this.functionalityService = functionalityService;
		this.anonymousShareEntryBusinessService = anonymousShareEntryBusinessService;
		this.shareExpiryDateService = shareExpiryDateService;
		this.logEntryService = logEntryService;
		this.notifierService = notifierService;
		this.mailContentBuildingService = mailElementsFactory;
		this.documentEntryBusinessService = documentEntryBusinessService;
	}

	
	@Override
	public AnonymousShareEntry findByUuid(Account actor, String shareUuid) throws BusinessException {
		AnonymousShareEntry share = anonymousShareEntryBusinessService.findByUuid(shareUuid);
		if(share == null) {
			logger.error("Share not found : " + shareUuid);
			throw new BusinessException(BusinessErrorCode.SHARED_DOCUMENT_NOT_FOUND, "Share entry not found : " + shareUuid);
		}
		if(actor.isTechnicalAccount() || share.getEntryOwner().equals(actor) ) {
			return share;
		} else {
			logger.error("Actor " + actor.getAccountReprentation() + " does not own the share : " + shareUuid);
			throw new BusinessException(BusinessErrorCode.NOT_AUTHORIZED, "You are not authorized to get this share, it does not belong to you.");
		}
	}
	

	@Override
	public List<AnonymousShareEntry> createAnonymousShare(List<DocumentEntry> documentEntries, User sender, Contact recipient, Calendar expirationDate, Boolean passwordProtected, MailContainer mailContainer) throws BusinessException {
		
		if(functionalityService.isSauMadatory(sender.getDomain().getIdentifier())) {
			passwordProtected = true;
		} else if(!functionalityService.isSauAllowed(sender.getDomain().getIdentifier())) {
			// if it is not mandatory an not allowed, it must be forbidden
			passwordProtected = false;
		}
		
		if (expirationDate == null) {
			expirationDate = shareExpiryDateService.computeMinShareExpiryDateOfList(documentEntries, sender);
		}
		
		AnonymousUrl anonymousUrl = anonymousShareEntryBusinessService.createAnonymousShare(documentEntries, sender, recipient, expirationDate, passwordProtected);
		
		
		// logs
		for (DocumentEntry documentEntry : documentEntries) {
			ShareLogEntry logEntry = new ShareLogEntry(sender, documentEntry, LogAction.FILE_SHARE, "Anonymous sharing of a file", expirationDate);
		    logEntryService.create(logEntry);
		}
		
		notifierService.sendAllNotification(mailContentBuildingService.buildMailNewSharingWithRecipient(mailContainer, anonymousUrl, sender));
		
		
		List<AnonymousShareEntry> anonymousShareEntries = new ArrayList<AnonymousShareEntry>(anonymousUrl.getAnonymousShareEntries());
		return anonymousShareEntries;
	}


	@Override
	public List<AnonymousShareEntry> createAnonymousShare(List<DocumentEntry> documentEntries, User sender, List<Contact> recipients, Calendar expirationDate, Boolean passwordProtected, MailContainer mailContainer) throws BusinessException {
		List<AnonymousShareEntry> anonymousShareEntries = new ArrayList<AnonymousShareEntry>();
		for (Contact contact : recipients) {
			anonymousShareEntries.addAll(createAnonymousShare(documentEntries, sender, contact, expirationDate, passwordProtected, mailContainer));
		}
		return anonymousShareEntries;
	}


	@Override
	public void deleteShare(Account actor, String shareUuid) throws BusinessException {
		AnonymousShareEntry shareEntry = findByUuid(actor, shareUuid);
		this.deleteShare(actor, shareEntry);
	}


	@Override
	public void deleteShare(Account actor, AnonymousShareEntry shareEntry) throws BusinessException {
		// TODO : fix permissions
//		if(shareEntry.getEntryOwner().equals(actor) || actor.equals(guestRepository.getSystemAccount()) || actor.getAccountType().equals(AccountType.ROOT) ) {
//			
//		} else {
//			logger.error("Actor " + actor.getAccountReprentation() + " does not own the share : " + shareEntry.getUuid());
//			throw new BusinessException(BusinessErrorCode.NOT_AUTHORIZED, "You are not authorized to delete this anonymous share, it does not belong to you.");
//		}
		
		
		
		anonymousShareEntryBusinessService.deleteAnonymousShare(shareEntry);
		ShareLogEntry logEntry = new ShareLogEntry(actor, shareEntry, LogAction.SHARE_DELETE, "Deleting anonymous share" );
		logEntryService.create(logEntry);
		
		//FIXME AnonymousShareEntry mail notification
//		notifierService.sendAllNotifications(mailElementsFactory.buildMailAnonymousDownload(actor, mailContainer, docs, email, recipient)
	}
	
	
	@Override
	public void deleteShare(SystemAccount systemAccount, AnonymousShareEntry shareEntry) throws BusinessException {
		anonymousShareEntryBusinessService.deleteAnonymousShare(shareEntry);
		ShareLogEntry logEntry = new ShareLogEntry(systemAccount, shareEntry, LogAction.SHARE_DELETE, "Deleting anonymous share" );
		logEntryService.create(logEntry);
	}
	
	
	@Override
	public InputStream getAnonymousShareEntryStream(String shareUuid) throws BusinessException {
		try {
			AnonymousShareEntry shareEntry = downloadAnonymousShareEntry(shareUuid);
			//send a notification by mail to the owner
			notifierService.sendAllNotification(mailContentBuildingService.buildMailAnonymousDownload(shareEntry));
			return documentEntryBusinessService.getDocumentStream(shareEntry.getDocumentEntry());
		} catch (BusinessException e) {
			logger.error("Can't find anonymous share : " + shareUuid + " : " + e.getMessage());
			throw e;
		}
	}

	
	private AnonymousShareEntry downloadAnonymousShareEntry(String shareUuid) throws BusinessException {
		AnonymousShareEntry shareEntry = anonymousShareEntryBusinessService.findByUuidForDownload(shareUuid);
		
		ShareLogEntry logEntry = new ShareLogEntry(shareEntry.getEntryOwner(), shareEntry, LogAction.ANONYMOUS_SHARE_DOWNLOAD, "Anonymous download of a file");
		logEntryService.create(logEntry);
		return shareEntry;
	}

	
	@Override
	public void sendDocumentEntryUpdateNotification(AnonymousShareEntry anonymousShareEntry, String friendlySize, String originalFileName) {
		try {
			notifierService.sendAllNotification(mailContentBuildingService.buildMailSharedDocumentUpdated(anonymousShareEntry, originalFileName, friendlySize));
		} catch (BusinessException e) {
			logger.error("Error while trying to notify document update ", e);
		}
	}
	
	
	@Override
	public void sendUpcomingOutdatedShareEntryNotification(SystemAccount actor, AnonymousShareEntry shareEntry, Integer days) {
		try {
			notifierService.sendAllNotification(mailContentBuildingService.buildMailUpcomingOutdatedShare(shareEntry, days));
		} catch (BusinessException e) {
			logger.error("Error while trying to notify upcoming outdated share", e);
		}
		
	}
}
