/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2017-2018 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2018. Contribute to
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

package org.linagora.linshare.webservice.delegationv2.impl;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.Validate;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.linagora.linshare.core.domain.constants.AsyncTaskType;
import org.linagora.linshare.core.domain.constants.ThumbnailType;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.webservice.common.dto.AccountDto;
import org.linagora.linshare.core.facade.webservice.common.dto.AsyncTaskDto;
import org.linagora.linshare.core.facade.webservice.common.dto.WorkGroupEntryDto;
import org.linagora.linshare.core.facade.webservice.delegation.AsyncTaskFacade;
import org.linagora.linshare.core.facade.webservice.delegation.WorkGroupEntryFacade;
import org.linagora.linshare.core.facade.webservice.user.WorkGroupEntryAsyncFacade;
import org.linagora.linshare.webservice.WebserviceBase;
import org.linagora.linshare.webservice.delegationv2.WorkGroupEntryRestService;
import org.linagora.linshare.webservice.userv1.task.WorkGroupEntryCopyAsyncTask;
import org.linagora.linshare.webservice.userv1.task.WorkGroupEntryUploadAsyncTask;
import org.linagora.linshare.webservice.userv1.task.context.WorkGroupEntryTaskContext;
import org.linagora.linshare.webservice.utils.WebServiceUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/{actorUuid}/workgroups/{workgroupUuid}/entries")
@Api(value = "/rest/delegation/v2/{actorUuid}/workgroups/{workgroupUuid}/entries", basePath = "/rest/workgroups/{workgroupUuid}/entries",
	description = "workgroup entries service.",
	produces = "application/json,application/xml", consumes = "application/json,application/xml")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class WorkGroupEntryRestServiceImpl extends WebserviceBase implements
		WorkGroupEntryRestService {

	private final WorkGroupEntryFacade workGroupEntryFacade;

	private final WorkGroupEntryAsyncFacade workGroupEntryAsyncFacade ;

	private final AsyncTaskFacade asyncTaskFacade;

	private org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor taskExecutor;

	private boolean sizeValidation;

	public WorkGroupEntryRestServiceImpl(WorkGroupEntryFacade workgroupEntryFacade,
			WorkGroupEntryAsyncFacade workgroupEntryAsyncFacade,
			AsyncTaskFacade asyncTaskFacade,
			ThreadPoolTaskExecutor taskExecutor,
			boolean sizeValidation) {
		super();
		this.workGroupEntryFacade = workgroupEntryFacade;
		this.workGroupEntryAsyncFacade = workgroupEntryAsyncFacade;
		this.asyncTaskFacade = asyncTaskFacade;
		this.taskExecutor = taskExecutor;
		this.sizeValidation = sizeValidation;
	}

	@Path("/")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Create a workgroup entry which will contain the uploaded file.", response = WorkGroupEntryDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role."),
					@ApiResponse(code = 404, message = "Workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public WorkGroupEntryDto create(
			@ApiParam(value = "The actor (user) uuid.", required = true) 
				@PathParam("actorUuid") String actorUuid,
			@ApiParam(value = "The workgroup uuid.", required = true) 
				@PathParam("workgroupUuid") String workgroupUuid,
			@ApiParam(value = "File stream.", required = true) 
				@Multipart(value = "file", required = true) InputStream file,
			@ApiParam(value = "An optional description of a document.") 
				@Multipart(value = "description", required = false) String description,
			@ApiParam(value = "The given file name of the uploaded file.", required = false) 
				@Multipart(value = "filename", required = false) String givenFileName,
			@ApiParam(value = "True to enable asynchronous upload processing.", required = false) 
				@DefaultValue("false") @QueryParam("async") Boolean async,
			@HeaderParam("Content-Length") Long contentLength,
			@ApiParam(value = "file size (size validation purpose).", required = true) 
				@Multipart(value = "filesize", required = true)  Long fileSize,
			MultipartBody body)
					throws BusinessException {
		Long transfertDuration = WebServiceUtils.getTransfertDuration();
		if (file == null) {
			logger.error("Missing file (check parameter file)");
			throw giveRestException(HttpStatus.SC_BAD_REQUEST, "Missing file (check parameter file)");
		}
		String fileName = getFileName(givenFileName, body);
		// Default mode. No user input.
		if (async == null) {
			async = false;
		}
		File tempFile = WebServiceUtils.getTempFile(file, "rest-delegation-workgroup-entries", fileName);
		long currSize = tempFile.length();
		if (sizeValidation) {
			checkSizeValidation(contentLength, fileSize, currSize);
		}
		if (async) {
			logger.debug("Async mode is used");
			// Asynchronous mode
			AccountDto authUserDto = workGroupEntryFacade.getAuthenticatedAccountDto();
			AsyncTaskDto asyncTask = null;
			try {
				asyncTask = asyncTaskFacade.create(actorUuid, currSize, transfertDuration, fileName, null, AsyncTaskType.THREAD_ENTRY_UPLOAD);
				WorkGroupEntryTaskContext workgroupEntryTaskContext = new WorkGroupEntryTaskContext(authUserDto, actorUuid, workgroupUuid, tempFile, fileName, null);
				WorkGroupEntryUploadAsyncTask task = new WorkGroupEntryUploadAsyncTask(workGroupEntryAsyncFacade, workgroupEntryTaskContext, asyncTask);
				taskExecutor.execute(task);
				return new WorkGroupEntryDto(asyncTask, workgroupEntryTaskContext);
			} catch (Exception e) {
				logAsyncFailure(actorUuid, asyncTask, e);
				WebServiceUtils.deleteTempFile(tempFile);
				throw e;
			}
		} else {
			// TODO : manage transfertDuration
			// Synchronous mode
			try {
				logger.debug("Async mode is not used");
				return workGroupEntryFacade.create(actorUuid, workgroupUuid, tempFile, fileName);
			} finally {
				WebServiceUtils.deleteTempFile(tempFile);
			}
		}
	}

	@Path("/copy")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Create a workgroup entry which will contain the uploaded file.", response = WorkGroupEntryDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role."),
					@ApiResponse(code = 404, message = "workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public WorkGroupEntryDto copy(
			@ApiParam(value = "The actor (user) uuid.", required = true) @PathParam("actorUuid") String actorUuid,
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workgroupUuid") String workgroupUuid,
			@ApiParam(value = "The document entry uuid.", required = true) @PathParam("entryUuid")  String entryUuid,
			@ApiParam(value = "True to enable asynchronous upload processing.", required = false) @QueryParam("async") Boolean async)
					throws BusinessException {
		// Default mode. No user input.
		if (async == null) {
			async = false;
		}
		if (async) {
			logger.debug("Async mode is used");
			AccountDto authUserDto = workGroupEntryFacade.getAuthenticatedAccountDto();
			AsyncTaskDto asyncTask = null;
			try {
				asyncTask = asyncTaskFacade.create(actorUuid, entryUuid, AsyncTaskType.DOCUMENT_COPY);
				WorkGroupEntryTaskContext tetc = new WorkGroupEntryTaskContext(authUserDto, actorUuid, workgroupUuid, entryUuid, null);
				WorkGroupEntryCopyAsyncTask task = new WorkGroupEntryCopyAsyncTask(workGroupEntryAsyncFacade, tetc, asyncTask);
				taskExecutor.execute(task);
				return new WorkGroupEntryDto(asyncTask, tetc);
			} catch (Exception e) {
				logAsyncFailure(actorUuid, asyncTask, e);
				throw e;
			}
		} else {
			logger.debug("Async mode is not used");
			return workGroupEntryFacade.copy(actorUuid, workgroupUuid, entryUuid);
		}
	}

	@Path("/{uuid}")
	@GET
	@ApiOperation(value = "Get a workgroup entry.", response = WorkGroupEntryDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role.") ,
					@ApiResponse(code = 404, message = "Workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public WorkGroupEntryDto find(
			@ApiParam(value = "The actor (user) uuid.", required = true) @PathParam("actorUuid") String actorUuid,
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workgroupUuid") String workgroupUuid,
			@ApiParam(value = "The workgroup entry uuid.", required = true) @PathParam("uuid") String uuid)
					throws BusinessException {
		return workGroupEntryFacade.find(actorUuid, workgroupUuid, uuid);
	}

	@Path("/{uuid}")
	@HEAD
	@ApiOperation(value = "Get a workgroup entry.", response = WorkGroupEntryDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role.") ,
		@ApiResponse(code = 404, message = "Workgroup entry not found."),
		@ApiResponse(code = 400, message = "Bad request : missing required fields."),
		@ApiResponse(code = 500, message = "Internal server error."),
	})
	@Override
	public void head(
			@ApiParam(value = "The actor (user) uuid.", required = true) @PathParam("actorUuid") String actorUuid,
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workgroupUuid") String workgroupUuid,
			@ApiParam(value = "The workgroup entry uuid.", required = true) @PathParam("uuid") String uuid)
					throws BusinessException {
		workGroupEntryFacade.find(actorUuid, workgroupUuid, uuid);
	}

	@Path("/")
	@GET
	@ApiOperation(value = "Get all workgroup entries.", response = WorkGroupEntryDto.class, responseContainer = "Set")
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role.") ,
					@ApiResponse(code = 404, message = "Workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public List<WorkGroupEntryDto> findAll(
			@ApiParam(value = "The actor (user) uuid.", required = true) @PathParam("actorUuid") String actorUuid,
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workgroupUuid") String workgroupUuid)
					throws BusinessException {
		return workGroupEntryFacade.findAll(actorUuid, workgroupUuid);
	}

	@Path("/")
	@DELETE
	@ApiOperation(value = "Delete a workgroup entry.", response = WorkGroupEntryDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role.") ,
					@ApiResponse(code = 404, message = "Workgroup entry or workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public WorkGroupEntryDto delete(
			@ApiParam(value = "The actor (user) uuid.", required = true) @PathParam("actorUuid") String actorUuid,
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workgroupUuid") String workgroupUuid,
			@ApiParam(value = "The workgroup entry to delete.", required = true) WorkGroupEntryDto workgroupEntry)
					throws BusinessException {
		return workGroupEntryFacade.delete(actorUuid, workgroupUuid, workgroupEntry);
	}

	@Path("/{uuid}")
	@DELETE
	@ApiOperation(value = "Delete a workgroup entry.", response = WorkGroupEntryDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role.") ,
					@ApiResponse(code = 404, message = "Workgroup entry or Workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public WorkGroupEntryDto delete(
			@ApiParam(value = "The actor (user) uuid.", required = true) @PathParam("actorUuid") String actorUuid,
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workgroupUuid") String workgroupUuid,
			@ApiParam(value = "The workgroup entry uuid to delete.", required = true) @PathParam("uuid") String uuid)
					throws BusinessException {
		return workGroupEntryFacade.delete(actorUuid, workgroupUuid, uuid);
	}

	@Path("/{actorUuid}/documents/{uuid}/download")
	@GET
	@ApiOperation(value = "Download a file.")
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role."),
					@ApiResponse(code = 404, message = "Workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public Response download(
			@ApiParam(value = "The actor (user) uuid.", required = true) @PathParam("actorUuid") String actorUuid,
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workgroupUuid") String workgroupUuid,
			@ApiParam(value = "The workgroup entry uuid.", required = true) @PathParam("uuid") String uuid)
					throws BusinessException {
		return workGroupEntryFacade.download(actorUuid, workgroupUuid, uuid);
	}

	@Path("/{actorUuid}/documents/{uuid}/thumbnail")
	@GET
	@ApiOperation(value = "Download the thumbnail of a file.")
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role."),
					@ApiResponse(code = 404, message = "Workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public Response thumbnail(
			@ApiParam(value = "The actor (user) uuid.", required = true) @PathParam("actorUuid") String actorUuid,
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workgroupUuid") String workgroupUuid,
			@ApiParam(value = "The document uuid.", required = true) @PathParam("uuid") String uuid)
					throws BusinessException {
			return workGroupEntryFacade.thumbnail(actorUuid, workgroupUuid, uuid, ThumbnailType.MEDIUM);
	}

	@Path("/{actorUuid}/documents/{uuid}")
	@PUT
	@ApiOperation(value = "Update the workgroup entry properties.")
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role."),
					@ApiResponse(code = 404, message = "Workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public WorkGroupEntryDto update(
			@ApiParam(value = "The actor (user) uuid.", required = true) @PathParam("actorUuid") String actorUuid,
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workgroupUuid") String workgroupUuid,
			@ApiParam(value = "The document uuid.", required = true) @PathParam("uuid") String workgroupEntryuuid,
			@ApiParam(value = "The workgroup Entry.", required = true) WorkGroupEntryDto workgroupEntryDto)
			throws BusinessException {
		return workGroupEntryFacade.update(actorUuid, workgroupUuid, workgroupEntryuuid, workgroupEntryDto);
	}

	@Path("/{uuid}/async")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Override
	public AsyncTaskDto findAsync(
			@ApiParam(value = "The actor (user) uuid.", required = true) @PathParam("actorUuid") String actorUuid,
			@ApiParam(value = "The async task uuid.", required = true) @PathParam("uuid") String uuid)
			throws BusinessException {
		Validate.notEmpty(uuid, "Missing uuid");
		return asyncTaskFacade.find(actorUuid, uuid);
	}

	protected void logAsyncFailure(String actorUuid, AsyncTaskDto asyncTask,
			Exception e) {
		logger.error(e.getMessage());
		logger.debug("Exception : ", e);
		if (asyncTask != null) {
			asyncTaskFacade.fail(actorUuid, asyncTask, e);
		}
	}
}

