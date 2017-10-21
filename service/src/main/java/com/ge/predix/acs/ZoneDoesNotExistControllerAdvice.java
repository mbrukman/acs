/*******************************************************************************
 * Copyright 2017 General Electric Company
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
 *******************************************************************************/

package com.ge.predix.acs;

import org.json.simple.JSONObject;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import com.ge.predix.acs.privilege.management.PrivilegeManagementUtility;
import com.ge.predix.acs.privilege.management.ZoneDoesNotExistException;

// A ZoneDoesNotExistException exception thrown from any @RestController should be handled by this @ControllerAdvice.
// Note that /v1/zone* APIs don't throw ZoneDoesNotExistExceptions anywhere in their call hierarchy so we don't need
// to explicitly exclude them here.
@ControllerAdvice(annotations = RestController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ZoneDoesNotExistControllerAdvice {
    @ExceptionHandler(ZoneDoesNotExistException.class)
    public ResponseEntity<JSONObject> handleZoneDoesNotExist() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .body(new JSONObject() {{
                    put(PrivilegeManagementUtility.INCORRECT_PARAMETER_TYPE_ERROR,
                            HttpStatus.BAD_REQUEST.getReasonPhrase());
                    put(PrivilegeManagementUtility.INCORRECT_PARAMETER_TYPE_MESSAGE,
                            "Zone not found");
                }});
    }
}
