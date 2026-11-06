package com.omnicart.model;

public class ShippingAddress {
    private String name;
    private String email;
    private String phone;
    private String address;
    private String city;

    public ShippingAddress() {}

    public ShippingAddress(String name, String email, String phone, String address, String city) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public boolean isValid() {
        return name != null && !name.isBlank()
                && email != null && email.contains("@")
                && phone != null && !phone.isBlank()
                && address != null && !address.isBlank()
                && city != null && !city.isBlank();
    }
}
