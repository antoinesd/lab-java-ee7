/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.lab.javaee.chat;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * @author Arun Gupta
 */
@ServerEndpoint("/websocket")
public class ChatEndpointImpl implements ChatEndpoint {
    private static final Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());

    @Override
    @OnOpen
    public void onOpen(Session peer) {
        peers.add(peer);
    }

    @Override
    @OnClose
    public void onClose(Session peer) {
        peers.remove(peer);
    }

    @Override
    @OnMessage
    public void message(String message, Session client) throws IOException, EncodeException {
        System.out.println(message);
        JsonReader reader = Json.createReader(new StringReader(message));
        try {
            JsonObject msgObj = reader.readObject();
            Message msg = new Message();
            msg.setUser(msgObj.getString("user"));
            msg.setContent(msgObj.getString("content"));
            System.out.println("Message from " + msg.getUser() + " : " + msg.getContent());
        } catch (JsonParsingException e) {
            System.out.println("Message is not in JSON format");
        } finally {
            reader.close();
        }
        for (Session peer : peers) {
            peer.getAsyncRemote().sendObject(message);
        }
    }
}
