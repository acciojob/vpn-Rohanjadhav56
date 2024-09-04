package com.driver.services.impl;

import com.driver.exception.CountryNotFoundException;
import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;



    @Override
    public User connect(int userId, String countryName) throws Exception{
        Optional<User> optionalUser = userRepository2.findById(userId);
        if(!optionalUser.isPresent())throw  new Exception("User Not Found");

        User user = optionalUser.get();
        if(user.getConnected()) throw new Exception("Already connected");

        Optional<CountryName> countryName1 = CountryName.byNameIgnoreCase(countryName);
        if(!countryName1.isPresent()) throw new CountryNotFoundException("Country not found");

        CountryName countryName2 = countryName1.get();
       if (user.getOriginalCountry().getCountryName().equals(countryName2)) return user;
       if(user.getServiceProviderList()==null) throw new Exception("Unable to connect");

       ServiceProvider serviceProvider = null;
       int id = Integer.MAX_VALUE;
       for (ServiceProvider s : user.getServiceProviderList())
       {
           if(s.getCountryList()==null) {
               continue;
           }
           else{
               for(Country c : s.getCountryList())
               {
                   if(c.getCountryName().equals(countryName2) && s.getId()<id)
                   {
                       id = s.getId();
                       serviceProvider = s;
                   }
               }
           }
       }

       if(id==Integer.MAX_VALUE)
       {
           throw new Exception("Unable to connect");
       }

        Connection connection = new Connection();
        connection.setUser(user);
        connection.setServiceProvider(serviceProvider);


        user.setConnected(true);
        user.setMaskedIp(countryName2.toCode()+"."+ id +"."+userId);
        user.getConnectionList().add(connection);

        serviceProvider.getConnectionList().add(connection);
        //connectionRepository2.save(connection);
        serviceProviderRepository2.save(serviceProvider);
        userRepository2.save(user);





        return user;

    }
    @Override
    public User disconnect(int userId) throws Exception {
        Optional<User> optionalUser = userRepository2.findById(userId);
        if (!optionalUser.isPresent()) throw new Exception("user not found");

        User user = optionalUser.get();
        if(!user.getConnected()) throw  new Exception("Already disconnected");

        List<Connection> connections = user.getConnectionList();
        if(connections==null) return  user;
        for (Connection c : connections)
        {
            c.setUser(null);
            c.setServiceProvider(null);
            connectionRepository2.delete(c);
        }

        user.setServiceProviderList(new ArrayList<>());
        user.setMaskedIp(null);
        user.setConnected(false);
        user.setConnectionList(new ArrayList<>());
        userRepository2.save(user);

        return user;

    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
       Optional<User> optionalUser = userRepository2.findById(receiverId);
       if(!optionalUser.isPresent()) throw new Exception("Cannot establish communication");

        Optional<User> optionalUser1 = userRepository2.findById(senderId);
        if(!optionalUser1.isPresent()) throw new Exception("Cannot establish communication");


        User receiver = optionalUser.get();
        User sender = optionalUser1.get();
        Country country = null;
        int serveId = Integer.MAX_VALUE;
       if(!receiver.getConnected()){
           Country rCountry = receiver.getOriginalCountry();
           if(sender.getOriginalCountry().equals(rCountry)) return sender;
           else{
               List<ServiceProvider> serviceProviders = sender.getServiceProviderList();
               for (ServiceProvider s : serviceProviders)
               {
                   for (Country c : s.getCountryList())
                   {
                       if(c.equals(rCountry))
                       {
                           if(serveId< s.getId())
                           {
                               serveId = s.getId();
                               country = c;
                           }
                       }
                   }
               }
           }

       }
       else {
           List<Connection> connections = receiver.getConnectionList();
           for (Connection c : connections)
           {
               ServiceProvider s = c.getServiceProvider();
               for (Country rCountry : s.getCountryList())
               {
                   List<ServiceProvider> serviceProviders = sender.getServiceProviderList();
                   for (ServiceProvider s1 : serviceProviders)
                   {
                       for (Country c1 : s.getCountryList())
                       {
                           if(c.equals(rCountry))
                           {
                               if(serveId< s1.getId())
                               {
                                   serveId = s1.getId();
                                   country = c1;
                               }
                           }
                       }
                   }
               }
           }
       }
       if(serveId==Integer.MAX_VALUE || country==null) throw new Exception("Cannot establish communication");

       sender.setConnected(true);
       sender.setMaskedIp(country.getCode()+"."+serveId+"."+sender.getId());

       Optional<ServiceProvider> optional = serviceProviderRepository2.findById(serveId);
       if(!optional.isPresent())throw new Exception("Cannot establish communication");

       ServiceProvider serviceProvider = optional.get();

       Connection connection = new Connection();
       connection.setUser(sender);
       connection.setServiceProvider(serviceProvider);

       Connection savedConnection = connectionRepository2.save(connection);

       sender.getConnectionList().add(savedConnection);
       sender.getServiceProviderList().add(serviceProvider);

       return userRepository2.save(sender);





    }
}
