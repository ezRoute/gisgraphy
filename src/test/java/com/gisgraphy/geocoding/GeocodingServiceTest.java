/*******************************************************************************
 * Gisgraphy Project 
 *  
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *  
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Lesser General Public License for more details.
 *  
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA
 *  
 *   Copyright 2008  Gisgraphy project 
 * 
 *   David Masclet <davidmasclet@gisgraphy.com>
 ******************************************************************************/
package com.gisgraphy.geocoding;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.gisgraphy.addressparser.Address;
import com.gisgraphy.addressparser.AddressQuery;
import com.gisgraphy.addressparser.AddressResultsDto;
import com.gisgraphy.addressparser.IAddressParserService;
import com.gisgraphy.addressparser.StructuredAddressQuery;
import com.gisgraphy.addressparser.commons.GeocodingLevels;
import com.gisgraphy.addressparser.exception.AddressParserException;
import com.gisgraphy.addressparser.format.BasicAddressFormater;
import com.gisgraphy.domain.valueobject.GisgraphyConfig;
import com.gisgraphy.domain.valueobject.Pagination;
import com.gisgraphy.fulltext.FullTextSearchEngine;
import com.gisgraphy.fulltext.FulltextQuery;
import com.gisgraphy.fulltext.FulltextResultsDto;
import com.gisgraphy.fulltext.SolrResponseDto;
import com.gisgraphy.helper.CountryInfo;
import com.gisgraphy.helper.GeolocHelper;
import com.gisgraphy.importer.ImporterConfig;
import com.gisgraphy.importer.LabelGenerator;
import com.gisgraphy.serializer.common.OutputFormat;
import com.gisgraphy.service.IStatsUsageService;
import com.gisgraphy.stats.StatsUsageType;
import com.gisgraphy.street.HouseNumberDto;
import com.gisgraphy.test.GisgraphyTestHelper;
import com.vividsolutions.jts.geom.Point;

public class GeocodingServiceTest {

    IStatsUsageService statsUsageService;
    GisgraphyConfig gisgraphyConfig;
    public boolean geocodeIsCalled = false;

    public boolean findCitiesCalled = false;
    public boolean findStreetCalled = false;
    public boolean GeocodeAdressCalled = false;
    public boolean populatecalled = false;
    
    private LabelGenerator labelGenerator = LabelGenerator.getInstance();
	private BasicAddressFormater addressFormater = BasicAddressFormater.getInstance();

    @Before
    public void beforeTest() {
	statsUsageService = EasyMock.createMock(IStatsUsageService.class);
	gisgraphyConfig = new GisgraphyConfig();
    }
    
    
    /*
                                    _     _                   
	__ __ __ ___      __   __ _  __| | __| |_ __ ___  ___ ___ 
	| '__/ _` \ \ /\ / /  / _` |/ _` |/ _` | '__/ _ \/ __/ __|
	| | | (_| |\ V  V /  | (_| | (_| | (_| | | |  __/\__ \__ \
	|_|  \__,_| \_/\_/    \__,_|\__,_|\__,_|_|  \___||___/___/
	                                                          
     */

    @Test(expected = IllegalArgumentException.class)
    public void geocodeRawAdressShouldThrowIfAddressIsNull() {
	IGeocodingService geocodingService = new GeocodingService();
	String rawAddress = null;
	AddressQuery query = new AddressQuery(rawAddress, "US");
	geocodingService.geocode(query);
    }

    @Test(expected = IllegalArgumentException.class)
    public void geocodeRawAdressShouldThrowIfAddressIsEmpty() {
	IGeocodingService geocodingService = new GeocodingService();
	String rawAddress = " ";
	AddressQuery query = new AddressQuery(rawAddress, "US");
	geocodingService.geocode(query);
    }

    @Test(expected = GeocodingException.class)
    public void geocodeRawAdressShouldThrowIfCountryCodehasOnlyOneLetter() {
	IGeocodingService geocodingService = new GeocodingService();
	String rawAddress = "t";
	AddressQuery query = new AddressQuery(rawAddress, "d");
	geocodingService.geocode(query);
    }
    
    @Test(expected = GeocodingException.class)
    public void geocodeRawAdressShouldThrowIfCountryCodehasThreeLetters() {
	IGeocodingService geocodingService = new GeocodingService();
	String rawAddress = "t";
	AddressQuery query = new AddressQuery(rawAddress, "ddd");
	geocodingService.geocode(query);
    }
    
    @Test
    public void geocodeRawAdressShouldNotThrowIfCountryCodeisEmpty() {
    	GeocodingService geocodingService = new GeocodingService(){
    	    @Override
    	    protected List<SolrResponseDto> findStreetInText(String text, String countryCode, Point point, boolean fuzzy, Double radius) {
    	        return new ArrayList<SolrResponseDto>();
    	    }
    	   @Override
    	protected List<SolrResponseDto> findExactMatches(String text,
    			String countryCode,boolean fuzzy, Point point, Double radius, Class[] placetypes) {
    		   return new ArrayList<SolrResponseDto>();
    	}
    	};
    	ImporterConfig config  = new ImporterConfig();
    	config.setOpenStreetMapFillIsIn(true);
    	geocodingService.setImporterConfig(config);
    	geocodingService.setGisgraphyConfig(gisgraphyConfig);
    	geocodingService.setStatsUsageService(statsUsageService);
    	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
    	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andStubThrow(new AddressParserException());
    	geocodingService.setStatsUsageService(statsUsageService);
    	EasyMock.replay(mockAddressParserService);
    	geocodingService.setAddressParser(mockAddressParserService);
    	String rawAddress = "t";
	AddressQuery query = new AddressQuery(rawAddress, " ");
	geocodingService.geocode(query);
    }
    
    @Test
    public void geocodeRawAdressShouldNotThrowIfCountryCodeisNull() {
    	GeocodingService geocodingService = new GeocodingService(){
    	    @Override
    	    protected List<SolrResponseDto> findStreetInText(String text, String countryCode, Point point, boolean fuzzy, Double radius) {
    	        return new ArrayList<SolrResponseDto>();
    	    }
    	   @Override
    	protected List<SolrResponseDto> findExactMatches(String text,
    			String countryCode,boolean fuzzy, Point point, Double radius, Class[] placetypes) {
    		   return new ArrayList<SolrResponseDto>();
    	}
    	};
    	ImporterConfig config  = new ImporterConfig();
    	config.setOpenStreetMapFillIsIn(true);
    	geocodingService.setImporterConfig(config);
    	geocodingService.setGisgraphyConfig(gisgraphyConfig);
    	geocodingService.setStatsUsageService(statsUsageService);
    	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
    	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andStubThrow(new AddressParserException());
    	geocodingService.setStatsUsageService(statsUsageService);
    	EasyMock.replay(mockAddressParserService);
    	geocodingService.setAddressParser(mockAddressParserService);
    	String rawAddress = "t";
    	AddressQuery query = new AddressQuery(rawAddress, null);
    	geocodingService.geocode(query);
    }

    @Test(expected = GeocodingException.class)
    public void geocodeRawAdressShouldThrowIfCountryCodeHasenTALengthOf2() {
	GeocodingService geocodingService = new GeocodingService();
	geocodingService.setStatsUsageService(statsUsageService);
	String rawAddress = "t";
	AddressQuery query = new AddressQuery(rawAddress, "abc");
	geocodingService.geocode(query);
    }

    @Test
    public void geocodeRawAdressShouldNotThrowGeocodingExceptionWhenAddressParserExceptionOccurs() {
	GeocodingService geocodingService = new GeocodingService(){
	    @Override
	    protected List<SolrResponseDto> findStreetInText(String text, String countryCode, Point point, boolean fuzzy, Double radius) {
	        return new ArrayList<SolrResponseDto>();
	    }
	   @Override
	protected List<SolrResponseDto> findExactMatches(String text,
			String countryCode,boolean fuzzy, Point point, Double radius, Class[] placetypes) {
		   return new ArrayList<SolrResponseDto>();
	}
	};
	ImporterConfig config  = new ImporterConfig();
	config.setOpenStreetMapFillIsIn(true);
	geocodingService.setImporterConfig(config);
	geocodingService.setGisgraphyConfig(gisgraphyConfig);
	geocodingService.setStatsUsageService(statsUsageService);
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andStubThrow(new AddressParserException());
	geocodingService.setStatsUsageService(statsUsageService);
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	String rawAddress = "t";
	AddressQuery query = new AddressQuery(rawAddress, "ac");
	try {
	    geocodingService.geocode(query);
	} catch (GeocodingException e) {
	   fail("geocoder should be tolerant to addressparser errors");
	}
    }
    
    @Test
    public void geocodeRawAddressToOutputStreamShouldHaveGeocodingLevelSet(){
    	
    	
    }

    @Test
    public void geocodeRawAdressShouldCallGeocodeAddressIfParsedAddressIsSuccess() {
	GeocodeAdressCalled = false;
	GeocodingService geocodingService = new GeocodingService() {
	    @Override
	    public AddressResultsDto geocode(Address address, String countryCode) throws GeocodingException {
		GeocodeAdressCalled = true;
		return new AddressResultsDto();
	    }
	};
	ImporterConfig importerConfig = EasyMock.createMock(ImporterConfig.class);
	EasyMock.expect(importerConfig.isOpenStreetMapFillIsIn()).andStubReturn(true);
	geocodingService.setImporterConfig(importerConfig);
	geocodingService.setStatsUsageService(statsUsageService);
	geocodingService.setGisgraphyConfig(gisgraphyConfig);
	gisgraphyConfig.setUseAddressParserWhenGeocoding(true);
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	List<Address> addressList = new ArrayList<Address>() {
	    {
	    	Address address = new Address();
			address.setStreetName("streetName");
			address.setCity("city");
			add(address);
	    }
	};
	AddressResultsDto addressresults = new AddressResultsDto(addressList, 3L);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andReturn(addressresults);
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	String rawAddress = "truc foo";
	AddressQuery query = new AddressQuery(rawAddress, "ac");
	query.setPostal(true);
	AddressResultsDto addressResultsDto = geocodingService.geocode(query);
//	Assert.assertEquals("Parsed address should be set when the address paresed is not null",addressList.get(0), addressResultsDto.getParsedAddress());
	Assert.assertTrue(GeocodeAdressCalled);
    }
    
    
    @Test
    public void geocodeRawAdressShouldNotCallGeocodeAddressIfParsedAddressIsNotParsable() {
    	findCitiesCalled = false;
    	findStreetCalled = false;
    	ImporterConfig importerConfig = new ImporterConfig();
    	importerConfig.setOpenStreetMapFillIsIn(false);
    	GeocodingService geocodingService = new GeocodingService() {

    	    @Override
    	    protected List<SolrResponseDto> findExactMatches(String text,
    				String countryCode,boolean fuzzy, Point point, Double radius, Class[] placetypes) {
    	    	findCitiesCalled = true;
    			   return new ArrayList<SolrResponseDto>();
    		}

    	    @Override
    	    protected java.util.List<SolrResponseDto> findStreetInText(String text, String countryCode, Point point, boolean fuzzy, Double radius) {
    		findStreetCalled = true;
    		return null;
    	    };
    	};
    	geocodingService.setStatsUsageService(statsUsageService);
    	geocodingService.setImporterConfig(importerConfig);
	geocodingService.setStatsUsageService(statsUsageService);
	geocodingService.setGisgraphyConfig(gisgraphyConfig);
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	List<Address> addressList = new ArrayList<Address>() {
	    {
		Address notGeocodableAddress = new Address();
		add(notGeocodableAddress);
	    }
	};
	AddressResultsDto addressresults = new AddressResultsDto(addressList, 3L);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andReturn(addressresults);
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	String rawAddress = "truc foo";
	AddressQuery query = new AddressQuery(rawAddress, "ac");
	query.setPostal(true);
	geocodingService.geocode(query);
	Assert.assertTrue(findCitiesCalled);
	Assert.assertTrue(findStreetCalled);
    }

    @Test
    public void geocodeRawAddressShouldCallFindCityInTextIfParsedAddressIsNullAndIsInIsFalse() {
	findCitiesCalled = false;
	findStreetCalled = false;
	ImporterConfig importerConfig = new ImporterConfig();
	importerConfig.setOpenStreetMapFillIsIn(false);
	GeocodingService geocodingService = new GeocodingService() {

		 @Override
 	    protected List<SolrResponseDto> findExactMatches(String text,
 				String countryCode,boolean fuzzy, Point point, Double radius, Class[] placetypes) {
 	    	findCitiesCalled = true;
 			   return new ArrayList<SolrResponseDto>();
 		}

	    @Override
	    protected java.util.List<SolrResponseDto> findStreetInText(String text, String countryCode, Point point, boolean fuzzy, Double radius) {
		findStreetCalled = true;
		return null;
	    };
	};
	geocodingService.setStatsUsageService(statsUsageService);
	geocodingService.setImporterConfig(importerConfig);
	geocodingService.setGisgraphyConfig(gisgraphyConfig);
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andStubReturn(null);
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	String rawAddress = "t";
	AddressQuery query = new AddressQuery(rawAddress, "ac");
	geocodingService.geocode(query);
	Assert.assertTrue(findCitiesCalled);
	Assert.assertTrue(findStreetCalled);
    }

