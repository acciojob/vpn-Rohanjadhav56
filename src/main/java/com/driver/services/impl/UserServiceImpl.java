package com.driver.services.impl;

import com.driver.exception.CountryNotFoundException;
import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.model.User;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository3;
    @Autowired
    ServiceProviderRepository serviceProviderRepository3;
    @Autowired
    CountryRepository countryRepository3;

    @Override
    public User register(String username, String password, String countryName) throws Exception{
        Optional<CountryName> countryName1 = CountryName.byNameIgnoreCase(countryName);
        if(!countryName1.isPresent()) throw new CountryNotFoundException("Country not found");

        CountryName countryName2 = countryName1.get();
        Country country = new Country();
        country.setCountryName(countryName2);
        country.setCode(countryName2.toCode());
        country.setUser(null);
        country.setServiceProvider(null);
        Country country1 = countryRepository3.save(country);

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setConnectionList(new ArrayList<>());
        user.setServiceProviderList(new ArrayList<>());
        user.setConnected(false);
        user.setMaskedIp(null);

        user.setOriginalCountry(country1);

        userRepository3.save(user);
        user.setOriginalIp(country1.getCountryName().toCode()+"."+user.getId());
        return  user;




    }

    @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {
      Optional<User> optionalUser = userRepository3.findById(userId);
      if (!optionalUser.isPresent()) return null;
      User user = optionalUser.get();

      Optional<ServiceProvider> optionalServiceProvider = serviceProviderRepository3.findById(serviceProviderId);
      if(!optionalServiceProvider.isPresent()) return user;
      ServiceProvider serviceProvider = optionalServiceProvider.get();

      serviceProvider.getUsers().add(user);
      user.getServiceProviderList().add(serviceProvider);

      return  userRepository3.save(user);



    }
}
