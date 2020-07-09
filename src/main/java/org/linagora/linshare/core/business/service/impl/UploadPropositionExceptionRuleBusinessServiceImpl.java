/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
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
 * and free version of LinShare™, powered by Linagora © 2009-2018. Contribute to
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
package org.linagora.linshare.core.business.service.impl;

import java.util.Date;
import java.util.List;

import org.linagora.linshare.core.business.service.UploadPropositionExceptionRuleBusinessService;
import org.linagora.linshare.core.domain.constants.UploadPropositionExceptionRuleType;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.mongo.entities.UploadPropositionExceptionRule;
import org.linagora.linshare.mongo.repository.UploadPropositionExceptionRuleMongoRepository;

public class UploadPropositionExceptionRuleBusinessServiceImpl
		implements UploadPropositionExceptionRuleBusinessService {

	private final UploadPropositionExceptionRuleMongoRepository exceptionRuleMongoRepository;

	public UploadPropositionExceptionRuleBusinessServiceImpl(
			final UploadPropositionExceptionRuleMongoRepository exceptionRuleMongoRepository) {
		super();
		this.exceptionRuleMongoRepository = exceptionRuleMongoRepository;
	}

	@Override
	public UploadPropositionExceptionRule find(String uuid) {
		return exceptionRuleMongoRepository.findByUuid(uuid);
	}

	@Override
	public List<UploadPropositionExceptionRule> findAll() {
		return exceptionRuleMongoRepository.findAll();
	}

	@Override
	public List<UploadPropositionExceptionRule> findByExceptionRuleType(String accountUuid,
			UploadPropositionExceptionRuleType exceptionRuleType) {
		return exceptionRuleMongoRepository.findByAccountUuidAndExceptionRuleType(accountUuid, exceptionRuleType);
	}

	@Override
	public UploadPropositionExceptionRule findByMail(String accountUuid, String mail) {
		return exceptionRuleMongoRepository.findByAccountUuidAndMail(accountUuid, mail);
	}

	@Override
	public UploadPropositionExceptionRule create(UploadPropositionExceptionRule exceptionRule) {
		UploadPropositionExceptionRule foundExceptionRule = findByMail(exceptionRule.getAccountUuid(),
				exceptionRule.getMail());
		if (foundExceptionRule != null) {
			throw new BusinessException(BusinessErrorCode.UPLOAD_PROPOSITION_EXCEPTION_RULE_ALREADY_EXISTS,
					"An exception rule already exists for this mail " + exceptionRule.getMail()
							+ " and this accountUuid " + exceptionRule.getAccountUuid());
		}
		return exceptionRuleMongoRepository.insert(exceptionRule);
	}

	@Override
	public void delete(UploadPropositionExceptionRule exceptionRule) {
		exceptionRuleMongoRepository.delete(exceptionRule);
	}

	@Override
	public UploadPropositionExceptionRule update(UploadPropositionExceptionRule found, UploadPropositionExceptionRule exceptionRule) {
		found.setAccountUuid(exceptionRule.getAccountUuid());
		found.setDomainUuid(exceptionRule.getDomainUuid());
		found.setExceptionRuleType(exceptionRule.getExceptionRuleType());
		found.setMail(exceptionRule.getMail());
		found.setModificationDate(new Date());
		return exceptionRuleMongoRepository.save(found);
	}
}
