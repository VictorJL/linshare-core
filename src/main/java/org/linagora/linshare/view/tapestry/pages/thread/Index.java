/*
 *    This file is part of Linshare.
 *
 *   Linshare is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as
 *   published by the Free Software Foundation, either version 3 of
 *   the License, or (at your option) any later version.
 *
 *   Linshare is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public
 *   License along with Foobar.  If not, see
 *                                    <http://www.gnu.org/licenses/>.
 *
 *   (c) 2008 Groupe Linagora - http://linagora.org
 *
 */
package org.linagora.linshare.view.tapestry.pages.thread;

import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.linagora.linshare.core.domain.vo.UserVo;
import org.linagora.linshare.view.tapestry.beans.ShareSessionObjects;
import org.linagora.linshare.view.tapestry.components.FileUploader;
import org.linagora.linshare.view.tapestry.components.WindowWithEffects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Index {

    public final static Logger Logger = LoggerFactory.getLogger(Index.class);

    @SessionState
    @Property
    private ShareSessionObjects shareSessionObjects;

    @SessionState
    @Property
    private UserVo userVo;

    @InjectComponent
    private FileUploader fileUploader;


    /* ***********************************************************
     *                      Injected services
     ************************************************************ */

    @Inject
    private Messages messages;



    @SetupRender
    public void setupRender() {
        ;
    }

    @AfterRender
    public void afterRender() {
        ;
    }

    Object onException(Throwable cause) {
        shareSessionObjects.addError(messages.get("global.exception.message"));
        Logger.error(cause.getMessage());
        cause.printStackTrace();
        return this;
    }
}
