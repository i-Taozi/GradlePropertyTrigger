/**
 * Copyright © 2014-2021 The SiteWhere Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sitewhere.connectors.spi.common;

import com.sitewhere.connectors.spi.IOutboundConnector;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.event.IDeviceEvent;
import com.sitewhere.spi.device.event.IDeviceEventContext;
import com.sitewhere.spi.microservice.lifecycle.ITenantEngineLifecycleComponent;

/**
 * Interface for a component that builds a URL based on a device event.
 */
public interface IUriBuilder extends ITenantEngineLifecycleComponent {

    /**
     * Build a URI based on device event information.
     * 
     * @param connector
     * @param context
     * @param event
     * @return
     * @throws SiteWhereException
     */
    public String buildUri(IOutboundConnector connector, IDeviceEventContext context, IDeviceEvent event)
	    throws SiteWhereException;
}
