package com.smartshehar.dashboard.app;


public class COfferCategory {

    String offer_id;
    String offer_category_id;
    String vendor_id;
    String offers;
    String offer_category_description;
    String offer_category_name;

    public COfferCategory(String offer_id, String offer_category_id,
                          String vendor_id, String offers, String offer_category_description,
                          String offer_category_name) {
        this.offer_id = offer_id;
        this.offer_category_id = offer_category_id;
        this.vendor_id = vendor_id;
        this.offers = offers;
        this.offer_category_description = offer_category_description;
        this.offer_category_name = offer_category_name;
    }

    public String getOffer_id() {
        return offer_id;
    }

    public void setOffer_id(String offer_id) {
        this.offer_id = offer_id;
    }

    public String getOffer_category_id() {
        return offer_category_id;
    }

    public void setOffer_category_id(String offer_category_id) {
        this.offer_category_id = offer_category_id;
    }

    public String getVendor_id() {
        return vendor_id;
    }

    public void setVendor_id(String vendor_id) {
        this.vendor_id = vendor_id;
    }

    public String getOffers() {
        return offers;
    }

    public void setOffers(String offers) {
        this.offers = offers;
    }

    public String getOffer_category_description() {
        return offer_category_description;
    }

    public void setOffer_category_description(String offer_category_description) {
        this.offer_category_description = offer_category_description;
    }

    public String getOffer_category_name() {
        return offer_category_name;
    }

    public void setOffer_category_name(String offer_category_name) {
        this.offer_category_name = offer_category_name;
    }
}
