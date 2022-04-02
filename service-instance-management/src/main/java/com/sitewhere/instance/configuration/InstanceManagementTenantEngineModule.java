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
package com.sitewhere.instance.configuration;

import com.sitewhere.instance.spi.microservice.IInstanceManagementTenantEngine;
import com.sitewhere.microservice.multitenant.TenantEngineModule;

/**
 * Guice module used for configuring components associated with an instance
 * management tenant engine.
 */
public class InstanceManagementTenantEngineModule extends TenantEngineModule<InstanceManagementTenantConfiguration> {

    public InstanceManagementTenantEngineModule(IInstanceManagementTenantEngine tenantEngine,
	    InstanceManagementTenantConfiguration configuration) {
	super(tenantEngine, configuration);
    }

    /*
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
	bind(IInstanceManagementTenantEngine.class).toInstance((IInstanceManagementTenantEngine) getTenantEngine());
    }
}
