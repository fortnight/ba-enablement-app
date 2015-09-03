package com.redhat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

public class DroolsTest {

	private StatelessDecisionService service = BrmsHelper.newStatelessDecisionServiceBuilder().auditLogName("audit2").build();

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
		business.setName("FedEx");
		business.setAddressLine1("400 FedEx Street");
		business.setAddressLine2("Penthouse Suite");
		business.setPhoneNumber("1800cars4kids");
		business.setCity("Kansas City");
		business.setStateCode("KS");
		business.setZipCode("90210");
		business.setFederalTaxId("Something");
		facts.add(business);
		
		// when I apply the filtering rules
		RuleResponse response = service.runRules(facts, "VerifySupplier", RuleResponse.class);
		
		// then the business should be filtered
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getResponseCode());
		Assert.assertEquals("filtered", response.getResponseCode());
		
		// and the reason message should be "business filtered from Kansas"
		boolean found = false;
		for (Reason reason : response.getReasons()){
			if ( reason.getReasonMessage().equals( "business filtered from Kansas not found") ){
				found = true;
			}
		}
		Assert.assertTrue( "business filtered from Kansas not found", found );
	}
		
	@Test
	public void shouldProcessAllBusinessesNotFromKansas(){
		// scenario: we are responsible for all businesses not from Kansas
		// given a business from New York
		Collection<Object> facts = new ArrayList<Object>();
		Business business = new Business();
		business.setStateCode("NY");
		facts.add(business);
		
		// when I apply the filtering rules
		RuleResponse response = service.runRules(facts, "VerifySupplier", RuleResponse.class);
		
		// then the business should be not be filtered
		Assert.assertNotNull(response);
		Assert.assertNotEquals("filtered", response.getResponseCode());
		
		// and the validation rules should be applied to the business
		boolean found = false;
		for (Reason reason : response.getReasons()){
			if ( reason.getReasonMessage().equals( "non-Kansas business validated") ){
				found = true;
			}
		}
		Assert.assertTrue( "non-Kansas business validated", found );
	}


	@Test
	public void shouldCreateValidationErrorsForAnyFieldThatAreEmptyOrNull(){
		// scenario: all fields must have values. 
		// given a business 
		// and the business' zipcode is empty
		// and the business' address line 1 is null
		Collection<Object> facts = new ArrayList<Object>();
		Business business = new Business();
		facts.add(business);
		
		// when I apply the validation rules
		RuleResponse response = service.runRules(facts, "VerifySupplier", RuleResponse.class);
		
		// then the business should return a validation error
		Assert.assertNotNull(response);
		Assert.assertEquals("error", response.getResponseCode());
		
		// and a message should say the zipcode is empty
		// and a message should say the address is null
		Assert.assertFalse(response.getReasons().isEmpty());
		boolean found = false;
		for (Reason reason : response.getReasons()){
			if ( reason.getReasonMessage().equals( "name field is empty") ){
				found = true;
				Assert.assertTrue( "name field is empty", found );
				found = false;
			}
			if ( reason.getReasonMessage().equals( "addressline1 field is empty") ){
				found = true;
				Assert.assertTrue( "addressline1 field is empty", found );
				found = false;
			}
			if ( reason.getReasonMessage().equals( "addressline2 field is empty") ){
				found = true;
				Assert.assertTrue( "addressline2 field is empty", found );
				found = false;
			}
			if ( reason.getReasonMessage().equals( "phoneNumber field is empty") ){
				found = true;
				Assert.assertTrue( "phone field is empty", found );
				found = false;
			}
			if ( reason.getReasonMessage().equals( "city field is empty") ){
				found = true;
				Assert.assertTrue( "city field is empty", found );
				found = false;
			}
			if ( reason.getReasonMessage().equals( "stateCode field is empty") ){
				found = true;
				Assert.assertTrue( "stateCode field is empty", found );
				found = false;
			}
			if ( reason.getReasonMessage().equals( "zipCode field is empty") ){
				found = true;
				Assert.assertTrue( "zipCode field is empty", found );
				found = false;
			}
			if ( reason.getReasonMessage().equals( "federalTaxId field is empty") ){
				found = true;
				Assert.assertTrue( "federalTaxId field is empty", found );
			}
		}
	}
	
	@Test
	public void shouldEnrichTheTaxIdWithZipCode(){
		// scenario: we need to enrich the taxId with the zipcode for system XYZ
		// given a business
		// and the business' zipcode is 10002
		// and the business' taxId is 98765
		Collection<Object> facts = new ArrayList<Object>();
		Business business = new Business();
		business.setZipCode("10002");
		business.setFederalTaxId("98765");
		facts.add(business);
		
		// when I apply the enrichment rules
		RuleResponse response = service.runRules(facts, "VerifySupplier", RuleResponse.class);
		
		
		// then the business' taxId should be enriched to 98765-10002
		Assert.assertEquals("98765-10002", response.getBusiness().getFederalTaxId());
	}
	
}