    @Test
    public void geocodeRawAdressShouldCallFindCityInTextIfParsedAddressIsNullThenFindStreetInTextIfCityFoundAndIsInIsFalse() {
	findCitiesCalled = false;
	findStreetCalled = false;
	ImporterConfig importerConfig = new ImporterConfig();
	importerConfig.setOpenStreetMapFillIsIn(false);
	final SolrResponseDto cityResult = EasyMock.createMock(SolrResponseDto.class);
	final Double latitude = 2.1d;
	EasyMock.expect(cityResult.getLat()).andStubReturn(latitude);
	EasyMock.expect(cityResult.getLat_admin_centre()).andStubReturn(latitude+2);
	final Double longitude = 5.2d;
	EasyMock.expect(cityResult.getLng()).andStubReturn(longitude);
	EasyMock.expect(cityResult.getLng_admin_centre()).andStubReturn(longitude+2);
	EasyMock.expect(cityResult.getName()).andStubReturn("paris");
	EasyMock.expect(cityResult.getAdm2_name()).andStubReturn("ile de france");
	EasyMock.expect(cityResult.getAdm1_name()).andStubReturn("paris region");
	EasyMock.expect(cityResult.getAdm3_name()).andStubReturn("adm3 name");
	EasyMock.expect(cityResult.getAdm4_name()).andStubReturn("adm4 name");
	EasyMock.expect(cityResult.getAdm5_name()).andStubReturn("adm5 name");
	EasyMock.expect(cityResult.getFully_qualified_name()).andStubReturn("FQDN");
	EasyMock.expect(cityResult.getScore()).andStubReturn(42.2F);
	List<String> alternateNames= new ArrayList<String>();
	alternateNames.add("paris alternate");
	EasyMock.expect(cityResult.getName_alternates()).andStubReturn(alternateNames);
	EasyMock.expect(cityResult.getZipcodes()).andStubReturn(null);
	EasyMock.expect(cityResult.getCountry_code()).andStubReturn("FR");
	EasyMock.expect(cityResult.getIs_in_zip()).andStubReturn(null);
	EasyMock.expect(cityResult.getIs_in()).andStubReturn("is_in");
	EasyMock.expect(cityResult.getFeature_id()).andStubReturn(123L);
	EasyMock.expect(cityResult.getStreet_type()).andStubReturn(null);
	EasyMock.expect(cityResult.getPlacetype()).andStubReturn("City");
	EasyMock.expect(cityResult.getOpenstreetmap_id()).andStubReturn(8888L);
	Set<String> zips = new HashSet<String>();
	zips.add("zip");
	EasyMock.expect(cityResult.getZipcodes()).andStubReturn(zips);
	EasyMock.expect(cityResult.getIs_in_adm()).andStubReturn("isinAdm");
	EasyMock.expect(cityResult.getIs_in_place()).andStubReturn("isinPlace");
	EasyMock.replay(cityResult);
	GeocodingService geocodingService = new GeocodingService() {

	    
		 @Override
 	    protected List<SolrResponseDto> findExactMatches(String text,
 				String countryCode,boolean fuzzy, Point point, Double radius, Class[] placetypes) {
 	    	findCitiesCalled = true;
 			List<SolrResponseDto> cities = new ArrayList<SolrResponseDto>();
 			cities.add(cityResult);
 			return cities;
 		}

	    @Override
	    protected java.util.List<SolrResponseDto> findStreetInText(String text, String countryCode, Point point, boolean fuzzy, Double radius) {
		findStreetCalled = true;
		return null;
	    };
	};
	geocodingService.setStatsUsageService(statsUsageService);
	geocodingService.setImporterConfig(importerConfig);
	geocodingService.setGisgraphyConfig(gisgraphyConfig);
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andStubReturn(null);
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	String rawAddress = "paris champs ellysees";
	AddressQuery query = new AddressQuery(rawAddress, "ac");
	geocodingService.geocode(query);
	Assert.assertTrue(findCitiesCalled);
	Assert.assertTrue(findStreetCalled);
    }

    @Test
    public void geocodeRawAdressShouldCallFindCityInTextIfParsedAddressIsNullThenFindStreetInTextIfCityFound_onlyCityInQueryAndIsInIsFalse() {
	findCitiesCalled = false;
	findStreetCalled = false;
	ImporterConfig importerConfig = new ImporterConfig();
	importerConfig.setOpenStreetMapFillIsIn(false);
	final SolrResponseDto cityResult = EasyMock.createMock(SolrResponseDto.class);
	final Double latitude = 2.1d;
	EasyMock.expect(cityResult.getLat()).andStubReturn(latitude);
	EasyMock.expect(cityResult.getLat_admin_centre()).andStubReturn(latitude+2);
	final Double longitude = 5.2d;
	EasyMock.expect(cityResult.getLng()).andStubReturn(longitude);
	EasyMock.expect(cityResult.getLng_admin_centre()).andStubReturn(longitude+2);
	EasyMock.expect(cityResult.getOpenstreetmap_id()).andStubReturn(888888L);
	EasyMock.expect(cityResult.getName()).andStubReturn("paris");
	EasyMock.expect(cityResult.getAdm2_name()).andStubReturn("ile de france");
	EasyMock.expect(cityResult.getAdm1_name()).andStubReturn("paris");
	EasyMock.expect(cityResult.getAdm3_name()).andStubReturn("adm3 name");
	EasyMock.expect(cityResult.getAdm4_name()).andStubReturn("adm4 name");
	EasyMock.expect(cityResult.getAdm5_name()).andStubReturn("adm5 name");
	EasyMock.expect(cityResult.getIs_in()).andStubReturn("is in");
	EasyMock.expect(cityResult.getStreet_type()).andStubReturn(null);
	EasyMock.expect(cityResult.getZipcodes()).andStubReturn(null);
	EasyMock.expect(cityResult.getIs_in_zip()).andStubReturn(null);
	EasyMock.expect(cityResult.getCountry_code()).andStubReturn("FR");
	EasyMock.expect(cityResult.getScore()).andStubReturn(45.3F);
	EasyMock.expect(cityResult.getFully_qualified_name()).andStubReturn("FQDN");
	EasyMock.expect(cityResult.getFeature_id()).andStubReturn(123L);
	EasyMock.expect(cityResult.getPlacetype()).andStubReturn("City");
	Set<String> zips = new HashSet<String>();
	zips.add("zip");
	EasyMock.expect(cityResult.getZipcodes()).andStubReturn(zips);
	EasyMock.expect(cityResult.getIs_in_adm()).andStubReturn("isinAdm");
	EasyMock.expect(cityResult.getIs_in_place()).andStubReturn("isinPlace");
	EasyMock.replay(cityResult);
	GeocodingService geocodingService = new GeocodingService() {

	    @Override
 	    protected List<SolrResponseDto> findExactMatches(String text,
 				String countryCode,boolean fuzzy, Point point, Double radius, Class[] placetypes) {
	    	findCitiesCalled = true;
			List<SolrResponseDto> cities = new ArrayList<SolrResponseDto>();
			cities.add(cityResult);
			return cities;
 		}

	    @Override
	    protected java.util.List<SolrResponseDto> findStreetInText(String text, String countryCode, Point point, boolean fuzzy, Double radius) {
		findStreetCalled = true;
		return null;
	    };
	};
	geocodingService.setStatsUsageService(statsUsageService);
	geocodingService.setImporterConfig(importerConfig);
	geocodingService.setGisgraphyConfig(gisgraphyConfig);
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andStubReturn(null);
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	String rawAddress = "paris";
	AddressQuery query = new AddressQuery(rawAddress, "ac");
	Assert.assertFalse(findStreetCalled);
    }
    
    
    

    @Test
    public void geocodeRawAdressShouldCallFindStreetInTextIfParsedAddressIsNullIsInIsTrue() {
	//fail("todo");
	findCitiesCalled = false;
	findStreetCalled = false;
	ImporterConfig importerConfig = new ImporterConfig();
	importerConfig.setOpenStreetMapFillIsIn(true);
	final SolrResponseDto cityResult = EasyMock.createMock(SolrResponseDto.class);
	final Double latitude = 2.1d;
	EasyMock.expect(cityResult.getLat()).andStubReturn(latitude);
	final Double longitude = 5.2d;
	EasyMock.expect(cityResult.getLng()).andStubReturn(longitude);
	EasyMock.expect(cityResult.getName()).andStubReturn("paris");
	EasyMock.expect(cityResult.getAdm1_name()).andStubReturn("paris region");
	EasyMock.expect(cityResult.getAdm2_name()).andStubReturn("ile de france");
	EasyMock.expect(cityResult.getAdm3_name()).andStubReturn("adm3 name");
	EasyMock.expect(cityResult.getAdm4_name()).andStubReturn("adm4 name");
	EasyMock.expect(cityResult.getAdm5_name()).andStubReturn("adm5 name");
	EasyMock.expect(cityResult.getFully_qualified_name()).andStubReturn("FQDN");
	EasyMock.expect(cityResult.getZipcodes()).andStubReturn(null);
	EasyMock.expect(cityResult.getIs_in_place()).andStubReturn("is_in_place");
	EasyMock.expect(cityResult.getCountry_code()).andStubReturn("FR");
	EasyMock.expect(cityResult.getFeature_id()).andStubReturn(123L);
	EasyMock.replay(cityResult);
	GeocodingService geocodingService = new GeocodingService() {


	    @Override
	    protected java.util.List<SolrResponseDto> findStreetInText(String text, String countryCode, Point point, boolean fuzzy, Double radius) {
		findStreetCalled = true;
		return null;
	    };
	    
	    @Override
	    protected List<SolrResponseDto> findExactMatches(String text,
	    		String countryCode,boolean fuzzy, Point point, Double radius, Class[] placetypes) {
	    	return null;
	    }
	};
	geocodingService.setGisgraphyConfig(gisgraphyConfig);
	geocodingService.setStatsUsageService(statsUsageService);
	geocodingService.setImporterConfig(importerConfig);
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andStubReturn(null);
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	String rawAddress = "paris champs ellysees";
	AddressQuery query = new AddressQuery(rawAddress, "ac");
	query.setPostal(true);
	geocodingService.geocode(query);
	Assert.assertFalse(findCitiesCalled);
	Assert.assertTrue(findStreetCalled);
    }
    
   
    
   
    @Test
    public void searchHouseNumberTest(){
    	GeocodingService service = new GeocodingService();
    	List<HouseNumberDto> houseNumbers = new ArrayList<HouseNumberDto>();
    	HouseNumberDto number1 = new HouseNumberDto(GeolocHelper.createPoint(2D, 3D), "1");
    	HouseNumberDto number2 = new HouseNumberDto(GeolocHelper.createPoint(4D, 5D), "2");
    	houseNumbers.add(number1);
    	houseNumbers.add(number2);
    	
    	HouseNumberDtoInterpolation result = service.searchHouseNumber(2, houseNumbers,"FR", true);
    	Assert.assertEquals(4D, result.getExactLocation().getX(),0.0001);
    	Assert.assertEquals(5D, result.getExactLocation().getY(),0.0001);
    	Assert.assertEquals(0, result.getHouseNumberDif().intValue());
    	
    }
    
    @Test
    public void searchHouseNumberTest_lower(){
    	GeocodingService service = new GeocodingService();
    	List<HouseNumberDto> houseNumbers = new ArrayList<HouseNumberDto>();
    	HouseNumberDto number1 = new HouseNumberDto(GeolocHelper.createPoint(2D, 3D), "2");
    	HouseNumberDto number2 = new HouseNumberDto(GeolocHelper.createPoint(4D, 5D), "3");
    	HouseNumberDto number3 = new HouseNumberDto(GeolocHelper.createPoint(6D, 7D), "5");
    	HouseNumberDto number4 = new HouseNumberDto(GeolocHelper.createPoint(8D, 9D), "6");
    	houseNumbers.add(number1);
    	houseNumbers.add(number2);
    	houseNumbers.add(number3);
    	houseNumbers.add(number4);
    	
    	HouseNumberDtoInterpolation result = service.searchHouseNumber(4, houseNumbers,"FR", false);
    	System.out.println(result);
    	Assert.assertNull(result.getExactLocation());
    	Assert.assertNull(result.getExactNumber());
    	Assert.assertEquals(4D, result.getLowerLocation().getX(),0.001);
    	Assert.assertEquals(5D, result.getLowerLocation().getY(),0.001);
    	Assert.assertEquals(3, result.getLowerNumber().intValue());
    	Assert.assertEquals(6D, result.getHigherLocation().getX(),0.001);
    	Assert.assertEquals(7D, result.getHigherLocation().getY(),0.001);
    	Assert.assertEquals(5, result.getHigherNumber().intValue());
    	Assert.assertEquals(1, result.getHouseNumberDif().intValue());
    	
    	result = service.searchHouseNumber(1, houseNumbers,"FR", true);
    	Assert.assertNull(result.getExactLocation());
    	Assert.assertNull(result.getExactNumber());
    	Assert.assertNull( result.getLowerLocation());
    	Assert.assertNull( result.getLowerNumber());
    	Assert.assertEquals(2D, result.getHigherLocation().getX(),0.001);
    	Assert.assertEquals(3D, result.getHigherLocation().getY(),0.001);
    	Assert.assertEquals(2, result.getHigherNumber().intValue());
    	Assert.assertEquals(1, result.getHouseNumberDif().intValue());
    	
    	result = service.searchHouseNumber(8, houseNumbers,"FR", true);
    	Assert.assertNull(result.getExactLocation());
    	Assert.assertNull(result.getExactNumber());
    	Assert.assertEquals(8D, result.getLowerLocation().getX(),0.001);
    	Assert.assertEquals(9D, result.getLowerLocation().getY(),0.001);
    	Assert.assertEquals(6, result.getLowerNumber().intValue());
    	Assert.assertNull(null, result.getHigherLocation());
    	Assert.assertNull(null, result.getHigherLocation());
    	Assert.assertNull(result.getHigherNumber());
    	Assert.assertEquals(-2, result.getHouseNumberDif().intValue());
    }
    
