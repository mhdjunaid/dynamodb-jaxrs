package com.jaysdk.api.controller;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jaysdk.api.model.Click;
import com.jaysdk.api.repo.ClickRepo;

@RestController
public class ClicksController {

	@Autowired
	ClickRepo clickRepository;

	@RequestMapping(method = RequestMethod.GET, value = "/clicks")
	@ResponseBody
	public ResponseEntity getWithPartnerIdAndClickIdAsQueryParams(@RequestHeader MultiValueMap<String, String> headers,
			@Size(min = 1) @NotNull @RequestParam("partner-id") String partnerId,
			@Size(min = 1) @NotNull @RequestParam("click-id") String clickId,
			@Size(min = 1) @NotNull @RequestParam("package-name") String packageName,
			@Size(min = 1) @NotNull @RequestParam("publisher-id") String publisherId,
			@RequestParam("campaign-id") String campaignId,
			@Size(min = 1) @NotNull @RequestParam("redirect-uri") String redirectUri,
			@Size(min = 1) @NotNull @RequestHeader("1sdk-rt-received-at") String receivedAt) {

		Click click = new Click();
		click.setCampaign(packageName);
		click.setCountry(publisherId);
		click.setIp(partnerId);
		click.setPartner(partnerId);
		clickRepository.save(click);
		return new ResponseEntity(HttpStatus.CREATED);

	}
	
	
	@GetMapping("/hello")
    public Collection<String> sayHello() {
        return IntStream.range(0, 10)
          .mapToObj(i -> "Hello number " + i)
          .collect(Collectors.toList());
    
}
	
}
