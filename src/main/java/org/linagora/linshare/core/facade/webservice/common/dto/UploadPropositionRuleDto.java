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

package org.linagora.linshare.core.facade.webservice.common.dto;

import org.linagora.linshare.core.domain.constants.UploadPropositionRuleFieldType;
import org.linagora.linshare.core.domain.constants.UploadPropositionRuleOperatorType;
import org.linagora.linshare.core.domain.entities.UploadPropositionRule;

import com.google.common.base.Function;

public class UploadPropositionRuleDto {

	protected String uuid;

	protected String operator;

	protected String field;

	protected String value;

	public UploadPropositionRuleDto() {
		super();
	}

	public UploadPropositionRuleDto(UploadPropositionRule entity) {
		super();
		this.uuid = entity.getUuid();
		this.operator = entity.getOperator().name();
		this.field = entity.getField().name();
		this.value = entity.getValue();
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/*
	 * Transformers
	 */
	public static Function<UploadPropositionRule, UploadPropositionRuleDto> toVo() {
		return new Function<UploadPropositionRule, UploadPropositionRuleDto>() {
			@Override
			public UploadPropositionRuleDto apply(UploadPropositionRule entity) {
				return new UploadPropositionRuleDto(entity);
			}
		};
	}

	public static Function<UploadPropositionRuleDto, UploadPropositionRule> toEntity() {
		return new Function<UploadPropositionRuleDto, UploadPropositionRule>() {
			@Override
			public UploadPropositionRule apply(UploadPropositionRuleDto dto) {
				UploadPropositionRule entity = new UploadPropositionRule();
				entity.setUuid(dto.getUuid());
				entity.setValue(dto.getValue());
				entity.setOperator(UploadPropositionRuleOperatorType.fromString(dto.getOperator()));
				entity.setField(UploadPropositionRuleFieldType.fromString(dto.getField()));
				return entity;
			}
		};
	}
}
