/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2015-2020 LINAGORA
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

package org.linagora.linshare.core.facade.webservice.common.dto;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.linagora.linshare.core.domain.constants.UploadPropositionMatchType;
import org.linagora.linshare.core.domain.entities.UploadPropositionAction;
import org.linagora.linshare.core.domain.entities.UploadPropositionFilterOLD;
import org.linagora.linshare.core.domain.entities.UploadPropositionRule;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

public class UploadPropositionFilterDto {

	protected String uuid;

	protected String name;

	protected String match;

	protected boolean enable;

	private int order;

	protected List<UploadPropositionRuleDto> uploadPropositionRules = Lists
			.newArrayList();

	protected List<UploadPropositionActionDto> uploadPropositionActions = Lists
			.newArrayList();

	public UploadPropositionFilterDto() {
		super();
	}

	public UploadPropositionFilterDto(UploadPropositionFilterOLD entity) {
		super();
		this.uuid = entity.getUuid();
		this.name = entity.getName();
		this.enable = entity.isEnable();
		this.match = entity.getMatch().name();
		this.order = entity.getOrder();
		for (UploadPropositionAction action : entity.getActions()) {
			this.uploadPropositionActions.add(new UploadPropositionActionDto(
					action));
		}
		for (UploadPropositionRule rule : entity.getRules()) {
			this.uploadPropositionRules.add(new UploadPropositionRuleDto(rule));
		}
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMatch() {
		return match;
	}

	public void setMatch(String match) {
		this.match = match;
	}

	public List<UploadPropositionRuleDto> getUploadPropositionRules() {
		return uploadPropositionRules;
	}

	public void setUploadPropositionRules(
			List<UploadPropositionRuleDto> uploadPropositionRules) {
		this.uploadPropositionRules = uploadPropositionRules;
	}

	public List<UploadPropositionActionDto> getUploadPropositionActions() {
		return uploadPropositionActions;
	}

	public void setUploadPropositionActions(
			List<UploadPropositionActionDto> uploadPropositionActions) {
		this.uploadPropositionActions = uploadPropositionActions;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	/*
	 * Transformers
	 */
	public static Function<UploadPropositionFilterOLD, UploadPropositionFilterDto> toVo() {
		return new Function<UploadPropositionFilterOLD, UploadPropositionFilterDto>() {
			@Override
			public UploadPropositionFilterDto apply(UploadPropositionFilterOLD arg0) {
				return new UploadPropositionFilterDto(arg0);
			}
		};
	}

	public static Function<UploadPropositionFilterDto, UploadPropositionFilterOLD> toEntity() {
		return new Function<UploadPropositionFilterDto, UploadPropositionFilterOLD>() {
			@Override
			public UploadPropositionFilterOLD apply(UploadPropositionFilterDto dto) {
				Validate.notNull(dto.getUploadPropositionActions());
				Validate.notNull(dto.getUploadPropositionRules());
				UploadPropositionFilterOLD filter = new UploadPropositionFilterOLD();
				filter.setUuid(dto.getUuid());
				filter.setMatch(UploadPropositionMatchType.fromString(dto
						.getMatch()));
				filter.setName(dto.getName());
				filter.setEnable(dto.isEnable());
				filter.setOrder(dto.getOrder());
				filter.setActions(FluentIterable
						.from(dto.getUploadPropositionActions())
						.transform(UploadPropositionActionDto.toEntity())
						.toSet());
				filter.setRules(FluentIterable
						.from(dto.getUploadPropositionRules())
						.transform(UploadPropositionRuleDto.toEntity())
						.toSet());
				return filter;
			}
		};
	}

}
