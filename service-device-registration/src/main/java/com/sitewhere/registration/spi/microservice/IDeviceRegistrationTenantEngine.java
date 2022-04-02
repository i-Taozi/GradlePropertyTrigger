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
package com.sitewhere.registration.spi.microservice;

import com.sitewhere.registration.configuration.DeviceRegistrationTenantConfiguration;
import com.sitewhere.registration.spi.IRegistrationManager;
import com.sitewhere.registration.spi.kafka.IRegistrationEventsPipeline;
import com.sitewhere.registration.spi.kafka.IUnregisteredEventsPipeline;
import com.sitewhere.spi.microservice.multitenant.IMicroserviceTenantEngine;

/**
 * Extends {@link IMicroserviceTenantEngine} with features specific to device
 * registration.
 */
public interface IDeviceRegistrationTenantEngine
	extends IMicroserviceTenantEngine<DeviceRegistrationTenantConfiguration> {

    /**
     * Get Kafka Streams pipeline for unregistered device events.
     * 
     * @return
     */
    public IUnregisteredEventsPipeline getUnregisteredEventsPipeline();

    /**
     * Get Kafka Streams pipeline for new device registrations.
     * 
     * @return
     */
    public IRegistrationEventsPipeline getRegistrationEventsPipeline();

    /**
     * Get registration manager implementation.
     * 
     * @return
     */
    public IRegistrationManager getRegistrationManager();
}