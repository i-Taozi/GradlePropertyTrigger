/*
 * Galaxy
 * Copyright (c) 2012-2014, Parallel Universe Software Co. All rights reserved.
 * 
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *  
 *   or (per the licensee's choosing)
 *  
 * under the terms of the GNU Lesser General Public License version 3.0
 * as published by the Free Software Foundation.
 */
package co.paralleluniverse.galaxy.jgroups;

import org.jgroups.Address;

/**
 *
 * @author pron
 */
public interface ExtendedChannel extends Channel {

    void send(Address dst, byte[] buf, int offset, int length) throws Exception;

    void send(Address dst, byte[] buf) throws Exception;

    void send(Address dst, Object obj) throws Exception;

    String getViewAsString();

    String getAddressAsUUID();

    String getAddressAsString();
}
