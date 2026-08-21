package com.ecommerce.identity.application.mapper;

import com.ecommerce.identity.application.dto.AddressResponse;
import com.ecommerce.identity.application.dto.CustomerResponse;
import com.ecommerce.identity.domain.model.Customer;
import com.ecommerce.identity.domain.model.CustomerAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "createdAt", source = "createdAt")
    CustomerResponse toResponse(Customer customer);

    @Mapping(target = "addressLine1", source = "address.addressLine1")
    @Mapping(target = "addressLine2", source = "address.addressLine2")
    @Mapping(target = "city",         source = "address.city")
    @Mapping(target = "state",        source = "address.state")
    @Mapping(target = "postalCode",   source = "address.postalCode")
    @Mapping(target = "country",      source = "address.country")
    AddressResponse toAddressResponse(CustomerAddress customerAddress);
}
