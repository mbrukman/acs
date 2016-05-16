/*******************************************************************************
 * Copyright 2016 General Electric Company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.ge.predix.acs.privilege.management.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.ge.predix.acs.zone.management.dao.ZoneEntity;

/**
 *
 * @author 212360328
 */
@Entity
@Table(
        name = "resource",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "authorization_zone_id", "resource_identifier" }) })
public class ResourceEntity implements ZonableEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "authorization_zone_id", referencedColumnName = "id", nullable = false, updatable = false)
    private ZoneEntity zone;

    @Column(name = "resource_identifier", nullable = false)
    private String resourceIdentifier;

    /**
     * Clob representing set of attributes as a JSON body.
     */
    @Column(name = "attributes", columnDefinition = "CLOB NOT NULL")
    private String attributesAsJson;

    /**
     * Note about all these Id's and identifiers:
     *
     * id: surrogate id generated by the rbdms, intended to FK references, etc. resourceIdentifier: The actual
     * resource URI being protected, Ex: /asset/sanramon
     */
    public ResourceEntity(final ZoneEntity zone, final String resourceIdentifier) {
        this.zone = zone;
        this.resourceIdentifier = resourceIdentifier;
    }

    public ResourceEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public ZoneEntity getZone() {
        return this.zone;
    }

    public void setZone(final ZoneEntity zone) {
        this.zone = zone;
    }

    public String getResourceIdentifier() {
        return this.resourceIdentifier;
    }

    public void setResourceIdentifier(final String resourceIdentifier) {
        this.resourceIdentifier = resourceIdentifier;
    }

    public String getAttributesAsJson() {
        return this.attributesAsJson;
    }

    public void setAttributesAsJson(final String attributesAsJson) {
        this.attributesAsJson = attributesAsJson;
    }

    @Override
    public String toString() {
        return "ResourceEntity [id=" + this.id + ", zone=" + this.zone + ", resourceIdentifier="
                + this.resourceIdentifier + ", attributesAsJson=" + this.attributesAsJson + "]";
    }

}
