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
package org.linagora.linshare.core.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.linagora.linshare.core.business.service.SanitizerInputHtmlBusinessService;
import org.linagora.linshare.core.business.service.UploadPropositionExceptionRuleBusinessService;
import org.linagora.linshare.core.domain.constants.UploadPropositionExceptionRuleType;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.rac.UploadPropositionExceptionRuleResourceAccessControl;
import org.linagora.linshare.core.service.UploadPropositionExceptionRuleService;
import org.linagora.linshare.mongo.entities.UploadPropositionExceptionRule;

public class UploadPropositionExceptionRuleServiceImpl extends GenericServiceImpl<Account, UploadPropositionExceptionRule> implements UploadPropositionExceptionRuleService {

	private final UploadPropositionExceptionRuleBusinessService exceptionRuleBusinessService;

	public UploadPropositionExceptionRuleServiceImpl(
			final UploadPropositionExceptionRuleBusinessService exceptionRuleBusinessService,
			final UploadPropositionExceptionRuleResourceAccessControl rac,
			SanitizerInputHtmlBusinessService sanitizerInputHtmlBusinessService) {
		super(rac, sanitizerInputHtmlBusinessService);
		this.exceptionRuleBusinessService = exceptionRuleBusinessService;
	}

	@Override
	public UploadPropositionExceptionRule find(Account authUser, Account actor, String uuid) {
		preChecks(authUser, actor);
		UploadPropositionExceptionRule found = exceptionRuleBusinessService.find(uuid);
		if (found == null) {
			throw new BusinessException(BusinessErrorCode.UPLOAD_PROPOSITION_EXCEPTION_RULE_NOT_FOUND, "Can not find upload proposition exception rule with uuid : " + uuid);
		}
		checkReadPermission(authUser, actor, UploadPropositionExceptionRule.class,
				BusinessErrorCode.UPLOAD_PROPOSITION_EXCEPTION_RULE_CAN_NOT_READ, found);
		return found;
	}

	@Override
	public List<UploadPropositionExceptionRule> findByExceptionRule(Account authUser, Account actor,
			UploadPropositionExceptionRuleType exceptionRuleType) {
		preChecks(authUser, actor);
		checkListPermission(authUser, actor, UploadPropositionExceptionRule.class,
				BusinessErrorCode.UPLOAD_PROPOSITION_EXCEPTION_RULE_CAN_NOT_LIST, null);
		return exceptionRuleBusinessService.findByExceptionRuleType(actor.getLsUuid(), exceptionRuleType);
	}

	@Override
	public UploadPropositionExceptionRule create(Account authUser, Account actor,
			UploadPropositionExceptionRule exceptionRule) {
		preChecks(authUser, actor);
		checkCreatePermission(authUser, actor, UploadPropositionExceptionRule.class,
				BusinessErrorCode.UPLOAD_PROPOSITION_EXCEPTION_RULE_CAN_NOT_CREATE, null);
		UploadPropositionExceptionRule persistedExceptionRule = new UploadPropositionExceptionRule(
				UUID.randomUUID().toString(), exceptionRule.getDomainUuid(), exceptionRule.getMail(),
				exceptionRule.getAccountUuid(), exceptionRule.getExceptionRuleType(), new Date(), new Date());
		return exceptionRuleBusinessService.create(persistedExceptionRule);
	}

	@Override
	public UploadPropositionExceptionRule delete(Account authUser, Account actor,
			UploadPropositionExceptionRule exceptionRule) {
		preChecks(authUser, actor);
		UploadPropositionExceptionRule found = find(authUser, actor, exceptionRule.getUuid());
		checkDeletePermission(authUser, actor, UploadPropositionExceptionRule.class,
				BusinessErrorCode.UPLOAD_PROPOSITION_CAN_NOT_DELETE, found);
		exceptionRuleBusinessService.delete(found);
		return found;
	}

	@Override
	public UploadPropositionExceptionRule update(Account authUser, Account actor,
			UploadPropositionExceptionRule exceptionRule) {
		preChecks(authUser, actor);
		UploadPropositionExceptionRule found = find(authUser, actor, exceptionRule.getUuid());
		checkUpdatePermission(authUser, actor, UploadPropositionExceptionRule.class,
				BusinessErrorCode.UPLOAD_PROPOSITION_CAN_NOT_UPDATE, found);
		return exceptionRuleBusinessService.update(found, exceptionRule);
	}
}
