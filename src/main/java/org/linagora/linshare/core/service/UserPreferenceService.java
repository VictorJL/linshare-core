/*
 * LinShare is an open source filesharing software developed by LINAGORA.
 * 
 * Copyright (C) 2016-2020 LINAGORA
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
package org.linagora.linshare.core.service;

import java.util.List;

import org.linagora.linshare.core.domain.entities.AbstractDomain;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.mongo.entities.UserPreference;

public interface UserPreferenceService {

	/**
	 * Find a specific key for an account. It is unique.
	 * 
	 * @param actor
	 * @param key
	 * @return UserPreference
	 */
	UserPreference findByKey(Account actor, Account owner, String key);

	/**
	 * Find all preferences for one account
	 * 
	 * @param actor
	 * @param owner
	 * @return List<UserPreference>
	 */
	List<UserPreference> findByAccount(Account actor, Account owner);

	/**
	 * Find all accounts's preferences from one domain.
	 * 
	 * @param actor
	 * @param domain
	 * @return List<UserPreference>
	 */
	List<UserPreference> findByDomain(Account actor, AbstractDomain domain);

	/**
	 * 
	 * @param actor
	 * @param owner
	 * @param uuid
	 * @return UserPreference
	 */
	UserPreference findByUuid(Account actor, Account owner, String uuid);

	UserPreference create(Account actor, Account owner, UserPreference dto) throws BusinessException;

	UserPreference update(Account actor, Account owner, UserPreference dto) throws BusinessException;

	UserPreference delete(Account actor, Account owner, String uuid) throws BusinessException;

	void deleteAll(Account actor, Account owner) throws BusinessException;
}
