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
package org.linagora.linshare.core.domain.constants;

import org.linagora.linshare.core.domain.entities.AbstractDomain;
import org.linagora.linshare.core.domain.entities.GuestDomain;
import org.linagora.linshare.core.domain.entities.RootDomain;
import org.linagora.linshare.core.domain.entities.SubDomain;
import org.linagora.linshare.core.domain.entities.TopDomain;
import org.linagora.linshare.core.facade.webservice.common.dto.DomainDto;

public enum DomainType {

	ROOTDOMAIN(0) {
		@Override
		public RootDomain getDomain(DomainDto domainDto, AbstractDomain parent) {
			return new RootDomain(domainDto);
		}
	},
	TOPDOMAIN(1) {
		@Override
		public TopDomain getDomain(DomainDto domainDto, AbstractDomain parent) {
			return new TopDomain(domainDto, parent);
		}
	},
	SUBDOMAIN(2) {
		@Override
		public SubDomain getDomain(DomainDto domainDto, AbstractDomain parent) {
			return new SubDomain(domainDto, parent);
		}
	},
	GUESTDOMAIN(3) {
		@Override
		public GuestDomain getDomain(DomainDto domainDto, AbstractDomain parent) {
			return new GuestDomain(domainDto, parent);
		}
	};

	private int value;

	private DomainType(int value) {
		this.value = value;
	}

	public int toInt() {
		return value;
	}

	public static DomainType fromInt(int value) {
		for (DomainType type : values()) {
			if (type.value == value) {
				return type;
			}
		}
		throw new IllegalArgumentException("Doesn't match an existing DomainType");
	}

	public abstract AbstractDomain getDomain(DomainDto domainDto, AbstractDomain parent);
}
