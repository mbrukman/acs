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

package com.ge.predix.acs.service.policy.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.ge.predix.acs.commons.policy.condition.ConditionScript;
import com.ge.predix.acs.commons.policy.condition.groovy.GroovyConditionCache;
import com.ge.predix.acs.commons.policy.condition.groovy.GroovyConditionShell;
import com.ge.predix.acs.model.ActionArgument;
import com.ge.predix.acs.model.Condition;
import com.ge.predix.acs.model.Obligation;
import com.ge.predix.acs.model.Policy;
import com.ge.predix.acs.model.PolicySet;
import com.ge.predix.acs.utils.JsonUtils;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

/**
 * @author 212406427
 */
@Component
@SuppressWarnings("nls")
public class PolicySetValidatorImpl implements PolicySetValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicySetValidatorImpl.class);

    private static final JsonUtils JSONUTILS = new JsonUtils();
    private static final JsonNode JSONSCHEMA;

    @Autowired
    private GroovyConditionCache conditionCache;

    @Autowired
    private GroovyConditionShell conditionShell;

    static {
        JSONSCHEMA = JSONUTILS.readJsonNodeFromFile("acs-policy-set-schema.json");
    }

    @Value("${validAcsPolicyHttpActions:GET, POST, PUT, DELETE, PATCH, SUBSCRIBE, MESSAGE}")
    private String validAcsPolicyHttpActions;

    private Set<String> validAcsPolicyHttpActionsSet;

    private static final String EXCEP_POLICY_OBL_IDS_NULL_EMPTY = "Policy validation failed. obligationsIds cannot"
            + " contain null or empty values for policy: [%s]";

    private static final String EXCEP_OBL_IDS_REPEATED = "Obligation validation failed. Obligation ids need to unique."
            + " Repeated values:  %s";

    private static final String EXCEP_OBL_ACTION_ARG_NULL_BLANK = "Obligation validation failed. actionArgument [%s]  "
            + "cannot be null or blank on [index: [%d] , actionArgument index: [%d] ]";

    private static final String EXCEP_OBL_ACTION_ARG_NULL = "Obligation validation failed. actionArgument [%s]  "
            + "cannot be null [index: [%d] , actionArgument index: [%d] ]";

    private static final String EXCEP_OBL_ACTION_ARG_DUPLICATED = "Obligation validation failed. actionArguments names "
            + "cannot be repeated on  index : [%d], values: [%s]";

    private static final String EXCEP_OBL_ID_NULL_BLANK = "Obligation validation failed. id cannot be null or blank "
            + "on index: [%s]";

    private static final String EXCEP_OBL_ACTION_TEMPLATE_NULL_BLANK = "Obligation validation failed. actionTemplate"
            + " cannot be null or empty on index: [%s]";

    private static final String EXCEP_POLICY_OBL_IDS_NOT_FOUND = "Policy validation failed. The following "
            + "obligationsIds  does not match with the obligations defined for this policy set. [ policy: [%s]"
            + ", obligationIds: %s ]";

    @Override
    public void removeCachedConditions(final PolicySet policySet) {
        for (Policy policy : policySet.getPolicies()) {
            for (Condition condition : policy.getConditions()) {
                this.conditionCache.remove(condition.getCondition());
            }
        }
    }

    @Override
    public void validatePolicySet(final PolicySet policySet) {
        validateSchema(policySet);
        List<String> obligationIds = validateObligationsAndGetIds(policySet.getObligations());
        for (Policy p : policySet.getPolicies()) {
            validatePolicyConditions(p.getConditions());
            validatePolicyActions(p);
            vaLidatePolicyObligations(obligationIds, p);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> validateObligationsAndGetIds(final List<Obligation> obligations) {
        List<String> ids = new ArrayList<String>();
        List<String> repeatedIds = new ArrayList<String>();
        AtomicInteger index = new AtomicInteger(0);

        for (Obligation obligation : obligations) {
            if (StringUtils.isEmpty(obligation.getId())) {
                throw new PolicySetValidationException(String.format(EXCEP_OBL_ID_NULL_BLANK, index.get()));
            }
            if (ids.contains(obligation.getId())) {
                repeatedIds.add(obligation.getId());
            }

            if (null == obligation.getActionTemplate()
                    || ((Map<Object, Object>) obligation.getActionTemplate()).size() == 0) {
                throw new PolicySetValidationException(
                        String.format(EXCEP_OBL_ACTION_TEMPLATE_NULL_BLANK, index.get()));
            }
            validateActionArguments(index.get(), obligation.getActionArguments());
            ids.add(obligation.getId());
            index.incrementAndGet();
        }
        if (!CollectionUtils.isEmpty(repeatedIds)) {
            throw new PolicySetValidationException(
                    String.format(EXCEP_OBL_IDS_REPEATED, Arrays.toString(repeatedIds.toArray())));
        }
        return ids;
    }

    private void validateActionArguments(final int obligationIndex, final List<ActionArgument> actionArguments) {
        AtomicInteger index = new AtomicInteger(0);
        List<String> iteratedActionArguments = new ArrayList<String>();
        List<String> duplicatedActionArguments = new ArrayList<String>();
        for (ActionArgument actionArgument : actionArguments) {
            if (StringUtils.isEmpty(actionArgument.getName())) {
                throw new PolicySetValidationException(
                        String.format(EXCEP_OBL_ACTION_ARG_NULL_BLANK, "name", obligationIndex, index.get()));
            }
            if (null == actionArgument.getValue()) {
                throw new PolicySetValidationException(
                        String.format(EXCEP_OBL_ACTION_ARG_NULL, "value", obligationIndex, index.get()));
            }
            if (iteratedActionArguments.contains(actionArgument.getName())) {
                duplicatedActionArguments.add(actionArgument.getName());
            }
            iteratedActionArguments.add(actionArgument.getName());
            index.incrementAndGet();
        }
        if (!CollectionUtils.isEmpty(duplicatedActionArguments)) {
            throw new PolicySetValidationException(String.format(EXCEP_OBL_ACTION_ARG_DUPLICATED, obligationIndex,
                    Arrays.toString(duplicatedActionArguments.toArray())));
        }

    }

    private void validatePolicyActions(final Policy p) {

        if (p.getTarget() != null && p.getTarget().getAction() != null) {
            String policyActions = p.getTarget().getAction();
            // Empty actions will be treated as null actions which behave like
            // match any
            // during policy evaluation
            if (policyActions.trim().length() == 0) {
                p.getTarget().setAction(null);
                return;
            }
            for (String action : policyActions.split("\\s*,\\s*")) {
                if (!this.validAcsPolicyHttpActionsSet.contains(action)) {
                    throw new PolicySetValidationException(String.format(
                            "Policy Action validation failed: "
                                    + "the action: [%s] is not contained in the allowed set of actions: [%s]",
                            action, this.validAcsPolicyHttpActions));
                }
            }
        }
    }

    private void vaLidatePolicyObligations(final List<String> obligationIds, final Policy policy) {
        List<String> notFound = new ArrayList<String>();
        for (String obligationId : policy.getObligationIds()) {
            if (StringUtils.isEmpty(obligationId)) {
                throw new PolicySetValidationException(
                        String.format(EXCEP_POLICY_OBL_IDS_NULL_EMPTY, policy.getName()));

            } else if (!obligationIds.contains(obligationId)) {
                notFound.add(obligationId);

            }
        }
        if (!notFound.isEmpty()) {
            throw new PolicySetValidationException(String.format(EXCEP_POLICY_OBL_IDS_NOT_FOUND, policy.getName(),
                    Arrays.toString(notFound.toArray())));
        }

    }

    private void validateSchema(final PolicySet policySet) {
        try {
            JsonNode policySetJsonNode = JSONUTILS.readJsonNodeFromObject(policySet);
            JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            JsonSchema schema = factory.getJsonSchema(JSONSCHEMA);
            ProcessingReport report = schema.validate(policySetJsonNode);
            Iterator<ProcessingMessage> iterator = report.iterator();
            boolean valid = report.isSuccess();
            StringBuilder sb = new StringBuilder();
            if (!valid) {
                while (iterator.hasNext()) {
                    ProcessingMessage processingMessage = iterator.next();
                    sb.append(" ");
                    sb.append(processingMessage);
                    LOGGER.debug("{}", processingMessage);
                }
                throw new PolicySetValidationException("JSON Schema validation " + sb.toString());
            }
        } catch (PolicySetValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new PolicySetValidationException("Error while validating JSON schema", e);
        }

    }

    @Override
    public List<ConditionScript> validatePolicyConditions(final List<Condition> conditions) {
        List<ConditionScript> conditionScripts = new ArrayList<>();
        try {
            if ((conditions == null) || conditions.isEmpty()) {
                return conditionScripts;
            }
            for (Condition condition : conditions) {
                String conditionScript = condition.getCondition();
                ConditionScript compiledScript = this.conditionCache.get(conditionScript);
                if (compiledScript != null) {
                    conditionScripts.add(compiledScript);
                    continue;
                }

                try {
                    LOGGER.debug("Adding condition: {}", conditionScript);
                    compiledScript = this.conditionShell.parse(conditionScript);
                    conditionScripts.add(compiledScript);
                    this.conditionCache.put(conditionScript, compiledScript);
                } catch (Exception e) {
                    throw new PolicySetValidationException(
                            "Condition : [" + conditionScript + "] validation failed with error : " + e.getMessage(),
                            e);
                }
            }

            return conditionScripts;

        } catch (PolicySetValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new PolicySetValidationException("Unexpected exception while validating policy conditions", e);
        }
    }

    /**
     * Initialization method to populate the allowedPolicyHttpActions.
     */
    @PostConstruct
    public void init() {
        String[] actions = this.validAcsPolicyHttpActions.split("\\s*,\\s*");
        LOGGER.debug("ACS Server configured with validAcsPolicyHttpActions : {}", this.validAcsPolicyHttpActions);
        this.validAcsPolicyHttpActionsSet = new HashSet<>(Arrays.asList(actions));
    }

    public void setValidAcsPolicyHttpActions(final String validAcsPolicyHttpActions) {
        this.validAcsPolicyHttpActions = validAcsPolicyHttpActions;
    }
}
