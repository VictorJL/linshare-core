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

import org.jsoup.helper.Validate;
import org.linagora.linshare.core.business.service.DriveMemberBusinessService;
import org.linagora.linshare.core.business.service.SharedSpaceMemberBusinessService;
import org.linagora.linshare.core.domain.constants.AuditLogEntryType;
import org.linagora.linshare.core.domain.constants.LogAction;
import org.linagora.linshare.core.domain.constants.NodeType;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.domain.objects.MailContainerWithRecipient;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.notifications.context.EmailContext;
import org.linagora.linshare.core.notifications.context.WorkGroupWarnDeletedMemberEmailContext;
import org.linagora.linshare.core.notifications.context.WorkGroupWarnNewMemberEmailContext;
import org.linagora.linshare.core.notifications.context.WorkGroupWarnUpdatedMemberEmailContext;
import org.linagora.linshare.core.notifications.service.MailBuildingService;
import org.linagora.linshare.core.rac.SharedSpaceMemberResourceAccessControl;
import org.linagora.linshare.core.repository.UserRepository;
import org.linagora.linshare.core.service.LogEntryService;
import org.linagora.linshare.core.service.NotifierService;
import org.linagora.linshare.core.service.SharedSpaceMemberService;
import org.linagora.linshare.mongo.entities.SharedSpaceMemberDrive;
import org.linagora.linshare.mongo.entities.SharedSpaceAccount;
import org.linagora.linshare.mongo.entities.SharedSpaceMember;
import org.linagora.linshare.mongo.entities.SharedSpaceNode;
import org.linagora.linshare.mongo.entities.SharedSpaceNodeNested;
import org.linagora.linshare.mongo.entities.SharedSpaceRole;
import org.linagora.linshare.mongo.entities.light.GenericLightEntity;
import org.linagora.linshare.mongo.entities.logs.AuditLogEntryUser;
import org.linagora.linshare.mongo.entities.logs.SharedSpaceMemberAuditLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class SharedSpaceMemberServiceImpl extends GenericServiceImpl<Account, SharedSpaceMember>
		implements SharedSpaceMemberService {

	protected static final String AUDIT_MEMBER = "_MEMBER";

	protected final SharedSpaceMemberBusinessService businessService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SharedSpaceMemberServiceImpl.class);

	protected final LogEntryService logEntryService;

	protected final UserRepository<User> userRepository;

	protected final NotifierService notifierService;

	protected final MailBuildingService mailBuildingService;

	protected final DriveMemberBusinessService driveMemberBusinessService;

	public SharedSpaceMemberServiceImpl(SharedSpaceMemberBusinessService businessService,
			NotifierService notifierService,
			MailBuildingService mailBuildingService,
			SharedSpaceMemberResourceAccessControl rac,
			LogEntryService logEntryService,
			UserRepository<User> userRepository,
			DriveMemberBusinessService driveMemberBusinessService) {
		super(rac);
		this.businessService = businessService;
		this.logEntryService = logEntryService;
		this.userRepository = userRepository;
		this.notifierService = notifierService;
		this.mailBuildingService = mailBuildingService;
		this.driveMemberBusinessService = driveMemberBusinessService;
	}

	@Override
	public SharedSpaceMember find(Account authUser, Account actor, String uuid) throws BusinessException {
		preChecks(authUser, actor);
		Validate.notEmpty(uuid, "Missing required shared space member uuid");
		SharedSpaceMember toFind = businessService.find(uuid);
		if (toFind == null) {
			throw new BusinessException(BusinessErrorCode.SHARED_SPACE_MEMBER_NOT_FOUND,
					"The Shared space member with uuid : " + uuid + " is not found");
		}
		checkReadPermission(authUser, actor, SharedSpaceMember.class, BusinessErrorCode.SHARED_SPACE_MEMBER_FORBIDDEN,
				toFind);
		return toFind;
	}

	@Override
	public SharedSpaceMember findMemberByNodeAndUuid(Account authUser, Account actor, String nodeUuid, String uuid)
			throws BusinessException {
		preChecks(authUser, actor);
		Validate.notEmpty(uuid, "memberUuid must be set.");
		Validate.notEmpty(nodeUuid, "nodeUuid must be set.");
		SharedSpaceMember foundMember = businessService.findByNodeAndUuid(nodeUuid, uuid);
		if (foundMember == null) {
			String message = String.format(
					"The member with the UUID : %s is not a member of the node with the uuid : %s", uuid, nodeUuid);
			throw new BusinessException(BusinessErrorCode.SHARED_SPACE_MEMBER_NOT_FOUND, message);
		}
		checkReadPermission(authUser, actor, SharedSpaceMember.class, BusinessErrorCode.SHARED_SPACE_MEMBER_FORBIDDEN,
				foundMember);
		return foundMember;
	}

	@Override
	public SharedSpaceMember findMemberByUuid(Account authUser, Account actor, String userUuid, String nodeUuid)
			throws BusinessException {
		preChecks(authUser, actor);
		Validate.notEmpty(userUuid, "userUuid must be set.");
		Validate.notEmpty(nodeUuid, "nodeUuid must be set.");
		SharedSpaceMember foundMember = businessService.findByAccountAndNode(userUuid, nodeUuid);
		if (foundMember == null) {
			String message = String.format(
					"The account with the UUID : %s is not a member of the node with the uuid : %s", userUuid,
					nodeUuid);
			throw new BusinessException(BusinessErrorCode.SHARED_SPACE_MEMBER_NOT_FOUND, message);
		}
		checkReadPermission(authUser, actor, SharedSpaceMember.class, BusinessErrorCode.SHARED_SPACE_MEMBER_FORBIDDEN,
				foundMember);
		return foundMember;
	}

	@Override
	public List<SharedSpaceMember> findAll(Account authUser, Account actor, String shareSpaceNodeUuid)
			throws BusinessException {
		preChecks(authUser, actor);
		Validate.notEmpty(shareSpaceNodeUuid, "Missing required shared space node uuid");
		checkListPermission(authUser, actor, SharedSpaceMember.class, BusinessErrorCode.SHARED_SPACE_MEMBER_FORBIDDEN,
				null, shareSpaceNodeUuid);
		List<SharedSpaceMember> foundMembers = businessService.findBySharedSpaceNodeUuid(shareSpaceNodeUuid);
		return foundMembers;
	}

	@Override
	public List<SharedSpaceMember> findAllByAccountAndRole(String accountUuid, String roleUuid) {
		return businessService.findAllByAccountAndRole(accountUuid, roleUuid);
	}

	@Override
	public List<SharedSpaceNodeNested> findAllByAccount(Account authUser, Account actor, String accountUuid, boolean withRole) {
		preChecks(authUser, actor);
		Validate.notEmpty(accountUuid, "accountUuid must be set.");
		return businessService.findAllNestedNodeByAccountUuid(accountUuid, withRole);
	}

	@Override
	public List<SharedSpaceMember> findByNode(Account authUser, Account actor, String ssnodeUuid) {
		preChecks(authUser, actor);
		Validate.notEmpty(ssnodeUuid, "The shared space node uuid must be set.");
		return businessService.findBySharedSpaceNodeUuid(ssnodeUuid);
	}

	@Override
	public SharedSpaceMember create(Account authUser, Account actor, SharedSpaceNode node, SharedSpaceRole role,
			SharedSpaceAccount account) throws BusinessException {
		return create(authUser, actor, node, role, null, account);
	}

	@Override
	public SharedSpaceMember create(Account authUser, Account actor, SharedSpaceNode node, SharedSpaceRole role, SharedSpaceRole driveRole,
			SharedSpaceAccount account) throws BusinessException {
		Validate.notNull(role, "Role must be set.");
		Validate.notNull(node, "Node uuid must be set.");
		checkCreatePermission(authUser, actor, SharedSpaceMember.class, BusinessErrorCode.SHARED_SPACE_MEMBER_FORBIDDEN,
				null, node);
		User newMember = userRepository.findByLsUuid(account.getUuid());
		if (newMember == null) {
			String message = String.format("The account with the UUID : %s is not existing", account.getUuid());
			throw new BusinessException(BusinessErrorCode.SHARED_SPACE_MEMBER_NOT_FOUND, message);
		}
		if (!checkMemberNotInNode(account.getUuid(), node.getUuid())) {
			String message = String.format(
					"The account with the UUID : %s is already a member of the node with the uuid : %s",
					account.getUuid(), node.getUuid());
			throw new BusinessException(BusinessErrorCode.SHARED_SPACE_MEMBER_ALREADY_EXISTS, message);
		}
		SharedSpaceMember member = createWithoutCheckPermission(authUser, actor, node, role, driveRole, account);
		notify(new WorkGroupWarnNewMemberEmailContext(member, actor, newMember));
		return member;
	}

	@Override
	public SharedSpaceMember createWithoutCheckPermission(Account authUser, Account actor, SharedSpaceNode node,
			SharedSpaceRole role, SharedSpaceRole nestedRole, SharedSpaceAccount account) throws BusinessException {
		preChecks(authUser, actor);
		Validate.notNull(role, "Role must be set.");
		Validate.notNull(node, "Node must be set.");
		SharedSpaceMember toAdd = new SharedSpaceMember();
		if (NodeType.DRIVE.equals(node.getNodeType())) {
			SharedSpaceMemberDrive member = new SharedSpaceMemberDrive(new SharedSpaceNodeNested(node),
					new GenericLightEntity(role.getUuid(), role.getName()), account,
					new GenericLightEntity(nestedRole.getUuid(), nestedRole.getName()));
			Validate.notNull(nestedRole, "Drive role must be set.");
			member.setNestedRole(new GenericLightEntity(nestedRole.getUuid(), nestedRole.getName()));
			toAdd = driveMemberBusinessService.create(member);
		} else if (NodeType.WORK_GROUP.equals(node.getNodeType())) {
			SharedSpaceMember memberWg = new SharedSpaceMember(new SharedSpaceNodeNested(node),
					new GenericLightEntity(role.getUuid(), role.getName()), account);
			toAdd = businessService.create(memberWg);
		} else {
			throw new BusinessException(BusinessErrorCode.SHARED_SPACE_MEMBER_FORBIDDEN, "Node type not supported");
		}
		saveLog(authUser, actor, LogAction.CREATE, toAdd);
		return toAdd;
	}

	@Override
	public SharedSpaceMember delete(Account authUser, Account actor, String uuid) {
		preChecks(authUser, actor);
		Validate.notNull(uuid, "Missing required member uuid to delete");
		SharedSpaceMember foundMemberToDelete = find(authUser, actor, uuid);
		checkDeletePermission(authUser, actor, SharedSpaceMember.class, BusinessErrorCode.SHARED_SPACE_MEMBER_FORBIDDEN,
				foundMemberToDelete);
		businessService.delete(foundMemberToDelete);
		saveLog(authUser, actor, LogAction.DELETE, foundMemberToDelete);
		User user = userRepository.findByLsUuid(foundMemberToDelete.getAccount().getUuid());
		notify(new WorkGroupWarnDeletedMemberEmailContext(foundMemberToDelete, actor, user));
		return foundMemberToDelete;
	}

	@Override
	public SharedSpaceMember update(Account authUser, Account actor, SharedSpaceMemberDrive memberToUpdate) {
		preChecks(authUser, actor);
		Validate.notNull(memberToUpdate, "Missing required member to update");
		SharedSpaceMember foundMemberToUpdate = null;
		/**
		 * TODO To ensure API compatibility, we fix the current api and we add a
		 * fallback to ensure old API usage is still working properly. At least ui-user
		 * 2.3.x is using it..
		 */
		try {
			foundMemberToUpdate = find(authUser, actor, memberToUpdate.getUuid());
		} catch (BusinessException e) {
			Validate.notNull(memberToUpdate.getAccount(), "You must set the account.");
			LOGGER.info("This is just a fallback to ensure old API usage : {}", e.getMessage());
			foundMemberToUpdate = findMemberByUuid(authUser, actor, memberToUpdate.getAccount().getUuid(),
					memberToUpdate.getNode().getUuid());
		}
		Validate.notNull(memberToUpdate.getUuid(), "Missing required member uuid to update");
		SharedSpaceMember foundMemberToUpdate = findMemberByUuid(authUser, actor, memberToUpdate.getAccount().getUuid(),
				memberToUpdate.getNode().getUuid());
		checkUpdatePermission(authUser, actor, SharedSpaceMember.class, BusinessErrorCode.SHARED_SPACE_MEMBER_FORBIDDEN,
				foundMemberToUpdate);
<<<<<<< HEAD
		SharedSpaceMember updated = businessService.update(foundMemberToUpdate, memberToUpdate);
		SharedSpaceMemberAuditLogEntry log = new SharedSpaceMemberAuditLogEntry(authUser, actor, LogAction.UPDATE,
				AuditLogEntryType.WORKGROUP_MEMBER, foundMemberToUpdate);
		log.setResourceUpdated(updated);
		addMembersToLog(memberToUpdate.getNode().getUuid(), log);
		logEntryService.insert(log);
		User user = userRepository.findByLsUuid(foundMemberToUpdate.getAccount().getUuid());
		notify(new WorkGroupWarnUpdatedMemberEmailContext(updated, user, actor));
=======
		SharedSpaceMember updated = new SharedSpaceMember();
		if (NodeType.DRIVE.equals(memberToUpdate.getNode().getNodeType())) {
			foundMemberToUpdate = (SharedSpaceMemberDrive) foundMemberToUpdate;
			updated = driveMemberBusinessService.update(foundMemberToUpdate, foundMemberToUpdate);
		} else if (NodeType.WORK_GROUP.equals(memberToUpdate.getNode().getNodeType())) {
			updated = businessService.update(foundMemberToUpdate, memberToUpdate);
			User user = userRepository.findByLsUuid(foundMemberToUpdate.getAccount().getUuid());
			notify(new WorkGroupWarnUpdatedMemberEmailContext(updated, user, actor));
		} else {
			throw new BusinessException(BusinessErrorCode.SHARED_SPACE_MEMBER_FORBIDDEN, "Node type not supported");
		}
		saveUpdateLog(authUser, actor, LogAction.UPDATE, foundMemberToUpdate, updated);
>>>>>>> improve drive member crud
		return updated;
	}

	@Override
	public List<SharedSpaceMember> deleteAllMembers(Account authUser, Account actor, String sharedSpaceNodeUuid) {
		preChecks(authUser, actor);
		Validate.notNull(sharedSpaceNodeUuid, "Missing required sharedSpaceNodeUuid");
		List<SharedSpaceMember> foundMembersToDelete = findAll(authUser, actor, sharedSpaceNodeUuid);
		if (foundMembersToDelete != null && !foundMembersToDelete.isEmpty()) {
			// We check the user has the right to delete members of this node
			// If he can delete one member, he can delete them all
			checkDeletePermission(authUser, actor, SharedSpaceMember.class,
					BusinessErrorCode.SHARED_SPACE_MEMBER_FORBIDDEN, foundMembersToDelete.get(0));
		}
		businessService.deleteAll(foundMembersToDelete);
		List<AuditLogEntryUser> logs = Lists.newArrayList();
		for (SharedSpaceMember member : foundMembersToDelete) {
			User user = userRepository.findByLsUuid(member.getAccount().getUuid());
			notify(new WorkGroupWarnDeletedMemberEmailContext(member, actor, user));
			logs.add(new SharedSpaceMemberAuditLogEntry(authUser, actor, LogAction.DELETE,
					AuditLogEntryType.WORKGROUP_MEMBER, member));
		}
		if (logs != null && !logs.isEmpty()) {
			logEntryService.insert(logs);
		}
		return foundMembersToDelete;
	}

	@Override
	public List<SharedSpaceMember> deleteAllUserMemberships(Account authUser, Account actor, String userUuid) {
		preChecks(authUser, actor);
		Validate.notNull(userUuid, "Missing required sharedSpaceNodeUuid");
		List<SharedSpaceMember> foundMembersToDelete = businessService.findAllUserMemberships(userUuid);
		for (SharedSpaceMember member : foundMembersToDelete) {
			delete(authUser, actor, member.getUuid());
		}
		return foundMembersToDelete;
	}

	protected boolean checkMemberNotInNode(String possibleMemberUuid, String nodeUuid) {
		return businessService.findByAccountAndNode(possibleMemberUuid, nodeUuid) == null;
	}

	protected void notify(EmailContext context) {
		MailContainerWithRecipient mail = mailBuildingService.build(context);
		notifierService.sendNotification(mail, true);
	}

	protected SharedSpaceMemberAuditLogEntry saveLog(Account authUser, Account actor, LogAction action,
			SharedSpaceMember resource) {
		AuditLogEntryType auditType = AuditLogEntryType.fromNodeType(resource.getNode().getNodeType().toString());
		SharedSpaceMemberAuditLogEntry log = new SharedSpaceMemberAuditLogEntry(authUser, actor, action,
				AuditLogEntryType.WORKGROUP_MEMBER, resource);
		addMembersToLog(resource.getNode().getUuid(), log);
		logEntryService.insert(log);
		return log;
	}

	protected SharedSpaceMemberAuditLogEntry saveUpdateLog(Account authUser, Account actor, LogAction action,
			SharedSpaceMember resource, SharedSpaceMember resourceUpdated) {
		AuditLogEntryType auditType = AuditLogEntryType.fromNodeType(resource.getNode().getNodeType().toString());
		SharedSpaceMemberAuditLogEntry log = new SharedSpaceMemberAuditLogEntry(authUser, actor, action,
				AuditLogEntryType.fromString(auditType.toString().concat(AUDIT_MEMBER)), resource);
		log.setResourceUpdated(resourceUpdated);
		addMembersToLog(resource.getNode().getUuid(), log);
		logEntryService.insert(log);
		return log;
	}

	@Override
	public List<SharedSpaceMember> findAllUserMemberships(Account authUser, Account actor) {
		preChecks(authUser, actor);
		return businessService.findAllUserMemberships(actor.getLsUuid());
	}

	@Override
	public void addMembersToLog(String workGroupUuid, AuditLogEntryUser log) {
		List<String> members = businessService.findMembersUuidBySharedSpaceNodeUuid(workGroupUuid);
		log.addRelatedAccounts(members);
	}

}
