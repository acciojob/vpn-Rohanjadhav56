package com.driver.services.impl;

import com.driver.exception.CountryNotFoundException;
import com.driver.model.Admin;
import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.repository.AdminRepository;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    AdminRepository adminRepository1;

    @Autowired
    ServiceProviderRepository serviceProviderRepository1;

    @Autowired
    CountryRepository countryRepository1;

    @Override
    public Admin register(String username, String password) {
      Admin admin = new Admin();
      admin.setUsername(username);
      admin.setPassword(password);
      admin.setServiceProviders(new ArrayList<>());

      Admin savedAdmin = adminRepository1.save(admin);
      return savedAdmin;
    }

    @Override
    public Admin addServiceProvider(int adminId, String providerName) {
        Optional<Admin> optionalAdmin = adminRepository1.findById(adminId);
        if(!optionalAdmin.isPresent()) return null;

        Admin admin = optionalAdmin.get();

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setName(providerName);
        serviceProvider.setAdmin(admin);
        serviceProvider.setCountryList(new ArrayList<>());
        serviceProvider.setUsers(new ArrayList<>());
        serviceProvider.setConnectionList(new ArrayList<>());

        ServiceProvider serviceProvider1 = serviceProviderRepository1.save(serviceProvider);
        admin.getServiceProviders().add(serviceProvider1);

        return adminRepository1.save(admin);


    }

    @Override
    public ServiceProvider addCountry(int serviceProviderId, String countryName) throws Exception{
        Optional<CountryName> countryName1 = CountryName.byNameIgnoreCase(countryName);
        if(!countryName1.isPresent()) throw new CountryNotFoundException("Country not found");

        CountryName countryName2 = countryName1.get();
        Optional<ServiceProvider> serviceProvider = serviceProviderRepository1.findById(serviceProviderId);
        Country country = new Country();
        country.setCountryName(countryName2);
        country.setCode(countryName2.toCode());
        country.setUser(null);
        country.setServiceProvider(serviceProvider.get());
        serviceProvider.get().getCountryList().add(country);

        return serviceProviderRepository1.save(serviceProvider.get());


      }
}
