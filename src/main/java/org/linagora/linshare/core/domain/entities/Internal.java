/*
 * LinShare is an open source filesharing software developed by LINAGORA.
 * 
 * Copyright (C) 2015-2020 LINAGORA
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
package org.linagora.linshare.core.domain.entities;

import java.util.UUID;

import org.linagora.linshare.core.domain.constants.AccountType;
import org.linagora.linshare.core.facade.webservice.common.dto.GenericUserDto;
import org.linagora.linshare.core.facade.webservice.common.dto.UserDto;

/**
 * Internal user.
 */
/**
 * @author fred
 *
 */
public class Internal extends User {

	/** Default constructor for hibernate. */
	public Internal() {
		super();
		lsUuid = UUID.randomUUID().toString();
		this.inconsistent = false;
	}

	/**
	 * Constructor.
	 * 
	 * @param firstName
	 *            first name.
	 * @param lastName
	 *            last name.
	 * @param mail
	 *            email.
	 * @param ldapUid
	 */
	public Internal(String firstName, String lastName, String mail,
			String ldapUid) {
		super(firstName, lastName, mail);
		this.ldapUid = ldapUid;
		this.lsUuid = UUID.randomUUID().toString();
		this.inconsistent = false;
	}

	public Internal(UserDto userDto) {
		super(userDto);
	}
	public Internal(GenericUserDto userDto) {
		this.lsUuid = userDto.getUuid();
		this.firstName = userDto.getFirstName();
		this.lastName = userDto.getLastName();
		this.setMail(userDto.getMail());
	}

	@Override
	public AccountType getAccountType() {
		return AccountType.INTERNAL;
	}

	@Override
	public String getAccountRepresentation() {
		return this.firstName + " " + this.lastName + "(" + lsUuid + ", "
				+ this.ldapUid + ")";
	}
}
