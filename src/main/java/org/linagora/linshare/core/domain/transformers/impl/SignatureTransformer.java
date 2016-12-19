/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2015-2016 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2015. Contribute to
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
package org.linagora.linshare.core.domain.transformers.impl;

import java.util.ArrayList;
import java.util.List;

import org.linagora.linshare.core.domain.entities.Signature;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.domain.transformers.Transformer;
import org.linagora.linshare.core.domain.vo.SignatureVo;
import org.linagora.linshare.core.domain.vo.UserVo;
import org.linagora.linshare.core.exception.TechnicalException;

public class SignatureTransformer implements Transformer<Signature, SignatureVo>{


	private final UserTransformer userTransformer;


	public SignatureTransformer(final UserTransformer userTransformer){
		this.userTransformer = userTransformer;
	}

	public Signature assemble(SignatureVo valueObject) {
		throw new TechnicalException("not implemented, should not be used");
	}

	public List<Signature> assembleList(List<SignatureVo> valueObjectList) {
		throw new TechnicalException("not implemented, should not be used");
	}

	public SignatureVo disassemble(Signature entityObject) {

		SignatureVo res = null;

		if(null!=entityObject){
			res = new SignatureVo();
			res.setIdentifier(entityObject.getUuid());
			res.setCreationDate(entityObject.getCreationDate());
			res.setCertIssuerDn(entityObject.getCertIssuerDn());
			res.setCertSubjectDn(entityObject.getCertSubjectDn());
			res.setCertNotAfter(entityObject.getCertNotAfter());
			res.setCert(entityObject.getCert());
			res.setSize(entityObject.getSize());
			res.setName(entityObject.getName());
			res.setPersistenceId(entityObject.getId());

			UserVo signer = userTransformer.disassemble((User)entityObject.getSigner());
			res.setSigner(signer);	
		}
		return res;
	}

	public List<SignatureVo> disassembleList(List<Signature> entityObjectList) {
		ArrayList<SignatureVo> sigs=new ArrayList<SignatureVo>();
		for(Signature sig :entityObjectList){
			sigs.add(disassemble(sig));
		}
		return sigs;
	}

}