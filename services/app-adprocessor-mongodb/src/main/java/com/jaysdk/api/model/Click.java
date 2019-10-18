package com.jaysdk.api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "clicks")
public class Click {

	@Id
    String id;
    String country;
    String partner;
    String city;
    String campaign;
    String ip;
    
    public Click()
    {
    	
    }

    public Click(String country, String partner, String city, String campaign, String ip) {
        this.country = country;
        this.partner = partner;
        this.city = city;
        this.campaign = campaign;
        this.ip = ip;
    }

    
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPartner() {
		return partner;
	}

	public void setPartner(String partner) {
		this.partner = partner;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCampaign() {
		return campaign;
	}

	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	
	
}