    @Test
    public void searchHouseNumberTest_doInterpolation(){
    	GeocodingService service = new GeocodingService();
    	List<HouseNumberDto> houseNumbers = new ArrayList<HouseNumberDto>();
    	HouseNumberDto number1 = new HouseNumberDto(GeolocHelper.createPoint(2D, 3D), "2");
    	HouseNumberDto number2 = new HouseNumberDto(GeolocHelper.createPoint(4D, 5D), "3");
    	HouseNumberDto number3 = new HouseNumberDto(GeolocHelper.createPoint(6D, 7D), "5");
    	HouseNumberDto number4 = new HouseNumberDto(GeolocHelper.createPoint(8D, 9D), "6");
    	houseNumbers.add(number1);
    	houseNumbers.add(number2);
    	houseNumbers.add(number3);
    	houseNumbers.add(number4);
    	
    	HouseNumberDtoInterpolation result = service.searchHouseNumber(4, houseNumbers,"FR", true);
    	System.out.println(result);
    	Assert.assertEquals(5,result.getExactLocation().getX(),0.001);
    	Assert.assertEquals(6,result.getExactLocation().getY(),0.001);
    	Assert.assertEquals(4,result.getExactNumber().intValue());
    	Assert.assertNull(result.getLowerLocation());
    	Assert.assertNull(result.getLowerLocation());
    	Assert.assertNull( result.getLowerNumber());
    	Assert.assertNull(result.getHigherLocation());
    	Assert.assertNull( result.getHigherLocation());
    	Assert.assertNull(result.getHigherNumber());
    	
    	result = service.searchHouseNumber(1, houseNumbers,"FR", true);
    	Assert.assertNull(result.getExactLocation());
    	Assert.assertNull(result.getExactNumber());
    	Assert.assertNull( result.getLowerLocation());
    	Assert.assertNull( result.getLowerNumber());
    	Assert.assertEquals(2D, result.getHigherLocation().getX(),0.001);
    	Assert.assertEquals(3D, result.getHigherLocation().getY(),0.001);
    	Assert.assertEquals(2, result.getHigherNumber().intValue());
    	
    	result = service.searchHouseNumber(7, houseNumbers,"FR", true);
    	Assert.assertNull(result.getExactLocation());
    	Assert.assertNull(result.getExactNumber());
    	Assert.assertEquals(8D, result.getLowerLocation().getX(),0.001);
    	Assert.assertEquals(9D, result.getLowerLocation().getY(),0.001);
    	Assert.assertEquals(6, result.getLowerNumber().intValue());
    	Assert.assertNull(null, result.getHigherLocation());
    	Assert.assertNull(null, result.getHigherLocation());
    	Assert.assertNull(result.getHigherNumber());
    }
    
    @Test
    public void searchHouseNumberTestWithNull(){
    	GeocodingService service = new GeocodingService();
    	List<HouseNumberDto> houseNumbers = new ArrayList<HouseNumberDto>();
    	HouseNumberDto number1 = new HouseNumberDto(GeolocHelper.createPoint(2D, 3D), "1");
    	HouseNumberDto number2 = new HouseNumberDto(GeolocHelper.createPoint(4D, 5D), "2");
    	houseNumbers.add(number1);
    	houseNumbers.add(number2);
    	
    	
    	HouseNumberDtoInterpolation result = service.searchHouseNumber(2, houseNumbers,null, true);
    	Assert.assertEquals(4D, result.getExactLocation().getX(),0.0001);
    	Assert.assertEquals(5D, result.getExactLocation().getY(),0.0001);
    	
    	result = service.searchHouseNumber(2, houseNumbers,null, true);
    	Assert.assertEquals(4D, result.getExactLocation().getX(),0.0001);
    	Assert.assertEquals(5D, result.getExactLocation().getY(),0.0001);
    }
    
    @Test
    public void searchHouseNumberTestforCZSK(){
    	GeocodingService service = new GeocodingService();
    	List<HouseNumberDto> houseNumbers = new ArrayList<HouseNumberDto>();
    	HouseNumberDto number1 = new HouseNumberDto(GeolocHelper.createPoint(2D, 3D), "1");
    	HouseNumberDto number2 = new HouseNumberDto(GeolocHelper.createPoint(4D, 5D), "2");
    	houseNumbers.add(number1);
    	houseNumbers.add(number2);
    	
    	
    	HouseNumberDtoInterpolation result = service.searchHouseNumber(2, houseNumbers,"CZ", true);
    	Assert.assertEquals(4D, result.getExactLocation().getX(),0.0001);
    	Assert.assertEquals(5D, result.getExactLocation().getY(),0.0001);
    	
    	result = service.searchHouseNumber(1, houseNumbers,"CZ", true);
    	Assert.assertEquals(2D, result.getExactLocation().getX(),0.0001);
    	Assert.assertEquals(3D, result.getExactLocation().getY(),0.0001);
    	
    }
    
    @Test
    public void searchHouseNumber_WithNullValues(){
    	GeocodingService service = new GeocodingService();
    	List<HouseNumberDto> houseNumbers = new ArrayList<HouseNumberDto>();
    	Assert.assertNull(service.searchHouseNumber(3, null,"FR", true));
    	Assert.assertNull(service.searchHouseNumber(null, houseNumbers,"FR", true));
    	Assert.assertNull(service.searchHouseNumber(null, null,"FR", true));
    }
    
    @Test
    public void searchHouseNumber_SN(){
        GeocodingService service = new GeocodingService();
        List<HouseNumberDto> houseNumbers = new ArrayList<HouseNumberDto>();
        HouseNumberDto number1 = new HouseNumberDto(GeolocHelper.createPoint(2D, 3D), "SN");
        houseNumbers.add(number1);
        
        HouseNumberDtoInterpolation result = service.searchHouseNumber(2, houseNumbers,"3", true);
        Assert.assertNull(result);
    }
    
    
    
  /*  @Test
    public void findCitiesInText() {
	List<SolrResponseDto> results = new ArrayList<SolrResponseDto>();
	SolrResponseDto solrResponseDto = EasyMock.createNiceMock(SolrResponseDto.class);
	results.add(solrResponseDto);
	FulltextResultsDto mockResultDTO = EasyMock.createMock(FulltextResultsDto.class);
	EasyMock.expect(mockResultDTO.getResultsSize()).andReturn(1);
	EasyMock.expect(mockResultDTO.getResults()).andReturn(results);
	EasyMock.replay(mockResultDTO);

	GeocodingService geocodingService = new GeocodingService();
	String text = "toto";
	String countryCode = "FR";
	FullTextSearchEngine mockfullFullTextSearchEngine = EasyMock.createMock(FullTextSearchEngine.class);
	FulltextQuery query = new FulltextQuery(text, Pagination.paginate().from(0).to(NUMBER_OF_STREET_TO_RETRIEVE), GeocodingService.LONG_OUTPUT, com.gisgraphy.fulltext.Constants.CITY_AND_CITYSUBDIVISION_PLACETYPE, countryCode);
	query.withAllWordsRequired(false).withoutSpellChecking();
	EasyMock.expect(mockfullFullTextSearchEngine.executeQuery(query)).andReturn(mockResultDTO);
	EasyMock.replay(mockfullFullTextSearchEngine);
	geocodingService.setFullTextSearchEngine(mockfullFullTextSearchEngine);

	List<SolrResponseDto> actual = geocodingService.findCitiesInText(text, countryCode);
	Assert.assertEquals(solrResponseDto, actual.get(0));
	EasyMock.verify(mockfullFullTextSearchEngine);
    }
    
     
    @Test
    public void findCitiesInTextWithNullOrEmptyText() {
	List<SolrResponseDto> expected = new ArrayList<SolrResponseDto>();
	GeocodingService geocodingService = new GeocodingService();
	List<SolrResponseDto> actual = geocodingService.findCitiesInText("", "fr");
	Assert.assertEquals(expected, actual);
	actual = geocodingService.findCitiesInText(null, "fr");
	Assert.assertEquals(expected, actual);
    }
*/
    @Test
    public void findStreetInText() {
	List<SolrResponseDto> results = new ArrayList<SolrResponseDto>();
	SolrResponseDto solrResponseDto = EasyMock.createMock(SolrResponseDto.class);
	EasyMock.expect(solrResponseDto.getLat()).andReturn(2D);
	EasyMock.expect(solrResponseDto.getLng()).andReturn(3D);
	EasyMock.replay(solrResponseDto);
	results.add(solrResponseDto);
	FulltextResultsDto mockResultDTO = EasyMock.createMock(FulltextResultsDto.class);
	EasyMock.expect(mockResultDTO.getResultsSize()).andReturn(1);
	EasyMock.expect(mockResultDTO.getResults()).andReturn(results);
	EasyMock.replay(mockResultDTO);

	GeocodingService geocodingService = new GeocodingService();
	String text = "toto";
	String countryCode = "FR";
	FullTextSearchEngine mockfullFullTextSearchEngine = EasyMock.createMock(FullTextSearchEngine.class);
	FulltextQuery query = new FulltextQuery(text, Pagination.paginate().from(0).to(40), GeocodingService.LONG_OUTPUT, com.gisgraphy.fulltext.Constants.STREET_PLACETYPE, countryCode);
	query.withAllWordsRequired(false).withoutSpellChecking();
	EasyMock.expect(mockfullFullTextSearchEngine.executeQuery(query)).andReturn(mockResultDTO);
	EasyMock.replay(mockfullFullTextSearchEngine);
	geocodingService.setFullTextSearchEngine(mockfullFullTextSearchEngine);

	List<SolrResponseDto> actual = geocodingService.findStreetInText(text, countryCode, null, false, null);
	Assert.assertEquals(results, actual);
	EasyMock.verify(mockfullFullTextSearchEngine);
    }
    
    
    @Test
    public void findExactMatches() {
	List<SolrResponseDto> results = new ArrayList<SolrResponseDto>();
	SolrResponseDto solrResponseDto = EasyMock.createMock(SolrResponseDto.class);
	EasyMock.expect(solrResponseDto.getLat()).andReturn(2D);
	EasyMock.expect(solrResponseDto.getLng()).andReturn(3D);
	EasyMock.replay(solrResponseDto);
	results.add(solrResponseDto);
	FulltextResultsDto mockResultDTO = EasyMock.createMock(FulltextResultsDto.class);
	EasyMock.expect(mockResultDTO.getResultsSize()).andReturn(1);
	EasyMock.expect(mockResultDTO.getResults()).andReturn(results);
	EasyMock.replay(mockResultDTO);

	GeocodingService geocodingService = new GeocodingService();
	String text = "toto";
	String countryCode = "FR";
	FullTextSearchEngine mockfullFullTextSearchEngine = EasyMock.createMock(FullTextSearchEngine.class);
	FulltextQuery query = new FulltextQuery(text, GeocodingService.TEN_RESULT_PAGINATION, GeocodingService.LONG_OUTPUT, com.gisgraphy.fulltext.Constants.CITY_CITYSUB_ADM_PLACETYPE, countryCode);
	query.withAllWordsRequired(true).withoutSpellChecking();
	EasyMock.expect(mockfullFullTextSearchEngine.executeQuery(query)).andReturn(mockResultDTO);
	EasyMock.replay(mockfullFullTextSearchEngine);
	geocodingService.setFullTextSearchEngine(mockfullFullTextSearchEngine);

	List<SolrResponseDto> actual = geocodingService.findExactMatches(text, countryCode,false, null, null, null);
	Assert.assertEquals(results, actual);
	EasyMock.verify(mockfullFullTextSearchEngine);
    }
    
    
    @Test
    public void findStreetInTextWithNullOrEmptyText() {
	List<SolrResponseDto> expected = new ArrayList<SolrResponseDto>();
	GeocodingService geocodingService = new GeocodingService();
	List<SolrResponseDto> actual = geocodingService.findStreetInText("", "fr", null, false, null);
	Assert.assertEquals(expected, actual);
	actual = geocodingService.findStreetInText(null, "fr", null, false, null);
	Assert.assertEquals(expected, actual);
    }
   
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street() {
	// setup
	GeocodingService geocodingService = new GeocodingService();
	List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
	SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreetFQDN("is_in");
	streets.add(street);
	// exercise
	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, null);

	// verify
	Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
	Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
	Assert.assertEquals(1, addressResultsDto.getResult().size());
	Address address = addressResultsDto.getResult().get(0);
	Assert.assertEquals("latitude is not correct", street.getLat_admin_centre(), address.getLat());
	Assert.assertEquals("longitude is not correct", street.getLng_admin_centre(), address.getLng());
	Assert.assertEquals("sourceid is not correct and should be osm one", street.getOpenstreetmap_id(), address.getSourceId());
	Assert.assertEquals("id is not correct and should be osm one", street.getFeature_id(), address.getId());
	Assert.assertEquals("geocoding level is not correct", GeocodingLevels.STREET, address.getGeocodingLevel());
	Assert.assertEquals("street name is not correct", street.getName(), address.getStreetName());
	Assert.assertEquals("street type is not correct", street.getStreet_type(), address.getStreetType());
	Assert.assertEquals("city name is not correct", street.getIs_in(), address.getCity());
	Assert.assertEquals("countrycode is not correct", street.getCountry_code(), address.getCountryCode());
	Assert.assertEquals("country is not correct", CountryInfo.countryLookupMap.get(address.getCountryCode().toUpperCase()), address.getCountry());
	Assert.assertEquals("Adm Name should not be the deeper one but the is_in_adm one", street.getIs_in_adm(), address.getState());
	Assert.assertEquals("place is not correct", street.getIs_in_place(), address.getDependentLocality());
	Assert.assertFalse("formated Postal is not correct should not contains streettype",  address.getFormatedPostal().contains(address.getStreetType()));
	Assert.assertNotNull("formated Postal is not correct ", address.getFormatedPostal());
	Assert.assertEquals("formated full is not correct", street.getFully_qualified_name(), address.getFormatedFull());
	
