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
        CountryName receiverCountry = null;
       if(!receiver.getConnected()){
          receiverCountry = receiver.getOriginalCountry().getCountryName();
       }
       else {
          String maskIp = receiver.getMaskedIp();
          if(maskIp.length()==0 || maskIp.length()<3)  throw new Exception("Cannot establish communication");
          String countryCode = "";
          for (int i =0;i<=2;i++)
          {
              countryCode += maskIp.charAt(i);
          }

          Optional<CountryName> optionalCountryName = CountryName.byFullNameIgnoreCase(countryCode);
          if(!optionalCountryName.isPresent()) throw new Exception("Cannot establish communication");

          receiverCountry = optionalCountryName.get();

       }
       if(sender.getOriginalCountry().getCountryName().equals(receiverCountry)) return sender;

        ServiceProvider serviceProvider = null;
        int id = Integer.MAX_VALUE;
        for (ServiceProvider s : sender.getServiceProviderList())
        {
            if(s.getCountryList()==null) {
                continue;
            }
            else{
                for(Country c : s.getCountryList())
                {
                    if(c.getCountryName().equals(receiverCountry) && s.getId()<id)
                    {
                        id = s.getId();
                        serviceProvider = s;
                    }
                }
            }
        }

        if(id==Integer.MAX_VALUE)
        {
            throw new Exception("Cannot establish communication");
        }

        Connection connection = new Connection();
        connection.setUser(sender);
        connection.setServiceProvider(serviceProvider);


        sender.setConnected(true);
        sender.setMaskedIp(receiverCountry.toCode()+"."+ id +"."+sender.getId());
        sender.getConnectionList().add(connection);

        serviceProvider.getConnectionList().add(connection);
        //connectionRepository2.save(connection);
        serviceProviderRepository2.save(serviceProvider);
        userRepository2.save(sender);

        return sender;

    }
}
