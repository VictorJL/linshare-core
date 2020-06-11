/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2020 LINAGORA
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

import java.security.SecureRandom;
import java.util.Date;

import org.jboss.aerogear.security.otp.api.Base32;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.BooleanValueFunctionality;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.webservice.user.SecondFactorAuthenticationFacade;
import org.linagora.linshare.core.facade.webservice.user.dto.SecondFactorDto;
import org.linagora.linshare.core.service.AccountService;
import org.linagora.linshare.core.service.FunctionalityReadOnlyService;

public class SecondFactorAuthenticationFacadeImpl extends UserGenericFacadeImp implements SecondFactorAuthenticationFacade {

	protected final FunctionalityReadOnlyService functionalityReadOnlyService;

	public SecondFactorAuthenticationFacadeImpl(
			final AccountService accountService,
			final FunctionalityReadOnlyService functionalityReadOnlyService
			) {
		super(accountService);
		this.functionalityReadOnlyService = functionalityReadOnlyService;
	}

	@Override
	public SecondFactorDto find(String uuid) {
		User authUser = checkAuthentication();
		SecondFactorDto dto = to2FADto(authUser);
		return dto;
	}

	@Override
	public SecondFactorDto create(SecondFactorDto sfd) {
		User authUser = checkAuthentication();
		BooleanValueFunctionality twofaFunc = functionalityReadOnlyService.getSecondFactorAuthenticationFunctionality(authUser.getDomain());
		if (!twofaFunc.getActivationPolicy().getStatus()) {
			String message = "You can not create a 2FA shared key for your account. Functionality is not enabled.";
			throw new BusinessException(BusinessErrorCode.AUTHENTICATION_SECOND_FACTOR_NOT_ENABLED, message);
		}
		if (authUser.isUsing2FA()) {
			String message = "You can not create a 2FA shared key for your account. It already exists.";
			throw new BusinessException(BusinessErrorCode.AUTHENTICATION_SECOND_FACTOR_ALREADY_EXISTS, message);
		}
		SecureRandom random = new SecureRandom();
		byte[] secret = random.generateSeed(20);
		authUser.setSecondFACreationDate(new Date());
		authUser.setSecondFASecret(Base32.encode(secret));
		Account updated = accountService.update(authUser);
		SecondFactorDto dto = to2FADto(updated);
		dto.setSharedKey(updated.getSecondFASecret());
		return dto;
	}

	@Override
	public SecondFactorDto delete(String uuid, SecondFactorDto sfd) {
		User authUser = checkAuthentication();
		authUser.setSecondFACreationDate(null);
		authUser.setSecondFASecret(null);
		Account updated = accountService.update(authUser);
		SecondFactorDto dto = to2FADto(updated);
		return dto;
	}

	private SecondFactorDto to2FADto(Account authUser) {
		SecondFactorDto dto = new SecondFactorDto(authUser.getLsUuid(), authUser.getSecondFACreationDate(), authUser.isUsing2FA());
		BooleanValueFunctionality twofaFunc = functionalityReadOnlyService.getSecondFactorAuthenticationFunctionality(authUser.getDomain());
		if (twofaFunc.getActivationPolicy().getStatus()) {
			dto.setRequired(twofaFunc.getValue());
			dto.setCanDeleteIt(twofaFunc.getDelegationPolicy().getStatus());
		}
		return dto;
	}
}
