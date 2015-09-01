package com.redhat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

public class DroolsTest {

	private StatelessDecisionService service = BrmsHelper.newStatelessDecisionServiceBuilder().auditLogName("audit").build();

	@Test
	public void helloWorldTest() {
		// given
		Collection<Object> facts = new ArrayList<Object>();
		Business business = new Business();
		business.setName("test");
		facts.add(business);

		// when
		RuleResponse response = service.runRules(facts, "VerifySupplier", RuleResponse.class);

		// then
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBusiness());
		Assert.assertEquals("test", response.getBusiness().getName());
	}
	
	@Test
	public void shouldFilterOutAllRequestsFromKansas(){
		// scenario: business from Kansas are handled by another system - filter them out
		// given a business from Kansas
		Collection<Object> facts = new ArrayList<Object>();
		Business business = new Business();
		business.setStateCode("KS");
		facts.add(business);
		Reason reason = new Reason();
		Collection<Reason> reasons = new ArrayList<Reason>();
		reasons.add(reason);
		
		// when I apply the filtering rules
		RuleResponse response = service.runRules(facts,  "VerifySupplier",  RuleResponse.class);
		response.setReasons(reasons);
		
		// then the business should be filtered
		// and the reason message should be "business filtered from Kansas"
		Assert.assertEquals("business filtered from Kansas", ((Reason) response.getReasons().toArray()[0]).getReasonMessage());
	}
	
	
	public void shouldProcessAllBusinessesNotFromKansas(){
		// scenario: we are responsible for all businesses not from Kansas
		// given a business from New York
		// when I apply the filtering rules
		// then the business should be not be filtered
		// and the validation rules should be applied to the business
	}
	
	public void shouldCreateValidationErrorsForAnyFieldThatAreEmptyOrNull(){
		// scenario: all fields must have values. 
		// given a business 
		// and the business' zipcode is empty
		// and the business' address line 1 is null
		// when I apply the validation rules
		// then the business should be return a validation error
		// and a message should say the zipcode is empty
		// and a message should say the address is null
	}
	
	public void shouldEnrichTheTaxIdWithZipCode(){
		// scenario: we need to enrich the taxId with the zipcode for system XYZ
		// given a business 
		// and the business' zipcode is 10002
		// and the business' taxId is 98765
		// when I apply the enrichment rules
		// then the business' taxId should be enriched to 98765-10002
	}
	
}
