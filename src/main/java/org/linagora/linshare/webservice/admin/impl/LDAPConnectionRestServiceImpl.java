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
package org.linagora.linshare.webservice.admin.impl;

import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.webservice.admin.LdapConnectionFacade;
import org.linagora.linshare.core.facade.webservice.admin.dto.LdapConnectionDto;
import org.linagora.linshare.webservice.WebserviceBase;
import org.linagora.linshare.webservice.admin.LDAPConnectionRestService;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;


@Path("/ldap_connections")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class LDAPConnectionRestServiceImpl extends WebserviceBase implements
		LDAPConnectionRestService {

	private final LdapConnectionFacade ldapConnectionFacade;

	public LDAPConnectionRestServiceImpl(
			final LdapConnectionFacade ldapConnectionFacade) {
		this.ldapConnectionFacade = ldapConnectionFacade;
	}

	@Path("/")
	@GET
	@Operation(summary = "Find all LDAP connections.", responses = {
		@ApiResponse(
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = LdapConnectionDto.class))),
			responseCode = "200"
		)
	})
	@Override
	public Set<LdapConnectionDto> findAll() throws BusinessException {
		return ldapConnectionFacade.findAll();
	}

	@Path("/{uuid}")
	@GET
	@Operation(summary = "Find a LDAP connection.", responses = {
		@ApiResponse(
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = LdapConnectionDto.class))),
			responseCode = "200"
		)
	})
	@Override
	public LdapConnectionDto find(@PathParam(value = "uuid") String uuid) throws BusinessException {
		return ldapConnectionFacade.find(uuid);
	}

	@Path("/{uuid}")
	@HEAD
	@Operation(summary = "Find a LDAP connection.")
	@Override
	public void head(@PathParam(value = "uuid") String uuid) throws BusinessException {
		ldapConnectionFacade.find(uuid);
	}

	@Path("/")
	@POST
	@Operation(summary = "Find a LDAP connection.", responses = {
		@ApiResponse(
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = LdapConnectionDto.class))),
			responseCode = "200"
		)
	})
	@Override
	public LdapConnectionDto create(LdapConnectionDto LDAPConnection)
			throws BusinessException {
		return ldapConnectionFacade.create(LDAPConnection);
	}

	@Path("/")
	@PUT
	@Operation(summary = "Update a LDAP connection.", responses = {
		@ApiResponse(
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = LdapConnectionDto.class))),
			responseCode = "200"
		)
	})
	@Override
	public LdapConnectionDto update(LdapConnectionDto LDAPConnection)
			throws BusinessException {
		return ldapConnectionFacade.update(LDAPConnection);
	}

	@Path("/")
	@DELETE
	@Operation(summary = "Delete a LDAP connection.", responses = {
		@ApiResponse(
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = LdapConnectionDto.class))),
			responseCode = "200"
		)
	})
	@Override
	public LdapConnectionDto delete(LdapConnectionDto LDAPConnection)
			throws BusinessException {
		return ldapConnectionFacade.delete(LDAPConnection);
	}
}
