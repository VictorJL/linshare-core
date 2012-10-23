package org.linagora.linshare.core.service.impl;

import java.util.List;

import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.Thread;
import org.linagora.linshare.core.domain.entities.ThreadMember;
import org.linagora.linshare.core.domain.entities.ThreadView;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.repository.ThreadMemberRepository;
import org.linagora.linshare.core.repository.ThreadRepository;
import org.linagora.linshare.core.repository.ThreadViewRepository;
import org.linagora.linshare.core.service.ThreadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadServiceImpl implements ThreadService {

	final private static Logger logger = LoggerFactory.getLogger(ThreadServiceImpl.class);
	
	private final ThreadRepository threadRepository;
	
	private final ThreadViewRepository threadViewRepository;
	
	private ThreadMemberRepository threadMemberRepository;
    
	
	public ThreadServiceImpl(ThreadRepository threadRepository, ThreadViewRepository threadViewRepository, ThreadMemberRepository threadMemberRepository) {
		super();
		this.threadRepository = threadRepository;
		this.threadViewRepository = threadViewRepository;
		this.threadMemberRepository = threadMemberRepository;
	}
	
	@Override
	public Thread findByLsUuid(String uuid) {
		Thread thread = threadRepository.findByLsUuid(uuid);
		if (thread == null) {
			logger.error("Can't find thread  : " + uuid);
		}
		return thread;
	}

	@Override
	public List<Thread> findAll() {
		List<Thread> all = threadRepository.findAll();
		logger.debug("count : " + all.size());
		return all;
	}

	@Override
	public void create(Account actor, String name) throws BusinessException {
		Thread thread = null;
		ThreadView threadView = null;
		ThreadMember member = null;
		
		thread = new Thread(actor.getDomain(), actor, name);
		threadRepository.create(thread);
		threadView = new ThreadView(thread);
		threadViewRepository.create(threadView);
		thread.setCurrentThreadView(threadView);
		threadRepository.update(thread);
		member = new ThreadMember(true, true, (User)actor, thread);
		thread.getMyMembers().add(member);
		threadRepository.update(thread);	
	}

	@Override
	public ThreadMember getThreadMemberById(String id) {
		if (id == null) {
			logger.debug("id is null");
			return null;
		}
		return threadMemberRepository.findById(id);
	}

	@Override
	public ThreadMember getThreadMemberFromUser(Thread thread, User user) {
		if (thread == null || user == null) {
			logger.debug("null parameter");
			return null;
		}
		return threadMemberRepository.findUserThreadMember(thread, user);
	}

}
