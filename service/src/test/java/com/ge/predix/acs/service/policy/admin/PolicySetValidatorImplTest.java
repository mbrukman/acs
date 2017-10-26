package com.ge.predix.acs.service.policy.admin;

import org.testng.annotations.Test;
import com.ge.predix.acs.model.PolicySet;
import com.ge.predix.acs.service.policy.validation.PolicySetValidationException;
import com.ge.predix.acs.service.policy.validation.PolicySetValidatorImpl;
import com.ge.predix.acs.utils.JsonUtils;

public class PolicySetValidatorImplTest {

    private final PolicySetValidatorImpl validator = new PolicySetValidatorImpl();
    private final JsonUtils jsonUtils = new JsonUtils();

    @Test(enabled = false)
    public void testValidatePolicyOkObligations() {
        PolicySet policySet = this.jsonUtils.deserializeFromFile("obligation/set-with-1-policy.json", PolicySet.class);
        this.validator.validatePolicySet(policySet);
    }

    @Test(
            enabled = false,
            expectedExceptions = { PolicySetValidationException.class },
            expectedExceptionsMessageRegExp = ".*Obligation validation failed\\. id cannot be null or blank on index.*")
    public void testValidatePolicyBadObligationIdNull() {
        PolicySet policySet = this.jsonUtils
                .deserializeFromFile("obligation/set-with-1-policy-bad-obligations-id-null.json", PolicySet.class);
        this.validator.validatePolicySet(policySet);
    }

    @Test(
            enabled = false,
            expectedExceptions = { PolicySetValidationException.class },
            expectedExceptionsMessageRegExp = ".*Obligation validation failed\\. id cannot be null or blank on index.*")
    public void testValidatePolicyBadObligationIdBlank() {
        PolicySet policySet = this.jsonUtils
                .deserializeFromFile("obligation/set-with-1-policy-bad-obligations-id-blank.json", PolicySet.class);
        this.validator.validatePolicySet(policySet);
    }

    @Test(
            enabled = false,
            expectedExceptions = { PolicySetValidationException.class },
            expectedExceptionsMessageRegExp = ".*Obligation validation failed\\. actionTemplate"
                    + " cannot be null or empty on index.*")
    public void testValidatePolicyBadObligationActionTemplateBlank() {
        PolicySet policySet = this.jsonUtils
                .deserializeFromFile("obligation/set-with-1-policy-bad-action-template-blank.json", PolicySet.class);
        this.validator.validatePolicySet(policySet);
    }

    @Test(
            enabled = false,
            expectedExceptions = { PolicySetValidationException.class },
            expectedExceptionsMessageRegExp = ".*Obligation validation failed\\. actionTemplate"
                    + " cannot be null or empty on index.*")
    public void testValidatePolicyBadObligationActionTemplateNull() {
        PolicySet policySet = this.jsonUtils
                .deserializeFromFile("obligation/set-with-1-policy-bad-action-template-null.json", PolicySet.class);
        this.validator.validatePolicySet(policySet);
    }

    @Test(
            enabled = false,
            expectedExceptions = { PolicySetValidationException.class },
            expectedExceptionsMessageRegExp = ".*Obligation validation failed\\. actionArgument.*cannot be null "
                    + "or blank on .*")
    public void testValidatePolicyBadObligationActionArgumentNameBlank() {
        PolicySet policySet = this.jsonUtils.deserializeFromFile(
                "obligation/set-with-1-policy-bad-obligation-action-argument-name-blank.json", PolicySet.class);
        this.validator.validatePolicySet(policySet);

    }

    @Test(
            enabled = false,
            expectedExceptions = { PolicySetValidationException.class },
            expectedExceptionsMessageRegExp = ".*Obligation validation failed\\. actionArgument.*cannot be null .*")

    public void testValidatePolicyBadObligationActionArgumentNameNull() {
        PolicySet policySet = this.jsonUtils.deserializeFromFile(
                "obligation/set-with-1-policy-bad-obligation-action-argument-name-null.json", PolicySet.class);
        this.validator.validatePolicySet(policySet);
    }

    @Test(
            enabled = false,
            expectedExceptions = { PolicySetValidationException.class },
            expectedExceptionsMessageRegExp = ".*Obligation validation failed\\. actionArguments names cannot"
                    + " be repeated on  index.*")
    public void testValidatePolicyBadObligationActionArgumentDuplicatedNames() {
        PolicySet policySet = this.jsonUtils.deserializeFromFile(
                "obligation/set-with-1-policy-bad-obligation-action-argument-name-duplicated.json", PolicySet.class);
        this.validator.validatePolicySet(policySet);
    }

    @Test(
            enabled = false,
            expectedExceptions = { PolicySetValidationException.class },
            expectedExceptionsMessageRegExp = ".*Policy validation failed\\. The following obligationsIds  "
                    + "does not match with the obligations defined for this policy set.*")
    public void testValidatePolicyObligationIdsNotFound() {
        PolicySet policySet = this.jsonUtils.deserializeFromFile(
                "obligation/set-with-1-policy-bad-obligations-ids-not-found.json", PolicySet.class);
        this.validator.validatePolicySet(policySet);
    }

    @Test(
            enabled = false,
            expectedExceptions = { PolicySetValidationException.class },
            expectedExceptionsMessageRegExp = ".*Obligation validation failed\\. Obligation ids need to unique\\."
                    + " Repeated values.*")
    public void testValidateObligationIdsNotUnique() {
        PolicySet policySet = this.jsonUtils
                .deserializeFromFile("obligation/set-with-1-policy-bad-obligation-ids-repeated.json", PolicySet.class);
        this.validator.validatePolicySet(policySet);
    }

}
