/*
 * LinShare is an open source filesharing software developed by LINAGORA.
 * 
 * Copyright (C) 2018-2020 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2020. Contribute to
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

package org.linagora.linshare.core.facade.webservice.user.impl;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.DocumentEntry;
import org.linagora.linshare.core.domain.entities.UploadRequestEntry;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.webservice.common.dto.UploadRequestEntryDto;
import org.linagora.linshare.core.facade.webservice.user.UploadRequestEntryFacade;
import org.linagora.linshare.core.facade.webservice.user.dto.DocumentDto;
import org.linagora.linshare.core.service.AccountService;
import org.linagora.linshare.core.service.UploadRequestEntryService;

import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;

public class UploadRequestEntryFacadeImpl extends GenericFacadeImpl implements UploadRequestEntryFacade {

	private final UploadRequestEntryService uploadRequestEntryService;

	public UploadRequestEntryFacadeImpl(final AccountService accountService,
			final UploadRequestEntryService uploadRequestEntryService) {
		super(accountService);
		this.uploadRequestEntryService = uploadRequestEntryService;
	}

	@Override
	public UploadRequestEntryDto find(String actorUuid, String uuid) throws BusinessException {
		Validate.notEmpty(uuid, "Upload request entry uuid must be set.");
		User authUser = checkAuthentication();
		User actor = getActor(authUser, actorUuid);
		UploadRequestEntry uploadRequestEntry =  uploadRequestEntryService.find(authUser, actor, uuid);
		return new UploadRequestEntryDto(uploadRequestEntry);
	}

	@Override
	public ByteSource download(String actorUuid, String uuid) throws BusinessException {
		Validate.notEmpty(uuid, "Missing required document uuid");
		User authUser = checkAuthentication();
		User actor = getActor(authUser, actorUuid);
		return uploadRequestEntryService.download(authUser, actor, uuid);
	}

	@Override
	public UploadRequestEntryDto delete(String actorUuid, String uuid) {
		Validate.notEmpty(uuid, "Upload request entry uuid must be set.");
		User authUser = checkAuthentication();
		User actor = getActor(authUser, actorUuid);
		UploadRequestEntry uploadRequestEntry =  uploadRequestEntryService.delete(authUser, actor, uuid);
		return new UploadRequestEntryDto(uploadRequestEntry);
	}

	@Override
	public List<DocumentDto> copy(String actorUuid, String uuid) throws BusinessException {
		Account authUser = checkAuthentication();
		Validate.notEmpty(uuid, "Upload request entry uuid must be set");
		User actor = (User) getActor(authUser, actorUuid);
		UploadRequestEntry uploadRequestEntry = uploadRequestEntryService.find(authUser, actor, uuid);
		Validate.notNull(uploadRequestEntry);
		DocumentEntry newDocumentEntry = uploadRequestEntryService.copy(authUser, actor, uploadRequestEntry);
		return Lists.newArrayList(new DocumentDto(newDocumentEntry));
	}
}
