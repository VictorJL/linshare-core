/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2018 LINAGORA
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
package org.linagora.linshare.core.service.impl;

import java.util.List;

import javax.ws.rs.NotSupportedException;

import org.jsoup.helper.Validate;
import org.linagora.linshare.core.business.service.AccountQuotaBusinessService;
import org.linagora.linshare.core.business.service.SharedSpaceMemberBusinessService;
import org.linagora.linshare.core.business.service.SharedSpaceNodeBusinessService;
import org.linagora.linshare.core.domain.constants.AuditLogEntryType;
import org.linagora.linshare.core.domain.constants.LogAction;
import org.linagora.linshare.core.domain.constants.NodeType;
import org.linagora.linshare.core.domain.entities.AbstractDomain;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.BooleanValueFunctionality;
import org.linagora.linshare.core.domain.entities.Functionality;
import org.linagora.linshare.core.domain.entities.Quota;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.domain.entities.WorkGroup;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.webservice.common.dto.PatchDto;
import org.linagora.linshare.core.facade.webservice.common.dto.WorkGroupDto;
import org.linagora.linshare.core.rac.SharedSpaceNodeResourceAccessControl;
import org.linagora.linshare.core.repository.ThreadRepository;
import org.linagora.linshare.core.service.FunctionalityReadOnlyService;
import org.linagora.linshare.core.service.LogEntryService;
import org.linagora.linshare.core.service.SharedSpaceMemberDriveService;
import org.linagora.linshare.core.service.SharedSpaceMemberService;
import org.linagora.linshare.core.service.SharedSpaceNodeService;
import org.linagora.linshare.core.service.SharedSpaceRoleService;
import org.linagora.linshare.core.service.ThreadService;
import org.linagora.linshare.core.service.WorkGroupNodeService;
import org.linagora.linshare.mongo.entities.SharedSpaceAccount;
import org.linagora.linshare.mongo.entities.SharedSpaceMember;
import org.linagora.linshare.mongo.entities.SharedSpaceNode;
import org.linagora.linshare.mongo.entities.SharedSpaceNodeNested;
import org.linagora.linshare.mongo.entities.SharedSpaceRole;
import org.linagora.linshare.mongo.entities.VersioningParameters;
import org.linagora.linshare.mongo.entities.WorkGroupNode;
import org.linagora.linshare.mongo.entities.light.GenericLightEntity;
import org.linagora.linshare.mongo.entities.logs.SharedSpaceNodeAuditLogEntry;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class SharedSpaceNodeServiceImpl extends GenericServiceImpl<Account, SharedSpaceNode>
		implements SharedSpaceNodeService {

	private final SharedSpaceNodeBusinessService businessService;

	private final SharedSpaceMemberBusinessService memberBusinessService;

	private final SharedSpaceMemberService memberService;

	private final SharedSpaceRoleService ssRoleService;

	private final LogEntryService logEntryService;

	protected final ThreadService threadService;

	protected final ThreadRepository threadRepository;
	
	protected final FunctionalityReadOnlyService functionalityService;

	protected final AccountQuotaBusinessService accountQuotaBusinessService;
	
	protected final WorkGroupNodeService workGroupNodeService;

	private final SharedSpaceMemberDriveService memberDriveService;

	public SharedSpaceNodeServiceImpl(SharedSpaceNodeBusinessService businessService,
			SharedSpaceNodeResourceAccessControl rac,
			SharedSpaceMemberBusinessService memberBusinessService,
			SharedSpaceMemberService memberService,
			SharedSpaceRoleService ssRoleService,
			LogEntryService logEntryService,
			ThreadService threadService,
			ThreadRepository threadRepository,
			FunctionalityReadOnlyService functionalityService,
			AccountQuotaBusinessService accountQuotaBusinessService,
			WorkGroupNodeService workGroupNodeService,
			SharedSpaceMemberDriveService memberDriveService) {
		super(rac);
		this.businessService = businessService;
		this.memberService = memberService;
		this.ssRoleService = ssRoleService;
		this.memberBusinessService = memberBusinessService;
		this.logEntryService = logEntryService;
		this.threadService = threadService;
		this.functionalityService = functionalityService;
		this.accountQuotaBusinessService = accountQuotaBusinessService;
		this.threadRepository = threadRepository;
		this.workGroupNodeService = workGroupNodeService;
		this.memberDriveService = memberDriveService;
	}

	@Override
	public SharedSpaceNode find(Account authUser, Account actor, String uuid) throws BusinessException {
		preChecks(authUser, actor);
		Validate.notEmpty(uuid, "Missing required shared space node uuid.");
		SharedSpaceNode found = businessService.find(uuid);
		if (found == null) {
			throw new BusinessException(BusinessErrorCode.SHARED_SPACE_NODE_NOT_FOUND,
					"The shared space node with uuid: " + uuid + " is not found");
		}
		checkReadPermission(authUser, actor, SharedSpaceNode.class, getBusinessErrorCode(found.getNodeType()), found);
		return found;
	}

	@Override
	public SharedSpaceNode findWithRole(Account authUser, Account actor, String uuid) throws BusinessException {
		SharedSpaceNode node = find(authUser, actor, uuid);
		SharedSpaceMember member = memberService.findMemberByUuid(authUser, actor, actor.getLsUuid(), uuid);
		node.setRole(new GenericLightEntity(member.getRole()));
		return node;
	}

	@Override
	public SharedSpaceNode create(Account authUser, Account actor, SharedSpaceNode node) throws BusinessException {
		preChecks(authUser, actor);
		Validate.notNull(node, "Missing required input shared space node.");
		Validate.notNull(node.getNodeType(), "you must set the node type");
		if (!NodeType.WORK_GROUP.equals(node.getNodeType())) {
			throw new BusinessException(BusinessErrorCode.WORK_GROUP_OPERATION_UNSUPPORTED,
					"Can not create this kind of sharedSpace with this method.");
		}
		checkVersioningParameter(actor.getDomain(), node);
		checkCreatePermission(authUser, actor, SharedSpaceNode.class, BusinessErrorCode.WORK_GROUP_FORBIDDEN, node);
		checkCreatePermission(authUser, actor, SharedSpaceNode.class, getBusinessErrorCode(node.getNodeType()), node);
		SharedSpaceNode created = new SharedSpaceNode();
		SharedSpaceRole role = ssRoleService.getAdmin(authUser, actor);
		SharedSpaceRole nestedRole = null;
		// Hack to create thread into shared space node
		created = simpleCreate(authUser, actor, node);
		if (NodeType.DRIVE.equals(node.getNodeType())) {
			nestedRole = ssRoleService.getDriveAdmin(authUser, actor);
			memberDriveService.createWithoutCheckPermission(authUser, actor, created, role, nestedRole,
					new SharedSpaceAccount((User) actor));
		} else if (NodeType.WORK_GROUP.equals(node.getNodeType())) {
			memberService.createWithoutCheckPermission(authUser, actor, created, role,
					new SharedSpaceAccount((User) actor));
		} else {
			throw new NotSupportedException("Node type not supported");
		}
		return created;
	}

	protected void checkVersioningParameter(AbstractDomain domain, SharedSpaceNode node) {
		BooleanValueFunctionality versioningFunctionality = functionalityService.getWorkGroupFileVersioning(domain);
		Boolean userValue = node.getVersioningParameters() == null ? null : node.getVersioningParameters().getEnable();
		node.setVersioningParameters(new VersioningParameters(versioningFunctionality.getFinalValue(userValue)));
	}

	protected SharedSpaceNode simpleCreate(Account authUser, Account actor, SharedSpaceNode node)
			throws BusinessException {
		if (node.getNodeType().equals(NodeType.WORK_GROUP)) {
			// Hack to create thread into shared space node
			WorkGroup workGroup = threadService.create(authUser, actor, node.getName());
			Quota workgroupQuota = 	accountQuotaBusinessService.find(workGroup);
			node.setUuid(workGroup.getLsUuid());
			node.setQuotaUuid(workgroupQuota.getUuid());
		}
		SharedSpaceNode created = businessService.create(node);
		saveLog(authUser, actor, LogAction.CREATE, created);
		return created;
	}

	/**
	 * Only use to compability with threadFacade
	 *
	 */
	@Deprecated
	public WorkGroupDto createWorkGroupDto(Account authUser, Account actor, SharedSpaceNode node)
			throws BusinessException {
		preChecks(authUser, actor);
		Validate.notNull(node, "Missing required input shared space node.");
		Validate.notNull(node.getNodeType(), "you must set the node type");
		checkVersioningParameter(actor.getDomain(), node);
		checkCreatePermission(authUser, actor, SharedSpaceNode.class, BusinessErrorCode.WORK_GROUP_FORBIDDEN, node);
		if (!node.getNodeType().equals(NodeType.WORK_GROUP)) {
			throw new BusinessException(BusinessErrorCode.WORK_GROUP_FORBIDDEN, "Node type not supported");
		}
		// Hack to create thread into shared space node
		SharedSpaceNode created = simpleCreate(authUser, actor, node);
		SharedSpaceRole role = ssRoleService.getAdmin(authUser, actor);
		memberService.createWithoutCheckPermission(authUser, actor, created, role,
					new SharedSpaceAccount((User) actor));
		WorkGroup workGroup = threadService.find(authUser, actor, created.getUuid());
		return new WorkGroupDto(workGroup, created);
	}

	@Override
	public SharedSpaceNode delete(Account authUser, Account actor, SharedSpaceNode node) throws BusinessException {
		preChecks(authUser, actor);
		Validate.notNull(node, "missing required node to delete.");
		Validate.notEmpty(node.getUuid(), "missing required node uuid to delete");
		SharedSpaceNode foundedNodeTodel = find(authUser, actor, node.getUuid());
		checkDeletePermission(authUser, actor, SharedSpaceNode.class,
				getBusinessErrorCode(foundedNodeTodel.getNodeType()), foundedNodeTodel);
		simpleDelete(authUser, actor, foundedNodeTodel);
		return foundedNodeTodel;
	}

	/**
	 * Only use to compability with threadFacade
	 *
	 */
	@Override
	public WorkGroupDto deleteWorkgroupDto(Account authUser, Account actor, SharedSpaceNode node)
			throws BusinessException {
		preChecks(authUser, actor);
		Validate.notNull(node, "missing required node to delete.");
		Validate.notEmpty(node.getUuid(), "missing required node uuid to delete");
		SharedSpaceNode foundedNodeTodel = find(authUser, actor, node.getUuid());
		checkDeletePermission(authUser, actor, SharedSpaceNode.class, BusinessErrorCode.WORK_GROUP_FORBIDDEN,
				foundedNodeTodel);
		WorkGroup workGroup = simpleDelete(authUser, actor, foundedNodeTodel);
		return new WorkGroupDto(workGroup, foundedNodeTodel);
	}

	private WorkGroup simpleDelete(Account authUser, Account actor, SharedSpaceNode foundedNodeTodel)
			throws BusinessException {
		WorkGroup workGroup = new WorkGroup();
		if (NodeType.WORK_GROUP.equals(foundedNodeTodel.getNodeType())) {
			workGroup = threadService.find(authUser, authUser, foundedNodeTodel.getUuid());
			threadService.deleteThread(authUser, authUser, workGroup);
			memberService.deleteAllMembers(authUser, actor, foundedNodeTodel.getUuid());
		}
		businessService.delete(foundedNodeTodel);
		saveLog(authUser, actor, LogAction.DELETE, foundedNodeTodel);
		return workGroup;
	}

	@Override
	public SharedSpaceNode update(Account authUser, Account actor, SharedSpaceNode nodeToUpdate)
			throws BusinessException {
		Validate.notNull(nodeToUpdate, "nodeToUpdate must be set.");
		Validate.notEmpty(nodeToUpdate.getUuid(), "shared space node uuid to update must be set.");
		Validate.notNull(nodeToUpdate.getVersioningParameters(),
				"The nested versioning object must be set for the whole object update");
		SharedSpaceNode node = find(authUser, actor, nodeToUpdate.getUuid());
		SharedSpaceNode nodeLog = new SharedSpaceNode(node);
		SharedSpaceNodeAuditLogEntry log = new SharedSpaceNodeAuditLogEntry(authUser, actor, LogAction.UPDATE,
				AuditLogEntryType.WORKGROUP, nodeLog);
		checkUpdatePermission(authUser, actor, SharedSpaceNode.class, BusinessErrorCode.WORK_GROUP_FORBIDDEN,
				nodeToUpdate);
		checkUpdateVersioningParameters(nodeToUpdate.getVersioningParameters(), node.getVersioningParameters(),
				actor.getDomain());
		SharedSpaceNode updated = businessService.update(node, nodeToUpdate);
		memberBusinessService.updateNestedNode(updated);
		//For compatibility with deprecated Thread API, should be removed when this api taken off
		if (updated.getNodeType().equals(NodeType.WORK_GROUP)) {
			WorkGroup wg = threadRepository.findByLsUuid(updated.getUuid());
			wg.setName(updated.getName());
			threadRepository.update(wg);
			WorkGroupNode rootFolder = workGroupNodeService.getRootFolder(authUser, actor, wg);
			rootFolder.setName(updated.getName());
			workGroupNodeService.update(authUser, actor, wg, rootFolder);
		}
		memberService.addMembersToLog(updated.getUuid(), log);
		log.setResourceUpdated(updated);
		logEntryService.insert(log);
		return updated;
	}

	/**
	 * Method that allow to do a partial update of shared space by sending a patch
	 * that contains the name of the attributes and there values.
	 */
	@Override
	public SharedSpaceNode updatePartial(Account authUser, Account actor, PatchDto patchNode) throws BusinessException {
		SharedSpaceNode nodeToUpdate = find(authUser, actor, patchNode.getUuid());
		if (patchNode.getName().equals("name")) {
			nodeToUpdate.setName(patchNode.getValue());
		} else {
			throw new BusinessException("Unsupported field name, allowed values: name");
		}
		return update(authUser, actor, nodeToUpdate);
	}

	protected void checkUpdateVersioningParameters(VersioningParameters newParam, VersioningParameters parameter,
			AbstractDomain domain) {
		if (!parameter.equals(newParam)) {
			Functionality versioning = functionalityService.getWorkGroupFileVersioning(domain);
			if (!versioning.getDelegationPolicy().getStatus()) {
				logger.error(
						"The current domain does not allow you to update the versioning parameters on the shared space node.");
				throw new BusinessException(BusinessErrorCode.SHARED_SPACE_MEMBER_FORBIDDEN,
						"can not update shared space versioning parameters, you are not authorized.");
			}
		}
	}

	@Override
	public List<SharedSpaceNode> findAll(Account authUser, Account actor) {
		preChecks(authUser, actor);
		checkListPermission(authUser, actor, SharedSpaceNode.class, BusinessErrorCode.SHARED_SPACE_NODE_FORBIDDEN, null);
		return businessService.findAll();
	}

	@Override
	public List<SharedSpaceNodeNested> findAllByAccount(Account authUser, Account actor) {
		preChecks(authUser, actor);
		checkListPermission(authUser, actor, SharedSpaceNode.class, BusinessErrorCode.WORK_GROUP_FORBIDDEN, null);
		return memberService.findAllByAccount(authUser, actor, actor.getLsUuid(), false);
	}

	@Override
	public List<SharedSpaceMember> findAllMembers(Account authUser, Account actor, String sharedSpaceNodeUuid,
			String accountUuid) {
		List<SharedSpaceMember> members = Lists.newArrayList();
		if (Strings.isNullOrEmpty(accountUuid)) {
			members = memberService.findAll(authUser, actor, sharedSpaceNodeUuid);
		} else {
			members.add(memberService.findMemberByUuid(authUser, actor, accountUuid, sharedSpaceNodeUuid));
		}
		return members;
	}

	@Override
	public List<SharedSpaceNode> searchByName(Account authUser, Account actor, String name) throws BusinessException {
		preChecks(authUser, actor);
		Validate.notEmpty(name, "Missing required shared space node name.");
		List<SharedSpaceNode> founds = businessService.searchByName(name);
		checkListPermission(authUser, actor, SharedSpaceNode.class, BusinessErrorCode.SHARED_SPACE_NODE_FORBIDDEN, null);
		return founds;
	}

	protected SharedSpaceNodeAuditLogEntry saveLog(Account authUser, Account actor, LogAction action,
			SharedSpaceNode resource) {
		SharedSpaceNodeAuditLogEntry log = new SharedSpaceNodeAuditLogEntry(authUser, actor, action,
				AuditLogEntryType.fromNodeType(resource.getNodeType().toString()) , resource);
		logEntryService.insert(log);
		return log;
	}

	protected SharedSpaceNodeAuditLogEntry saveUpdateLog(Account authUser, Account actor, SharedSpaceNode resource,
			SharedSpaceNode resourceUpdated) {
		SharedSpaceNodeAuditLogEntry log = new SharedSpaceNodeAuditLogEntry(authUser, actor, LogAction.UPDATE,
				AuditLogEntryType.fromNodeType(resource.getNodeType().toString()), resource);
		log.setResourceUpdated(resourceUpdated);
		logEntryService.insert(log);
		return log;
	}

	private BusinessErrorCode getBusinessErrorCode(NodeType nodeType) {
		if (NodeType.DRIVE.equals(nodeType)) {
			return BusinessErrorCode.DRIVE_FORBIDDEN;
		} else {
			return BusinessErrorCode.WORK_GROUP_FORBIDDEN;
		}
	}
}