	Assert.assertNotNull(address.getAdm1Name());
	Assert.assertEquals("adm1Name is not correct", street.getAdm1_name(), address.getAdm1Name());
	Assert.assertNotNull(address.getAdm2Name());
	Assert.assertEquals("adm2Name is not correct", street.getAdm2_name(), address.getAdm2Name());
	Assert.assertNotNull(address.getAdm3Name());
	Assert.assertEquals("adm3Name is not correct", street.getAdm3_name(), address.getAdm3Name());
	Assert.assertNotNull(address.getAdm4Name());
	Assert.assertEquals("adm4Name is not correct", street.getAdm4_name(), address.getAdm4Name());
	Assert.assertNotNull(address.getAdm5Name());
	Assert.assertEquals("adm5Name is not correct", street.getAdm5_name(), address.getAdm5Name());
	
    }
    
    
    
    
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_NoIsInPlace() {
	// setup
	GeocodingService geocodingService = new GeocodingService();
	List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
	SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in",null,111L,"1", "2");
	streets.add(street);
	SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in",null,222L,"1", "2");
	streets.add(street2);
	
	// exercise
	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, null);

	// verify
	Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
	Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
	Assert.assertEquals(1, addressResultsDto.getResult().size());
	
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_sameIsInPlace() {
	// setup
	GeocodingService geocodingService = new GeocodingService();
	List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
	SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"1", "2");
	streets.add(street);
	SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"1", "2");
	streets.add(street2);
	
	// exercise
	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, null);

	// verify
	Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
	Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
	Assert.assertEquals(1, addressResultsDto.getResult().size());
	
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_noHouseThenHouse() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,null, null);
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"4", "5");
    streets.add(street2);
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "2");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(1, addressResultsDto.getResult().size());
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",222L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation",null, addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",3F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",2f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_HouseThenNoHouse() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"4", "5");
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,null, null);
    streets.add(street);
    streets.add(street2);
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "2");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(1, addressResultsDto.getResult().size());
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",222L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation",null, addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",3F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",2f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    }
    
    
    
   /*----------------------------------------------------------------one street with multiple segment-----------------------------*/ 
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_InterpolationOnFirstSegment() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"1", "3");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"4", "5");
    streets.add(street2);
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "2");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(1, addressResultsDto.getResult().size());
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",111L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation","2", addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",4F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",3f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    }
    
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_InterpolationOnSecondSegment() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"1", "3");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"7", "9");
    streets.add(street2);
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "8");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(1, addressResultsDto.getResult().size());
    Assert.assertEquals("house number should be set because there is interpolation","8", addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("the second segment should be return because the interpolation is done on the first segment",222L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("Coordinate should be the house one, not the street",4F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",3f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    }
    
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_higherOnSecondSegment() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"1", "3");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"7", "9");
    streets.add(street2);
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "10");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(1, addressResultsDto.getResult().size());
    Assert.assertEquals("house number should be not be set (only interpolation does",null, addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("the second segment should be return because the interpolation is done on the first segment",222L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("Coordinate should be the house one, not the street",5F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",4f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_LowerOnSecondSegment() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"1", "3");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"7", "9");
    streets.add(street2);
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "6");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(1, addressResultsDto.getResult().size());
    Assert.assertEquals("house number should be not be set (only interpolation does",null, addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("the second segment should be return because the interpolation is done on the first segment",222L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("Coordinate should be the house one, not the street",3F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",2f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    }
    
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_LowerOnfirstSegment() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"2", "4");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"7", "9");
    streets.add(street2);
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "1");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(1, addressResultsDto.getResult().size());
    Assert.assertEquals("house number should be not be set (only interpolation does",null, addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("the second segment should be return because the interpolation is done on the first segment",111L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("Coordinate should be the house one, not the street",3F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",2f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_higherOnfirstSegment() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"2", "4");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"7", "9");
    streets.add(street2);
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "5");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(1, addressResultsDto.getResult().size());
    Assert.assertEquals("house number should be not be set (only interpolation does",null, addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("the second segment should be return because the interpolation is done on the first segment",111L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("Coordinate should be the house one, not the street",5F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",4f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    }
    
    
    /*----------------------------------------------------------------Several street second not null name--------------------------------*/
    
    
    
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_InterpolationOnFirstSegment_Then_otherWithNullName() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"1", "3");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"4", "5");
    streets.add(street2);
    SolrResponseDto street3 = GisgraphyTestHelper.createSolrResponseDtoForStreet(null,"is_in","is_in_place",333L,"1", "3");
    streets.add(street3);
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "2");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(2, addressResultsDto.getResult().size());
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",111L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation","2", addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",4F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",3f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    
    
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",333L, addressResultsDto.getResult().get(1).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation","2", addressResultsDto.getResult().get(1).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",4F, addressResultsDto.getResult().get(1).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",3f, addressResultsDto.getResult().get(1).getLng().floatValue(),0.0001);
    
    }
    
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_InterpolationOnSecondSegment_Then_otherWithNullName() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"1", "3");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"7", "9");
    streets.add(street2);
    SolrResponseDto street3 = GisgraphyTestHelper.createSolrResponseDtoForStreet(null,"is_in","is_in_place",333L,"7", "9");
    streets.add(street3);
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "8");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(2, addressResultsDto.getResult().size());
    Assert.assertEquals("house number should be set because there is interpolation","8", addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("the second segment should be return because the interpolation is done on the first segment",222L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("Coordinate should be the house one, not the street",4F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",3f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",333L, addressResultsDto.getResult().get(1).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation","8", addressResultsDto.getResult().get(1).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",4F, addressResultsDto.getResult().get(1).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",3f, addressResultsDto.getResult().get(1).getLng().floatValue(),0.0001);
    
    }
    
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_higherOnSecondSegment_Then_otherWithNullName() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"1", "3");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"7", "9");
    streets.add(street2);
    SolrResponseDto street3 = GisgraphyTestHelper.createSolrResponseDtoForStreet(null,"is_in","is_in_place",333L,"7", "9");
    streets.add(street3);
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "10");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(2, addressResultsDto.getResult().size());
    Assert.assertEquals("house number should be not be set (only interpolation does",null, addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("the second segment should be return because the interpolation is done on the first segment",222L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("Coordinate should be the house one, not the street",5F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",4f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",333L, addressResultsDto.getResult().get(1).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation",null, addressResultsDto.getResult().get(1).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",5F, addressResultsDto.getResult().get(1).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",4f, addressResultsDto.getResult().get(1).getLng().floatValue(),0.0001);
    
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_LowerOnSecondSegment_Then_otherWithNullName() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"1", "3");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"7", "9");
    streets.add(street2);
    SolrResponseDto street3 = GisgraphyTestHelper.createSolrResponseDtoForStreet(null,"is_in","is_in_place",333L,"7", "9");
    streets.add(street3);
    
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "6");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(2, addressResultsDto.getResult().size());
    Assert.assertEquals("house number should be not be set (only interpolation does",null, addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("the second segment should be return because the interpolation is done on the first segment",222L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("Coordinate should be the house one, not the street",3F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",2f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",333L, addressResultsDto.getResult().get(1).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation",null, addressResultsDto.getResult().get(1).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",3F, addressResultsDto.getResult().get(1).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",2f, addressResultsDto.getResult().get(1).getLng().floatValue(),0.0001);
    
    }
    
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_LowerOnfirstSegment_Then_otherWithNullName() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"2", "4");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"7", "9");
    streets.add(street2);
    SolrResponseDto street3 = GisgraphyTestHelper.createSolrResponseDtoForStreet(null,"is_in","is_in_place",333L,"2", "4");
    streets.add(street3);
    
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "1");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(2, addressResultsDto.getResult().size());
    Assert.assertEquals("house number should be not be set (only interpolation does",null, addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("the second segment should be return because the interpolation is done on the first segment",111L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("Coordinate should be the house one, not the street",3F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",2f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",333L, addressResultsDto.getResult().get(1).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation",null, addressResultsDto.getResult().get(1).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",3F, addressResultsDto.getResult().get(1).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",2f, addressResultsDto.getResult().get(1).getLng().floatValue(),0.0001);
    
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_higherOnfirstSegment_Then_otherWithNotName() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"2", "4");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"7", "9");
    streets.add(street2);
    SolrResponseDto street3 = GisgraphyTestHelper.createSolrResponseDtoForStreet(null,"is_in","is_in_place",333L,"2", "4");
    streets.add(street3);
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "5");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(2, addressResultsDto.getResult().size());
    Assert.assertEquals("house number should be not be set (only interpolation does",null, addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("the second segment should be return because the interpolation is done on the first segment",111L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("Coordinate should be the house one, not the street",5F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",4f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    Assert.assertEquals("house number should be not be set (only interpolation does",null, addressResultsDto.getResult().get(1).getHouseNumber());
    Assert.assertEquals("the second segment should be return because the interpolation is done on the first segment",333L, addressResultsDto.getResult().get(1).getSourceId().intValue());
    Assert.assertEquals("Coordinate should be the house one, not the street",5F, addressResultsDto.getResult().get(1).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",4f, addressResultsDto.getResult().get(1).getLng().floatValue(),0.0001);
    
    }
    
    /*----------------------------------------------------------------Several street second null name--------------------------------*/
    
    
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_InterpolationOnFirstSegment_Then_otherWithNotNullName() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"1", "3");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"4", "5");
    streets.add(street2);
    SolrResponseDto street3 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name2","is_in","is_in_place",333L,"1", "3");
    streets.add(street3);
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "2");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(2, addressResultsDto.getResult().size());
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",111L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation","2", addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",4F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",3f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    
    
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",333L, addressResultsDto.getResult().get(1).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation","2", addressResultsDto.getResult().get(1).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",4F, addressResultsDto.getResult().get(1).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",3f, addressResultsDto.getResult().get(1).getLng().floatValue(),0.0001);
    
    }
    
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_InterpolationOnSecondSegment_Then_otherWithNotNullName() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"1", "3");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"7", "9");
    streets.add(street2);
    SolrResponseDto street3 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name2","is_in","is_in_place",333L,"7", "9");
    streets.add(street3);
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "8");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(2, addressResultsDto.getResult().size());
    Assert.assertEquals("house number should be set because there is interpolation","8", addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("the second segment should be return because the interpolation is done on the first segment",222L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("Coordinate should be the house one, not the street",4F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",3f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",333L, addressResultsDto.getResult().get(1).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation","8", addressResultsDto.getResult().get(1).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",4F, addressResultsDto.getResult().get(1).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",3f, addressResultsDto.getResult().get(1).getLng().floatValue(),0.0001);
    
    }
    
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_higherOnSecondSegment_Then_otherWithNotNullName() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"1", "3");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"7", "9");
    streets.add(street2);
    SolrResponseDto street3 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name2","is_in","is_in_place",333L,"7", "9");
    streets.add(street3);
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "10");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(2, addressResultsDto.getResult().size());
    Assert.assertEquals("house number should be not be set (only interpolation does",null, addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("the second segment should be return because the interpolation is done on the first segment",222L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("Coordinate should be the house one, not the street",5F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",4f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",333L, addressResultsDto.getResult().get(1).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation",null, addressResultsDto.getResult().get(1).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",5F, addressResultsDto.getResult().get(1).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",4f, addressResultsDto.getResult().get(1).getLng().floatValue(),0.0001);
    
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_LowerOnSecondSegment_Then_otherWithNotNullName() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"1", "3");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"7", "9");
    streets.add(street2);
    SolrResponseDto street3 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name2","is_in","is_in_place",333L,"7", "9");
    streets.add(street3);
    
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "6");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(2, addressResultsDto.getResult().size());
    Assert.assertEquals("house number should be not be set (only interpolation does",null, addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("the second segment should be return because the interpolation is done on the first segment",222L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("Coordinate should be the house one, not the street",3F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",2f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",333L, addressResultsDto.getResult().get(1).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation",null, addressResultsDto.getResult().get(1).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",3F, addressResultsDto.getResult().get(1).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",2f, addressResultsDto.getResult().get(1).getLng().floatValue(),0.0001);
    
    }
    
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_LowerOnfirstSegment_Then_otherWithNotNullName() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"2", "4");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"7", "9");
    streets.add(street2);
    SolrResponseDto street3 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name2","is_in","is_in_place",333L,"2", "4");
    streets.add(street3);
    
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "1");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(2, addressResultsDto.getResult().size());
    Assert.assertEquals("house number should be not be set (only interpolation does",null, addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("the second segment should be return because the interpolation is done on the first segment",111L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("Coordinate should be the house one, not the street",3F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",2f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",333L, addressResultsDto.getResult().get(1).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation",null, addressResultsDto.getResult().get(1).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",3F, addressResultsDto.getResult().get(1).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",2f, addressResultsDto.getResult().get(1).getLng().floatValue(),0.0001);
    
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_higherOnfirstSegment_Then_otherWithNotNullName() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"2", "4");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"7", "9");
    streets.add(street2);
    SolrResponseDto street3 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name2","is_in","is_in_place",333L,"2", "4");
    streets.add(street3);
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "5");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(2, addressResultsDto.getResult().size());
    Assert.assertEquals("house number should be not be set (only interpolation does",null, addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("the second segment should be return because the interpolation is done on the first segment",111L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("Coordinate should be the house one, not the street",5F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",4f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    Assert.assertEquals("house number should be not be set (only interpolation does",null, addressResultsDto.getResult().get(1).getHouseNumber());
    Assert.assertEquals("the second segment should be return because the interpolation is done on the first segment",333L, addressResultsDto.getResult().get(1).getSourceId().intValue());
    Assert.assertEquals("Coordinate should be the house one, not the street",5F, addressResultsDto.getResult().get(1).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",4f, addressResultsDto.getResult().get(1).getLng().floatValue(),0.0001);
    
    }
    
    
    /*-------------------------------------------*/
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_InterpolationOnFirstSegment_nullNameFirst() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet(null,"is_in","is_in_place",111L,"1", "3");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",222L,"4", "5");
    streets.add(street2);
   
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "2");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(2, addressResultsDto.getResult().size());
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",111L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation","2", addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",4F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",3f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    
    
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",222L, addressResultsDto.getResult().get(1).getSourceId().intValue());
    Assert.assertEquals("house number should be set only interpolation and exact does",null, addressResultsDto.getResult().get(1).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",3F, addressResultsDto.getResult().get(1).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",2f, addressResultsDto.getResult().get(1).getLng().floatValue(),0.0001);
    
    }
    
    
    /*-------------------------------------------*/
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_same_InterpolationOnFirstSegment_nullNamethenNullName() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet(null,"is_in","is_in_place",111L,"1", "3");
    streets.add(street);
    SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet(null,"is_in","is_in_place",333L,"1", "3");
    streets.add(street2);
    
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, "2");

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(2, addressResultsDto.getResult().size());
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",111L, addressResultsDto.getResult().get(0).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation","2", addressResultsDto.getResult().get(0).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",4F, addressResultsDto.getResult().get(0).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",3f, addressResultsDto.getResult().get(0).getLng().floatValue(),0.0001);
    
    
    
    Assert.assertEquals("the first segment should be return because the interpolation is done on the first segment",333L, addressResultsDto.getResult().get(1).getSourceId().intValue());
    Assert.assertEquals("house number should be set because there is interpolation","2", addressResultsDto.getResult().get(1).getHouseNumber());
    Assert.assertEquals("Coordinate should be the house one, not the street",4F, addressResultsDto.getResult().get(1).getLat().floatValue(),0.0001);
    Assert.assertEquals("Coordinate should be the house one, not the street",3f, addressResultsDto.getResult().get(1).getLng().floatValue(),0.0001);
    
    }
    
    
    
    
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_differentIsInPlace() {
	// setup
	GeocodingService geocodingService = new GeocodingService();
	List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
	SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"1", "2");
	streets.add(street);
	SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place2",222L,"1", "2");
	streets.add(street2);
	
	// exercise
	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, null);

	// verify
	Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
	Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
	Assert.assertEquals(2, addressResultsDto.getResult().size());
	
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_differentIsIn() {
	// setup
	GeocodingService geocodingService = new GeocodingService();
	List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
	SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in","is_in_place",111L,"1", "2");
	streets.add(street);
	SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreet("street Name","is_in2","is_in_place",222L,"1", "2");
	streets.add(street2);
	
	// exercise
	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, null);

	// verify
	Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
	Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
	Assert.assertEquals(2, addressResultsDto.getResult().size());
	
    }
    
    //because a street can have several segment in several area with different zip, we only take isin + isinplace
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_duplicate_FQDN_With_differentZipShouldDuplicate() {
	// setup
	GeocodingService geocodingService = new GeocodingService();
	List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
	SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreetFQDN("fqdn 567");
	
	streets.add(street);
	SolrResponseDto street2 = GisgraphyTestHelper.createSolrResponseDtoForStreetFQDN("fqdn 789");
	streets.add(street2);
	
	// exercise
	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, null);

	// verify
	Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
	Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
	Assert.assertEquals(1, addressResultsDto.getResult().size());
	
    }
    
    
   
    
    
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_street_houseNumber() {
	// setup
	GeocodingService geocodingService = new GeocodingService();
	List<SolrResponseDto> streets = new ArrayList<SolrResponseDto>();
	SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreetFQDN("is_in");
	streets.add(street);
	String houseNumberToFind = "2";
	// exercise
	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(streets, houseNumberToFind);

	// verify
	Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
	Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
	Assert.assertEquals(1, addressResultsDto.getResult().size());
	Address address = addressResultsDto.getResult().get(0);
	Assert.assertEquals("latitude is not correct, it should be the house number one", 5D, address.getLat(),0.001);
	Assert.assertEquals("longitude is not correct, it should be the house number one", 4D, address.getLng(),0.001);
	Assert.assertEquals("sourceid is not correct and should be osm one", street.getOpenstreetmap_id(), address.getSourceId());
	Assert.assertEquals("id is not correct and should be osm one", street.getFeature_id(), address.getId());
	Assert.assertEquals("geocoding level is not correct", GeocodingLevels.HOUSE_NUMBER, address.getGeocodingLevel());
	Assert.assertEquals("street name is not correct", street.getName(), address.getStreetName());
	Assert.assertEquals("street type is not correct", street.getStreet_type(), address.getStreetType());
	Assert.assertEquals("city name is not correct", street.getIs_in(), address.getCity());
	Assert.assertEquals("countrycode is not correct", street.getCountry_code(), address.getCountryCode());
	
	Assert.assertEquals("zip is not correct", street.getZipcodes().iterator().next(), address.getZipCode());
	Assert.assertEquals("Adm Name should not be the deeper one but the is_inadm one", street.getIs_in_adm(), address.getState());
	Assert.assertEquals("place is not correct", street.getIs_in_place(), address.getDependentLocality());
	Assert.assertFalse("formated Postal is not correct should not contains streettype",  address.getFormatedPostal().contains(address.getStreetType()));
	Assert.assertNotNull("formated Postal is not correct ", address.getFormatedPostal());
	Assert.assertEquals("formated full is not correct", labelGenerator.getFullyQualifiedName(address), address.getFormatedFull());
	
	Assert.assertNotNull(address.getAdm1Name());
	Assert.assertEquals("adm1Name is not correct", street.getAdm1_name(), address.getAdm1Name());
	Assert.assertNotNull(address.getAdm2Name());
	Assert.assertEquals("adm2Name is not correct", street.getAdm2_name(), address.getAdm2Name());
	Assert.assertNotNull(address.getAdm3Name());
	Assert.assertEquals("adm3Name is not correct", street.getAdm3_name(), address.getAdm3Name());
	Assert.assertNotNull(address.getAdm4Name());
	Assert.assertEquals("adm4Name is not correct", street.getAdm4_name(), address.getAdm4Name());
	Assert.assertNotNull(address.getAdm5Name());
	Assert.assertEquals("adm5Name is not correct", street.getAdm5_name(), address.getAdm5Name());
	
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_severalType_StreetthenCity() {
	// setup
	GeocodingService geocodingService = new GeocodingService();
	List<SolrResponseDto> results = new ArrayList<SolrResponseDto>();
	SolrResponseDto city = GisgraphyTestHelper.createSolrResponseDtoForCity();
	SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreetFQDN("is_in");
	results.add(street);
	results.add(city);
	// exercise
	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(results, null);

	// verify
	Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
	Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
	Assert.assertEquals(2, addressResultsDto.getResult().size());
	Address address1 = addressResultsDto.getResult().get(0);
	Address address2 = addressResultsDto.getResult().get(1);
	Assert.assertEquals("id is not correct for address 1", street.getFeature_id(), address1.getId());
	Assert.assertEquals("id is not correct for address 2", city.getFeature_id(), address2.getId());
	
	Assert.assertEquals("source id is not correct for address 1", street.getOpenstreetmap_id(), address1.getSourceId());
	Assert.assertEquals("source id is not correct for address 2", city.getOpenstreetmap_id(), address2.getSourceId());
	
	
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_severalType_onlyCity() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> results = new ArrayList<SolrResponseDto>();
    SolrResponseDto city = GisgraphyTestHelper.createSolrResponseDtoForCity();
    results.add(city);
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(results, null);

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(1, addressResultsDto.getResult().size());
    Address address1 = addressResultsDto.getResult().get(0);
    Assert.assertEquals("id is not correct for address 2", city.getFeature_id(), address1.getId());
    
    
    }
    
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_severalType_cityThenStreet() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> results = new ArrayList<SolrResponseDto>();
    SolrResponseDto city = GisgraphyTestHelper.createSolrResponseDtoForCity();
    SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreetFQDN("is_in");
    results.add(city);
    results.add(street);
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(results, null);

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(2, addressResultsDto.getResult().size());
    Address address1 = addressResultsDto.getResult().get(0);
    Address address2 = addressResultsDto.getResult().get(1);
    Assert.assertEquals("id is not correct for address 2", city.getFeature_id(), address1.getId());
    Assert.assertEquals("id is not correct for address 1", street.getFeature_id(), address2.getId());
    
    Assert.assertEquals("source id is not correct for address 2", city.getOpenstreetmap_id(), address1.getSourceId());
    Assert.assertEquals("source id is not correct for address 1", street.getOpenstreetmap_id(), address2.getSourceId());
    
    
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_severalType_cityThenAdm() {
    // setup
    GeocodingService geocodingService = new GeocodingService();
    List<SolrResponseDto> results = new ArrayList<SolrResponseDto>();
    SolrResponseDto city = GisgraphyTestHelper.createSolrResponseDtoForCity();
    SolrResponseDto adm = GisgraphyTestHelper.createSolrResponseDtoForAdm();
    results.add(city);
    results.add(adm);
    // exercise
    AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(results, null);

    // verify
    Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    Assert.assertEquals(2, addressResultsDto.getResult().size());
    Address address1 = addressResultsDto.getResult().get(0);
    Address address2 = addressResultsDto.getResult().get(1);
    Assert.assertEquals("id is not correct for address 2", city.getFeature_id(), address1.getId());
    Assert.assertEquals("id is not correct for address 1", adm.getFeature_id(), address2.getId());
    
    Assert.assertEquals("source id is not correct for address 2", city.getOpenstreetmap_id(), address1.getSourceId());
    Assert.assertEquals("source id is not correct for address 1", adm.getOpenstreetmap_id(), address2.getSourceId());
    
    
    }
    
    
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_city() {
	// setup
	GeocodingService geocodingService = new GeocodingService();
	SolrResponseDto city = GisgraphyTestHelper.createSolrResponseDtoForCity();
	List<SolrResponseDto> results = new ArrayList<SolrResponseDto>();
	results.add(city);
	// exercise
	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(results, null);

	// verify
	Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
	Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
	Assert.assertEquals(1, addressResultsDto.getResult().size());
	Address address = addressResultsDto.getResult().get(0);
	Assert.assertEquals("id is not correct", city.getFeature_id(), address.getId());
	Assert.assertEquals("id is not correct", city.getOpenstreetmap_id(), address.getSourceId());
	Assert.assertEquals("latitude is not correct, admin centre should be prefered", city.getLat_admin_centre(), address.getLat());
	Assert.assertEquals("longitude is not correct, admin centre should be prefered", city.getLng_admin_centre(), address.getLng());
	Assert.assertEquals("geocoding level is not correct", GeocodingLevels.CITY, address.getGeocodingLevel());
	Assert.assertNull("street name is not correct", address.getStreetName());
	Assert.assertEquals("city name is not correct", city.getName(), address.getCity());
	Assert.assertNull("street type is not correct", address.getStreetType());
	Assert.assertEquals("score is not correct", city.getScore(), address.getScore());
	Assert.assertEquals("zipcode is not correct", city.getZipcodes().iterator().next(), address.getZipCode());
	Assert.assertEquals("Adm Name should be the lower one", city.getAdm1_name(), address.getState());
	Assert.assertEquals("countrycode is not correct", city.getCountry_code(), address.getCountryCode());
	Assert.assertEquals("country is not correct", CountryInfo.countryLookupMap.get(address.getCountryCode().toUpperCase()), address.getCountry());
	Assert.assertNotNull("formated Postal is not correct ", address.getFormatedPostal());
	Assert.assertEquals("formated full is not correct", city.getFully_qualified_name(), address.getFormatedFull());
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_citySubdivision() {
	// setup
	GeocodingService geocodingService = new GeocodingService();
	SolrResponseDto citySubdivision = GisgraphyTestHelper.createSolrResponseDtoForCitySudivision();
	List<SolrResponseDto> results = new ArrayList<SolrResponseDto>();
	results.add(citySubdivision);
	// exercise
	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(results, null);

	// verify
	Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
	Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
	Assert.assertEquals(1, addressResultsDto.getResult().size());
	Address address = addressResultsDto.getResult().get(0);
	Assert.assertEquals("sourceid is not correct and should be osm one", citySubdivision.getOpenstreetmap_id(), address.getSourceId());
	Assert.assertEquals("id is not correct and should be osm one", citySubdivision.getFeature_id(), address.getId());
	Assert.assertEquals("latitude is not correct, admin centre should be prefered", citySubdivision.getLat_admin_centre(), address.getLat());
	Assert.assertEquals("longitude is not correct, admin centre should be prefered", citySubdivision.getLng_admin_centre(), address.getLng());
	Assert.assertEquals("geocoding level is not correct", GeocodingLevels.CITY_SUBDIVISION, address.getGeocodingLevel());
	Assert.assertNull("street name is not correct", address.getStreetName());
	Assert.assertEquals("quarter name is not correct", citySubdivision.getName(), address.getQuarter());
	Assert.assertNull("city name is not correct", address.getCity());
	Assert.assertNull("street type is not correct", address.getStreetType());
	Assert.assertEquals("score is not correct", citySubdivision.getScore(), address.getScore());
	Assert.assertEquals("zipcode is not correct", citySubdivision.getZipcodes().iterator().next(), address.getZipCode());
	Assert.assertEquals("Adm Name should be the lower one", citySubdivision.getAdm1_name(), address.getState());
	Assert.assertEquals("countrycode is not correct", citySubdivision.getCountry_code(), address.getCountryCode());
	Assert.assertEquals("country is not correct", CountryInfo.countryLookupMap.get(address.getCountryCode().toUpperCase()), address.getCountry());
	Assert.assertNotNull("formated Postal is not correct ", address.getFormatedPostal());
	Assert.assertEquals("formated full is not correct", citySubdivision.getFully_qualified_name(), address.getFormatedFull());
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_adm() {
    	GeocodingService geocodingService = new GeocodingService();
    	SolrResponseDto adm = GisgraphyTestHelper.createSolrResponseDtoForAdm();
    	List<SolrResponseDto> results = new ArrayList<SolrResponseDto>();
    	results.add(adm);
    	// exercise
    	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(results, null);

    	// verify
    	Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    	Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    	Assert.assertEquals(1, addressResultsDto.getResult().size());
    	Address address = addressResultsDto.getResult().get(0);
    	Assert.assertEquals("id is not correct", adm.getFeature_id(), address.getId());
    	Assert.assertEquals("source id is not correct", adm.getOpenstreetmap_id(), address.getSourceId());
    	Assert.assertEquals("latitude is not correct, admin centre should be prefered", adm.getLat_admin_centre(), address.getLat());
    	Assert.assertEquals("longitude is not correct, admin centre should be prefered", adm.getLng_admin_centre(), address.getLng());
    	Assert.assertEquals("geocoding level is not correct", GeocodingLevels.STATE, address.getGeocodingLevel());
    	Assert.assertNull("street name is not correct", address.getStreetName());
    	Assert.assertNull("city name is not correct", address.getCity());
    	Assert.assertNull("street type is not correct", address.getStreetType());
    	Assert.assertNull("zipcode is not correct", address.getZipCode());
    	Assert.assertEquals("score is not correct", adm.getScore(), address.getScore());
    	Assert.assertEquals("Adm Name should be the deeper one", adm.getName(), address.getState());
    	Assert.assertEquals("countrycode is not correct", adm.getCountry_code(), address.getCountryCode());
    	Assert.assertEquals("country is not correct", CountryInfo.countryLookupMap.get(address.getCountryCode().toUpperCase()), address.getCountry());
    	
    	Assert.assertNotNull("formated Postal is not correct ", address.getFormatedPostal());
    	Assert.assertEquals("formated full is not correct", adm.getFully_qualified_name(), address.getFormatedFull());
    }
    
    @Test
    public void buildAddressResultDtoFromSolrResponseDto_gisFeature() {
    	GeocodingService geocodingService = new GeocodingService();
    	SolrResponseDto feature = GisgraphyTestHelper.createSolrResponseDtoForGisFeature();
    	List<SolrResponseDto> cities = new ArrayList<SolrResponseDto>();
    	cities.add(feature);
    	// exercise
    	AddressResultsDto addressResultsDto = geocodingService.buildAddressResultDtoFromSolrResponseDto(cities, null);

    	// verify
    	Assert.assertNotNull("qtime should not be null", addressResultsDto.getQTime());
    	Assert.assertNotNull("results should not be null, but at least empty list", addressResultsDto.getResult());
    	Assert.assertEquals(1, addressResultsDto.getResult().size());
    	Address address = addressResultsDto.getResult().get(0);
    	Assert.assertEquals("id is not correct", feature.getFeature_id(), address.getId());
    	Assert.assertEquals("latitude is not correct, admin centre should be prefered", feature.getLat_admin_centre(), address.getLat());
    	Assert.assertEquals("longitude is not correct, admin centre should be prefered", feature.getLng_admin_centre(), address.getLng());
    	Assert.assertEquals("geocoding level is not correct", GeocodingLevels.STATE, address.getGeocodingLevel());
    	Assert.assertNull("street name is not correct", address.getStreetName());
    	Assert.assertNull("city name is not correct", address.getCity());
    	Assert.assertNull("street type is not correct", address.getStreetType());
    	Assert.assertNull("zipcode is not correct", address.getZipCode());
    	Assert.assertEquals("score is not correct", feature.getScore(), address.getScore());
    	Assert.assertEquals("Adm Name should be the lower one", feature.getAdm1_name(), address.getState());
    	Assert.assertEquals("countrycode is not correct", feature.getCountry_code(), address.getCountryCode());
    	Assert.assertEquals("country is not correct", CountryInfo.countryLookupMap.get(address.getCountryCode().toUpperCase()), address.getCountry());
    	Assert.assertNotNull("formated Postal is not correct ", address.getFormatedPostal());
    	Assert.assertEquals("formated full is not correct", feature.getFully_qualified_name(), address.getFormatedFull());
    }

   
    
    @Test
    public void testStatsShouldBeIncreaseForGeocode_addressQuery() {
	GeocodingService geocodingService = new GeocodingService() {
		 @Override
		    protected List<SolrResponseDto> findExactMatches(String text,
		    		String countryCode,boolean fuzzy, Point point, Double radius, Class[] placetypes) {
		return new ArrayList<SolrResponseDto>();
	    }
	    @Override
	    protected List<SolrResponseDto> findStreetInText(String text, String countryCode, Point point, boolean fuzzy, Double radius) {
		return new ArrayList<SolrResponseDto>();
	    }
	};
	geocodingService.setStatsUsageService(statsUsageService);
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	List<Address> addressList = new ArrayList<Address>() {
	    {
		Address address = new Address();
		address.setCity("city");
		add(address);
		
	    }
	};
	geocodingService.setGisgraphyConfig(gisgraphyConfig);
	gisgraphyConfig.setSearchForExactMatchWhenGeocoding(false);
	AddressResultsDto addressresults = new AddressResultsDto(addressList, 3L);
	statsUsageService.increaseUsage(StatsUsageType.GEOCODING);
	EasyMock.replay(statsUsageService);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andStubReturn(addressresults);
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	AddressQuery addressQuery = new AddressQuery("paris", "fr");
	geocodingService.setImporterConfig(new ImporterConfig());
	geocodingService.geocode(addressQuery);
	EasyMock.verify(statsUsageService);
    }

    @Test
    public void testStatsShouldBeIncreaseForGeocode_address() {
	GeocodingService geocodingService = new GeocodingService() {
		 @Override
		    protected List<SolrResponseDto> findExactMatches(String text,
		    		String countryCode,boolean fuzzy, Point point, Double radius, Class[] placetypes) {
		return new ArrayList<SolrResponseDto>();
	    }
	    
	    @Override
	    protected List<SolrResponseDto> findStreetInText(String text, String countryCode, Point point, boolean fuzzy, Double radius) {
		return new ArrayList<SolrResponseDto>();
	    }
	};
	geocodingService.setStatsUsageService(statsUsageService);
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	List<Address> addressList = new ArrayList<Address>() {
	    {
		Address address = new Address();
		address.setCity("city");
		add(address);
		
	    }
	};
	AddressResultsDto addressresults = new AddressResultsDto(addressList, 3L);
	statsUsageService.increaseUsage(StatsUsageType.GEOCODING);
	EasyMock.replay(statsUsageService);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andStubReturn(addressresults);
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	Address address = new Address();
	address.setCity("city");
	geocodingService.setImporterConfig(new ImporterConfig());
	geocodingService.geocode(address, "fr");
	EasyMock.verify(statsUsageService);
    }
    
    
    @Test
    public void testParsedAddressShouldBeSetForGeocodeAddress_city() {
	GeocodingService geocodingService = new GeocodingService() {
		 @Override
		    protected List<SolrResponseDto> findExactMatches(String text,
		    		String countryCode,boolean fuzzy, Point point, Double radius, Class[] placetypes) {
		return new ArrayList<SolrResponseDto>();
	    }
	    
	    @Override
	    protected List<SolrResponseDto> findStreetInText(String text, String countryCode, Point point, boolean fuzzy, Double radius) {
		return new ArrayList<SolrResponseDto>();
	    }
	};
	geocodingService.setStatsUsageService(statsUsageService);
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	List<Address> addressList = new ArrayList<Address>() {
	    {
		Address address = new Address();
		address.setCity("city");
		add(address);
		
	    }
	};
	AddressResultsDto addressresults = new AddressResultsDto(addressList, 3L);
	statsUsageService.increaseUsage(StatsUsageType.GEOCODING);
	EasyMock.replay(statsUsageService);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andStubReturn(addressresults);
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	Address address = new Address();
	address.setCity("city");
	geocodingService.setImporterConfig(new ImporterConfig());
	AddressResultsDto addressResultsDto = geocodingService.geocode(address, "fr");
	//Assert.assertEquals("parsed address should be filled with the providedone ",address, addressResultsDto.getParsedAddress());
	EasyMock.verify(statsUsageService);
    }
    
   /* @Test
    public void testParsedAddressShouldBeSetForGeocodeAddress_OnlyStreet() {
	findStreetCalled = false;
	GeocodingService geocodingService = new GeocodingService() {

	    @Override
	    protected java.util.List<SolrResponseDto> findStreetInText(String text, String countryCode, Point point, boolean fuzzy, Double radius) {
		findStreetCalled = true;
		return null;
	    }
	};
	geocodingService.setStatsUsageService(statsUsageService);
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andStubReturn(null);
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	Address address = new Address();
	address.setStreetName("foo");

	AddressResultsDto addressResultsDto = geocodingService.geocode(address, "ac");
	Assert.assertEquals("parsed address should be null when a structured address is provided",null, addressResultsDto.getParsedAddress());
	Assert.assertTrue(findStreetCalled);
    }*/
    
   /* @Test
    public void getBestCitySearchSentence(){
	GeocodingService geocodingService = new GeocodingService();
	
	//only city
	Address address = new Address();
	address.setCity("city with space");
	String sentence = geocodingService.getBestCitySearchSentence(address);
	Assert.assertEquals("city with space",sentence);
	
	//only postTown
	address = new Address();
	address.setPostTown("postTown with space");
	sentence = geocodingService.getBestCitySearchSentence(address);
	Assert.assertEquals("postTown with space",sentence);
	
	//postTown and city
	address = new Address();
	address.setPostTown("postTown with space");
	address.setCity("city with space");
	sentence = geocodingService.getBestCitySearchSentence(address);
	Assert.assertEquals("city with space",sentence);
		
	
	//only zip
	address = new Address();
	address.setZipCode("75002");
	sentence = geocodingService.getBestCitySearchSentence(address);
	Assert.assertEquals("75002",sentence);
	
	//zip and city
	address = new Address();
	address.setCity("city with space");
	address.setZipCode("75002");
	sentence = geocodingService.getBestCitySearchSentence(address);
	Assert.assertEquals("city with space 75002",sentence);
	//---------------
	//dependent locality only
	address = new Address();
	address.setDependentLocality("dep loc");
	sentence = geocodingService.getBestCitySearchSentence(address);
	Assert.assertEquals("dep loc",sentence);
	
	//dependent locality and zip
	address = new Address();
	address.setDependentLocality("dep loc");
	address.setZipCode("75002");
	sentence = geocodingService.getBestCitySearchSentence(address);
	Assert.assertEquals("75002 dep loc",sentence);
	
	//dependent locality and state
	address = new Address();
	address.setDependentLocality("dep loc");
	address.setState("state");
	sentence = geocodingService.getBestCitySearchSentence(address);
	Assert.assertEquals("state dep loc",sentence);
	
	//dependent locality and state and zip
	address = new Address();
	address.setDependentLocality("dep loc");
	address.setState("state");
	address.setZipCode("75002");
	sentence = geocodingService.getBestCitySearchSentence(address);
	Assert.assertEquals("75002 state dep loc",sentence);
	
	//zip and state
	address = new Address();
	address.setState("state");
	address.setZipCode("75002");
	sentence = geocodingService.getBestCitySearchSentence(address);
	Assert.assertEquals("75002 state",sentence);
	
    }*/
    
   /* @Test
    public void mergeSolrResponseDto(){
    	GeocodingService geocodingService = new GeocodingService();
    	Assert.assertTrue(geocodingService.mergeSolrResponseDto(null, null).size()==0);
    	Assert.assertTrue(geocodingService.mergeSolrResponseDto(null, new ArrayList<SolrResponseDto>()).size()==0);
    	Assert.assertTrue(geocodingService.mergeSolrResponseDto(new ArrayList<SolrResponseDto>(), null).size()==0);
    	Assert.assertTrue(geocodingService.mergeSolrResponseDto(new ArrayList<SolrResponseDto>(), new ArrayList<SolrResponseDto>()).size()==0);
    	String is_in="city";
		SolrResponseDto street = GisgraphyTestHelper.createSolrResponseDtoForStreet(is_in);
		SolrResponseDto city = GisgraphyTestHelper.createSolrResponseDtoForCity();
		SolrResponseDto city_other = GisgraphyTestHelper.createSolrResponseDtoForCity_other();
		
		
		List<SolrResponseDto> list1 = new ArrayList<SolrResponseDto>();
		list1.add(street);
		List<SolrResponseDto> list2 = new ArrayList<SolrResponseDto>();
		list2.add(city);
		list2.add(null);
		list2.add(street);
		list2.add(city_other);
		
		//aproximative size greater than exact
		List<SolrResponseDto> mergeSolrResponseDto =geocodingService.mergeSolrResponseDto(list1, list2);
		Assert.assertTrue(mergeSolrResponseDto.size()==3);
		Assert.assertEquals(street,mergeSolrResponseDto.get(0));
		Assert.assertEquals(city,mergeSolrResponseDto.get(1));
		Assert.assertEquals(city_other,mergeSolrResponseDto.get(2));
		
		//exact matche size greater than aproximative
		mergeSolrResponseDto = geocodingService.mergeSolrResponseDto(list2, list1);
		Assert.assertTrue(mergeSolrResponseDto.size()==3);
		Assert.assertEquals(city,mergeSolrResponseDto.get(0));
		Assert.assertEquals(street,mergeSolrResponseDto.get(1));
		Assert.assertEquals(city_other,mergeSolrResponseDto.get(2));
		
    }*/

    @Test
    public void isGeocodable(){
    	GeocodingService service = new GeocodingService();
    	Address address = new Address();
    	address.setCity("city");
    	Assert.assertTrue(service.isGeocodable(address));
    	
    	address = new Address();
    	address.setZipCode("zipCode");
    	Assert.assertTrue(service.isGeocodable(address));
    	
    	address = new Address();
    	address.setStreetName("streetName");
    	Assert.assertTrue(service.isGeocodable(address));
    	
    	address = new Address();
    	address.setPostTown("postTown");
    	Assert.assertTrue(service.isGeocodable(address));
    	
    	address = new Address();
    	address.setCitySubdivision("citySubdivision");
    	Assert.assertTrue(service.isGeocodable(address));
    	
    	address = new Address();
    	Assert.assertFalse(service.isGeocodable(address));
    	
    }
    
 /*  _                   _                      _ 
 ___| |_ _ __ _   _  ___| |_ _   _ _ __ ___  __| |
/ __| __| '__| | | |/ __| __| | | | '__/ _ \/ _` |
\__ \ |_| |  | |_| | (__| |_| |_| | | |  __/ (_| |
|___/\__|_|   \__,_|\___|\__|\__,_|_|  \___|\__,_|
                                                 
*/
    
    @Test(expected = IllegalArgumentException.class)
    public void geocodeStructuredAdressShouldThrowIfAddressIsNull() {
	IGeocodingService geocodingService = new GeocodingService();
	Address address = null;
	AddressQuery query = new StructuredAddressQuery(address, "US");
	geocodingService.geocode(query);
    }

  
    @Test(expected = GeocodingException.class)
    public void geocodeStructuredAdressShouldThrowIfCountryCodeIsNull() {
	IGeocodingService geocodingService = new GeocodingService();
	String countrycode = null;
	AddressQuery query = new StructuredAddressQuery(new Address(), countrycode);
	geocodingService.geocode(query);
    }

    @Test(expected = GeocodingException.class)
    public void geocodeStructuredAdressShouldThrowIfCountryCodeHasenTALengthOf2() {
	GeocodingService geocodingService = new GeocodingService();
	geocodingService.setStatsUsageService(statsUsageService);
	AddressQuery query = new StructuredAddressQuery(new Address(), "abc");
	geocodingService.geocode(query);
    }

    @Test
    public void geocodeStructuredAdressShouldCallGeocodeAddressIfParsedAddressIsSuccess() {
	GeocodeAdressCalled = false;
	GeocodingService geocodingService = new GeocodingService() {
	    @Override
	    public AddressResultsDto geocode(Address address, String countryCode) throws GeocodingException {
		GeocodeAdressCalled = true;
		return null;
	    }
	};
	ImporterConfig importerConfig = EasyMock.createMock(ImporterConfig.class);
	EasyMock.expect(importerConfig.isOpenStreetMapFillIsIn()).andStubReturn(true);
	geocodingService.setImporterConfig(importerConfig);
	geocodingService.setStatsUsageService(statsUsageService);
	geocodingService.setGisgraphyConfig(gisgraphyConfig);
	gisgraphyConfig.setUseAddressParserWhenGeocoding(true);
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	List<Address> addressList = new ArrayList<Address>() {
	    {
	    	Address address = new Address();
			address.setStreetName("streetName");
			address.setCity("city");
			add(address);
	    }
	};
	AddressResultsDto addressresults = new AddressResultsDto(addressList, 3L);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andReturn(addressresults);
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	AddressQuery query = new StructuredAddressQuery(new Address(), "ac");
	geocodingService.geocode(query);
	Assert.assertTrue(GeocodeAdressCalled);
    }
    
    
    

  /*          _     _                   
	  __ _  __| | __| |_ __ ___  ___ ___ 
	 / _` |/ _` |/ _` | '__/ _ \/ __/ __|
	| (_| | (_| | (_| | | |  __/\__ \__ \
	 \__,_|\__,_|\__,_|_|  \___||___/___/
	                                     

   * 
   */

    @Test
    public void geocodeAdressShouldCallFindStreetInTextIfStreetNameIsNotNull() {
	findStreetCalled = false;
	GeocodingService geocodingService = new GeocodingService() {

		
	    @Override
	    protected java.util.List<SolrResponseDto> findStreetInText(String text, String countryCode, Point point, boolean fuzzy, Double radius) {
	    	Assert.assertEquals("foo", text);
		findStreetCalled = true;
		return null;
	    }
	};
	geocodingService.setStatsUsageService(statsUsageService);
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andStubReturn(null);
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	Address address = new Address();
	address.setStreetName("foo");

	geocodingService.geocode(address, "ac");
	Assert.assertTrue(findStreetCalled);
    }
    
    @Test
    public void geocodeAdressShouldNotCallFindStreetInTextIfStreetNameIsnull() {
	findStreetCalled = false;
	findCitiesCalled = false;
	GeocodingService geocodingService = new GeocodingService() {

		 @Override
		    protected List<SolrResponseDto> findExactMatches(String text,
		    		String countryCode,boolean fuzzy, Point point, Double radius, Class[] placetypes) {
		findCitiesCalled = true;
		return null;
	    }
	    @Override
	    protected java.util.List<SolrResponseDto> findStreetInText(String text, String countryCode, Point point, boolean fuzzy, Double radius) {
	    	Assert.assertEquals("foo", text);
		findStreetCalled = true;
		return null;
	    }
	};
	geocodingService.setStatsUsageService(statsUsageService);
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andStubReturn(null);
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	Address address = new Address();
	address.setStreetName("foo");

	geocodingService.geocode(address, "ac");
	Assert.assertFalse(findCitiesCalled);
	Assert.assertTrue(findStreetCalled);
    }
    
  

   
    @Test
    public void geocodeAdressShouldCallFindCityInTextIfStreetIsNull_cityFound() {
	String city = "city";
	findStreetCalled = false;
	findCitiesCalled = false;
	populatecalled = false;
	final SolrResponseDto cityResult = EasyMock.createMock(SolrResponseDto.class);
	final Double latitude = 2.1d;
	EasyMock.expect(cityResult.getLat()).andStubReturn(latitude);
	EasyMock.expect(cityResult.getLat_admin_centre()).andStubReturn(latitude+2);
	final Double longitude = 5.2d;
	EasyMock.expect(cityResult.getLng()).andStubReturn(longitude);
	EasyMock.expect(cityResult.getLng_admin_centre()).andStubReturn(longitude+2);
	EasyMock.expect(cityResult.getOpenstreetmap_id()).andStubReturn(888888L);
	EasyMock.expect(cityResult.getName()).andStubReturn("paris");
	EasyMock.expect(cityResult.getAdm2_name()).andStubReturn("ile de france");
	EasyMock.expect(cityResult.getAdm1_name()).andStubReturn("paris");
	EasyMock.expect(cityResult.getAdm3_name()).andStubReturn("adm3 name");
	EasyMock.expect(cityResult.getAdm4_name()).andStubReturn("adm4 name");
	EasyMock.expect(cityResult.getAdm5_name()).andStubReturn("adm5 name");
	List<String> alternateNames= new ArrayList<String>();
	alternateNames.add("paris alternate");
	EasyMock.expect(cityResult.getName_alternates()).andStubReturn(alternateNames);
	EasyMock.expect(cityResult.getIs_in()).andStubReturn("is in");
	EasyMock.expect(cityResult.getStreet_type()).andStubReturn(null);
	EasyMock.expect(cityResult.getZipcodes()).andStubReturn(null);
	EasyMock.expect(cityResult.getIs_in_zip()).andStubReturn(null);
	EasyMock.expect(cityResult.getCountry_code()).andStubReturn("FR");
	EasyMock.expect(cityResult.getScore()).andStubReturn(45.3F);
	EasyMock.expect(cityResult.getFully_qualified_name()).andStubReturn("FQDN");
	EasyMock.expect(cityResult.getFeature_id()).andStubReturn(123L);
	EasyMock.expect(cityResult.getPlacetype()).andStubReturn("City");
	Set<String> zips = new HashSet<String>();
	zips.add("zip");
	EasyMock.expect(cityResult.getZipcodes()).andStubReturn(zips);
	EasyMock.expect(cityResult.getIs_in_adm()).andStubReturn("isinAdm");
	EasyMock.expect(cityResult.getIs_in_place()).andStubReturn("isinPlace");
	EasyMock.replay(cityResult);
	GeocodingService geocodingService = new GeocodingService() {


	    @Override
	    protected List<SolrResponseDto> findExactMatches(String text,
	    		String countryCode,boolean fuzzy, Point point, Double radius, Class[] placetypes) {
		findCitiesCalled = true;
		List<SolrResponseDto> cities = new ArrayList<SolrResponseDto>();
		cities.add(cityResult);
		return cities;
	    }

	    @Override
	    protected List<SolrResponseDto> findStreetInText(String text, String countryCode, Point point, boolean fuzzy, Double radius) {
		Point checkedPoint = GeolocHelper.createPoint(longitude.floatValue(), latitude.floatValue());
		if (point.getX() != checkedPoint.getX() || point.getY() != checkedPoint.getY()) {
		    Assert.fail("when city is found we shoud search with location restriction");
		}
		findStreetCalled = true;
		return null;
	    }
	};
	geocodingService.setStatsUsageService(statsUsageService);
	geocodingService.setImporterConfig(new ImporterConfig());
	IAddressParserService mockAddressParserService = EasyMock.createMock(IAddressParserService.class);
	EasyMock.expect(mockAddressParserService.execute((AddressQuery) EasyMock.anyObject())).andStubReturn(null);
	EasyMock.replay(mockAddressParserService);
	geocodingService.setAddressParser(mockAddressParserService);
	Address address = new Address();
	address.setCity(city);

	geocodingService.geocode(address, "ac");
	Assert.assertFalse(findStreetCalled);
	Assert.assertTrue(findCitiesCalled);
    }

    @Test(expected = GeocodingException.class)
    public void geocodeAddressShouldThrowIfAddressIsNull() {
	IGeocodingService geocodingService = new GeocodingService();
	Address address = null;
	geocodingService.geocode(address, "DE");
    }

    @Test(expected = GeocodingException.class)
    public void geocodeAddressShouldThrowIfStreetNameCityAndZipAreNull() {
	GeocodingService geocodingService = new GeocodingService();
	geocodingService.setStatsUsageService(statsUsageService);
	Address address = new Address();
	geocodingService.geocode(address, "De");
    }

    @Test(expected = GeocodingException.class)
    public void geocodeAddressShouldThrowIfStreetIntersection() {
	GeocodingService geocodingService = new GeocodingService();
	geocodingService.setStatsUsageService(statsUsageService);
	Address address = new Address();
	address.setStreetNameIntersection("intersection");
	geocodingService.geocode(address, "De");
    }

    @Test(expected = GeocodingException.class)
    public void geocodeAddressShouldNotThrowIfCountryCodeIsNull() {
	IGeocodingService geocodingService = new GeocodingService();
	Address address = new Address();
	geocodingService.geocode(address, null);
    }
    
    @Test(expected = GeocodingException.class)
    public void geocodeAddressShouldThrowIfCountryCodeHasOneLetter() {
	IGeocodingService geocodingService = new GeocodingService();
	Address address = new Address();
	geocodingService.geocode(address, "a");
    }
    
    @Test(expected = GeocodingException.class)
    public void geocodeAddressShouldThrowIfCountryisEmpty() {
	IGeocodingService geocodingService = new GeocodingService();
	Address address = new Address();
	geocodingService.geocode(address, " ");
    }
    
    @Test(expected = GeocodingException.class)
    public void geocodeAddressWithToolessInformations_null() {
	IGeocodingService geocodingService = new GeocodingService();
	Address address = new Address();
	geocodingService.geocode(address, "FR");
    }
    
    @Test(expected = GeocodingException.class)
    public void geocodeAddressWithToolessInformations_emptyString() {
	IGeocodingService geocodingService = new GeocodingService();
	Address address = new Address();
	address.setCity("");
	address.setStreetName("");
	address.setZipCode("");
	geocodingService.geocode(address, "FR");
    }

    @Test(expected = GeocodingException.class)
    public void geocodeAdressShouldThrowIfCountryCodeHasenTALengthOf2() {
	IGeocodingService geocodingService = new GeocodingService();
	Address address = new Address();
	geocodingService.geocode(address, "abc");
    }

    @Test(expected = GeocodingException.class)
    public void testGeocodeWithNullQueryShouldThrows() {
	IGeocodingService geocodingService = new GeocodingService();
	geocodingService.geocode(null);
    }

    @Test(expected = GeocodingException.class)
    public void testGeocodeToStringWithNullQueryShouldThrows() {
	IGeocodingService geocodingService = new GeocodingService();
	geocodingService.geocodeToString(null);
	fail("executeQueryToString does not accept null query");
    }

    @Test(expected = GeocodingException.class)
    public void testGeocodeAndSerializeWithNullQueryShouldThrows() {
	IGeocodingService geocodingService = new GeocodingService();
	geocodingService.geocodeAndSerialize(null, new ByteArrayOutputStream());
	fail("executeAndSerialize does not accept null query");
    }

    @Test(expected = GeocodingException.class)
    public void testGeocodeAndSerializeWithNullOutputStreamShouldThrows() {
	IGeocodingService geocodingService = new GeocodingService();
	geocodingService.geocodeAndSerialize(new AddressQuery("address", "XX"), null);
	fail("executeAndSerialize does not accept null query");
    }

    @Test
    public void testGeocodeToStringShouldTakeTheCallbackParameterIntoAccount() {
	IGeocodingService geocodingService = new GeocodingService() {
	    @Override
	    public AddressResultsDto geocode(AddressQuery query) throws GeocodingException {
		return new AddressResultsDto();
	    }
	};
	AddressQuery addressQuery = new AddressQuery("paris", "fr");
	String callBackName = "doIt";
	addressQuery.setCallback(callBackName);
	addressQuery.setFormat(OutputFormat.JSON);
	String result = geocodingService.geocodeToString(addressQuery);
	Assert.assertTrue(result.startsWith(callBackName));
    }

    @Test
    public void testGeocodeToStringShouldCallGeocode() {
	geocodeIsCalled = false;
	IGeocodingService geocodingService = new GeocodingService() {
	    @Override
	    public AddressResultsDto geocode(AddressQuery query) throws GeocodingException {
		geocodeIsCalled = true;
		return new AddressResultsDto();
	    }
	};
	AddressQuery addressQuery = new AddressQuery("paris", "fr");
	geocodingService.geocodeToString(addressQuery);
	Assert.assertTrue(geocodeIsCalled);
    }

    @Test
    public void testGeocodeAndSerializeShouldCallGeocode() {
	geocodeIsCalled = false;
	IGeocodingService geocodingService = new GeocodingService() {
	    @Override
	    public AddressResultsDto geocode(AddressQuery query) throws GeocodingException {
		geocodeIsCalled = true;
		return new AddressResultsDto();
	    }
	};
	AddressQuery addressQuery = new AddressQuery("paris", "fr");
	geocodingService.geocodeAndSerialize(addressQuery, new ByteArrayOutputStream());
	Assert.assertTrue(geocodeIsCalled);
    }
    
   
   
    
    @Test
    public void needParsing(){
    	GeocodingService geocodingService = new GeocodingService();
    	Assert.assertFalse(geocodingService.needParsing(""));
    	Assert.assertFalse(geocodingService.needParsing(null));
    	Assert.assertFalse(geocodingService.needParsing(" "));
    	Assert.assertFalse(geocodingService.needParsing(" toto "));
    	Assert.assertFalse(geocodingService.needParsing(" to-to "));
    	Assert.assertTrue(geocodingService.needParsing(" toto toto "));
    	Assert.assertTrue(geocodingService.needParsing("toto,toto"));
    	Assert.assertTrue(geocodingService.needParsing("toto;toto"));
    }
    
    @Test
    public void setStateInAddress_ForFrance(){
    	GeocodingService geocodingService = new GeocodingService();
    	SolrResponseDto solrResponseDto = EasyMock.createMock(SolrResponseDto.class);
    	EasyMock.expect(solrResponseDto.getCountry_code()).andStubReturn("FR");
    	EasyMock.expect(solrResponseDto.getAdm2_name()).andStubReturn("adm2");
    	EasyMock.expect(solrResponseDto.getIs_in_adm()).andStubReturn("isinadm");
    	EasyMock.replay(solrResponseDto);
		Address address = new Address();
		geocodingService.setStateInAddress(solrResponseDto, address );
		Assert.assertEquals("adm2", address.getState());
    	
    }
    
    @Test
    public void setStateInAddress(){
    	GeocodingService geocodingService = new GeocodingService();
    	SolrResponseDto solrResponseDto = EasyMock.createMock(SolrResponseDto.class);
    	EasyMock.expect(solrResponseDto.getCountry_code()).andStubReturn("MM");
    	EasyMock.expect(solrResponseDto.getAdm2_name()).andStubReturn("adm2");
    	EasyMock.expect(solrResponseDto.getIs_in_adm()).andStubReturn("isinadm");
    	EasyMock.replay(solrResponseDto);
		Address address = new Address();
		geocodingService.setStateInAddress(solrResponseDto, address );
		Assert.assertEquals("isinadm", address.getState());
    	
    }
    @Test
    public void setStateInAddress_nullcountrycode(){
    	GeocodingService geocodingService = new GeocodingService();
    	SolrResponseDto solrResponseDto = EasyMock.createMock(SolrResponseDto.class);
    	EasyMock.expect(solrResponseDto.getCountry_code()).andStubReturn(null);
    	EasyMock.expect(solrResponseDto.getAdm2_name()).andStubReturn("adm2");
    	EasyMock.expect(solrResponseDto.getIs_in_adm()).andStubReturn("isinadm");
    	EasyMock.replay(solrResponseDto);
		Address address = new Address();
		geocodingService.setStateInAddress(solrResponseDto, address );
		Assert.assertEquals("isinadm", address.getState());
    	
    }
    
    @Test
    public void testReplaceGermanSynonyms(){
        GeocodingService service = new GeocodingService();
        
        //no str
        Assert.assertEquals("foo",service.replaceGermanSynonyms("foo"));
        
        //one without point
        Assert.assertEquals("trucstraße",service.replaceGermanSynonyms("trucStr"));
        Assert.assertEquals("trucStrasse",service.replaceGermanSynonyms("trucStrasse"));
        Assert.assertEquals("truc Strasse",service.replaceGermanSynonyms("truc Strasse"));
    //  Assert.assertEquals("truc straße",service.replaceGermanSynonyms("truc Str"));
        
        //one with point
        Assert.assertEquals("trucstraße",service.replaceGermanSynonyms("trucStr."));
        Assert.assertEquals("trucStrasse.",service.replaceGermanSynonyms("trucStrasse."));
        Assert.assertEquals("truc Strasse.",service.replaceGermanSynonyms("truc Strasse."));
    //  Assert.assertEquals("truc straße",service.replaceGermanSynonyms("truc Str."));
        
        //one without point + other word
        Assert.assertEquals("foo trucstraße",service.replaceGermanSynonyms("foo trucStr"));
        Assert.assertEquals("foo trucStrasse",service.replaceGermanSynonyms("foo trucStrasse"));
        Assert.assertEquals("foo truc Strasse",service.replaceGermanSynonyms("foo truc Strasse"));
    //  Assert.assertEquals("foo truc straße",service.replaceGermanSynonyms("foo truc Str"));
        
        //one with point + other word
        Assert.assertEquals("foo trucstraße",service.replaceGermanSynonyms("foo trucStr."));
        Assert.assertEquals("foo trucStrasse.",service.replaceGermanSynonyms("foo trucStrasse."));
        Assert.assertEquals("foo truc Strasse.",service.replaceGermanSynonyms("foo truc Strasse."));
    //  Assert.assertEquals("foo truc straße",service.replaceGermanSynonyms("foo truc Str."));
        
        //two 
        //Assert.assertEquals("foo trucstraße foo trucstraße",service.replaceGermanSynonyms("foo trucStr. foo trucStr."));
        
    //  Assert.assertEquals("foo truc str",service.replaceGermanSynonyms("foo truc Str."));
    }
    
    @Test
    public void mergeExactAndFuzzy_AllNull(){
        GeocodingService service = new GeocodingService();
        AddressResultsDto results = null;
        AddressResultsDto resultsFuzzy =null;
        String newAddress=null;
        service.mergeExactAndFuzzy(results, newAddress, resultsFuzzy);
    }
    
    @Test
    public void mergeExactAndFuzzy_fuzzyEmpty_exactFilled(){
        GeocodingService service = new GeocodingService();
        List<Address> addresses = new ArrayList<Address>();
        Address address = new Address();
        address.setFormatedPostal("rue de la vallee");
        addresses.add(address );
        AddressResultsDto results = new AddressResultsDto(addresses ,2L);
        AddressResultsDto resultsFuzzy =new AddressResultsDto();
        String newAddress=null;
       AddressResultsDto result = service.mergeExactAndFuzzy(results, newAddress, resultsFuzzy);
       Assert.assertEquals(1, result.getResult().size());
    }
    
    @Test
    public void mergeExactAndFuzzy_fuzzyFilled_exactEmpty(){
        GeocodingService service = new GeocodingService();
        List<Address> addresses = new ArrayList<Address>();
        Address address = new Address();
        address.setFormatedPostal("rue de la vallee");
        addresses.add(address );
        AddressResultsDto resultsFuzzy = new AddressResultsDto(addresses ,2L);
        AddressResultsDto results =new AddressResultsDto();
        String newAddress=null;
       AddressResultsDto result = service.mergeExactAndFuzzy(results, newAddress, resultsFuzzy);
       Assert.assertEquals(1, result.getResult().size());
    }
    
    @Test
    public void mergeExactAndFuzzy_fuzzyFilled_exactFilled_addressnull(){
        GeocodingService service = new GeocodingService();
        List<Address> addresses = new ArrayList<Address>();
        List<Address> addressesFuzzy = new ArrayList<Address>();
        Address address = new Address();
        address.setFormatedPostal("rue de la vallee");
        addresses.add(address );
        
        Address addressFuzzy = new Address();
        addressFuzzy.setFormatedPostal("rue de la vallee verte");
        addressesFuzzy.add(addressFuzzy );
        
        AddressResultsDto results = new AddressResultsDto(addresses ,2L);
        AddressResultsDto resultsFuzzy =new AddressResultsDto(addressesFuzzy,3L);
        String newAddress=null;
       AddressResultsDto result = service.mergeExactAndFuzzy(results, newAddress, resultsFuzzy);
       Assert.assertEquals(2, result.getResult().size());
       Assert.assertEquals("exact match should be first","rue de la vallee", result.getResult().get(0).getFormatedPostal());
    }
    
    @Test
    public void mergeExactAndFuzzy_fuzzyFilled_exactFilled_exactBetter(){
        GeocodingService service = new GeocodingService();
        List<Address> addresses = new ArrayList<Address>();
        List<Address> addressesFuzzy = new ArrayList<Address>();
        Address address = new Address();
        address.setFormatedPostal("rue de la vallee");
        addresses.add(address );
        
        Address addressFuzzy = new Address();
        addressFuzzy.setFormatedPostal("rue de la vallee verte");
        addressesFuzzy.add(addressFuzzy );
        
        AddressResultsDto results = new AddressResultsDto(addresses ,2L);
        AddressResultsDto resultsFuzzy =new AddressResultsDto(addressesFuzzy,3L);
        String newAddress="rue de la vallee";
       AddressResultsDto result = service.mergeExactAndFuzzy(results, newAddress, resultsFuzzy);
       Assert.assertEquals(2, result.getResult().size());
       Assert.assertEquals("exact match should be first","rue de la vallee", result.getResult().get(0).getFormatedPostal());
    }
    
    @Test
    public void mergeExactAndFuzzy_fuzzyFilled_exactFilled_FuzzyBetter(){
        GeocodingService service = new GeocodingService();
        List<Address> addresses = new ArrayList<Address>();
        List<Address> addressesFuzzy = new ArrayList<Address>();
        Address address = new Address();
        address.setFormatedPostal("rue de la vallee");
        addresses.add(address );
        
        Address addressFuzzy = new Address();
        addressFuzzy.setFormatedPostal("rue de la vallee verte");
        addressesFuzzy.add(addressFuzzy );
        
        AddressResultsDto results = new AddressResultsDto(addresses ,2L);
        AddressResultsDto resultsFuzzy =new AddressResultsDto(addressesFuzzy,3L);
        String newAddress="rue de la vallee verte";
       AddressResultsDto result = service.mergeExactAndFuzzy(results, newAddress, resultsFuzzy);
       Assert.assertEquals(2, result.getResult().size());
       Assert.assertEquals("fuzzy match should be first","rue de la vallee verte", result.getResult().get(0).getFormatedPostal());
    }
    
    @Test
    public void  getNumberOfDigit(){
        GeocodingService service = new GeocodingService();
        Assert.assertEquals(1,service.countDigitOrCP(service.splitDigitOrCP("59 310 coutiches")));
        Assert.assertEquals(1,service.countDigitOrCP(service.splitDigitOrCP("59310 coutiches")));
        Assert.assertEquals(1,service.countDigitOrCP(service.splitDigitOrCP("59-310 coutiches")));
        Assert.assertEquals(1,service.countDigitOrCP(service.splitDigitOrCP("59310 coutiches")));
        
        
        Assert.assertEquals(1,service.countDigitOrCP(service.splitDigitOrCP("59 310,coutiches")));
        Assert.assertEquals(1,service.countDigitOrCP(service.splitDigitOrCP("59310,coutiches")));
        Assert.assertEquals(1,service.countDigitOrCP(service.splitDigitOrCP("59-310,coutiches")));
        Assert.assertEquals(1,service.countDigitOrCP(service.splitDigitOrCP("59310,coutiches")));
        
        Assert.assertEquals(2,service.countDigitOrCP(service.splitDigitOrCP(" Kraków,30-036 25")));
        Assert.assertEquals(2,service.countDigitOrCP(service.splitDigitOrCP(" Kraków,  30-036 25")));
        
       
    }
    

   
}
