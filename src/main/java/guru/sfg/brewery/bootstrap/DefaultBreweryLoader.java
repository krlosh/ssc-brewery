/*
 *  Copyright 2020 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package guru.sfg.brewery.bootstrap;

import guru.sfg.brewery.domain.*;
import guru.sfg.brewery.domain.security.Authority;
import guru.sfg.brewery.domain.security.Role;
import guru.sfg.brewery.domain.security.User;
import guru.sfg.brewery.repositories.*;
import guru.sfg.brewery.repositories.security.AuthorityRepository;
import guru.sfg.brewery.repositories.security.RoleRepository;
import guru.sfg.brewery.repositories.security.UserRepository;
import guru.sfg.brewery.web.model.BeerStyleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


/**
 * Created by jt on 2019-01-26.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DefaultBreweryLoader implements CommandLineRunner {

    public static final String TASTING_ROOM = "Tasting Room";
    public static final String ST_PETE_DISTRIBUTING = "St Pete Distributing";
    public static final String DUNEDIN_DISTRIBUTING = "Dunedin Distributing";
    public static final String KEY_WEST_DISTRIBUTORS = "Key West Distributors";

    public static final String BEER_1_UPC = "0631234200036";
    public static final String BEER_2_UPC = "0631234300019";
    public static final String BEER_3_UPC = "0083783375213";
    public static final String STPETE_USER = "stpete";
    public static final String DUNEDIN_USER = "dunedin";
    public static final String KEYWEST_USER = "keywest";

    private final BreweryRepository breweryRepository;
    private final BeerRepository beerRepository;
    private final BeerInventoryRepository beerInventoryRepository;
    private final BeerOrderRepository beerOrderRepository;
    private final CustomerRepository customerRepository;

    private final AuthorityRepository authorityRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        loadSecurityData();
        loadBreweryData();
        loadTastingRoomData();
        loadCustomerData();
    }

    private void loadCustomerData() {
        Role customerRole = this.roleRepository.findByName("CUSTOMER").orElseThrow();

        //Create customers
        Customer stPeteCustomer = this.customerRepository.save(Customer.builder()
                .customerName(ST_PETE_DISTRIBUTING)
                .apiKey(UUID.randomUUID())
                .build());

        Customer dunedinCustomer = this.customerRepository.save(Customer.builder()
                .customerName(DUNEDIN_DISTRIBUTING)
                .apiKey(UUID.randomUUID())
                .build());

        Customer keyWestCustomer = this.customerRepository.save(Customer.builder()
                .customerName(KEY_WEST_DISTRIBUTORS)
                .apiKey(UUID.randomUUID())
                .build());

        //Create Users
        User stPeteUser = userRepository.save(User.builder()
                .username(STPETE_USER)
                .password(passwordEncoder.encode("password"))
                .role(customerRole)
                .customer(stPeteCustomer)
                .build());

        User dunedinUser = userRepository.save(User.builder()
                .username(DUNEDIN_USER)
                .password(passwordEncoder.encode("password"))
                .role(customerRole)
                .customer(dunedinCustomer)
                .build());

        User keyWestUser = userRepository.save(User.builder()
                .username(KEYWEST_USER)
                .password(passwordEncoder.encode("password"))
                .role(customerRole)
                .customer(keyWestCustomer)
                .build());

        createOrder(stPeteCustomer);
        createOrder(dunedinCustomer);
        createOrder(keyWestCustomer);

        log.debug("Orders loaded: " +beerOrderRepository.count());
    }

    private BeerOrder createOrder(Customer customer) {
        return this.beerOrderRepository.save(BeerOrder.builder()
                .customer(customer)
                .orderStatus(OrderStatusEnum.NEW)
                .beerOrderLines(Set.of(BeerOrderLine.builder()
                        .beer(this.beerRepository.findByUpc(BEER_1_UPC))
                        .orderQuantity(2)
                        .build()))
                .build());
    }

    private void loadTastingRoomData() {
        Customer tastingRoom = Customer.builder()
                .customerName(TASTING_ROOM)
                .apiKey(UUID.randomUUID())
                .build();

        customerRepository.save(tastingRoom);

        beerRepository.findAll().forEach(beer -> {
            beerOrderRepository.save(BeerOrder.builder()
                    .customer(tastingRoom)
                    .orderStatus(OrderStatusEnum.NEW)
                    .beerOrderLines(Set.of(BeerOrderLine.builder()
                            .beer(beer)
                            .orderQuantity(2)
                            .build()))
                    .build());
        });
    }

    private void loadBreweryData() {
        if (breweryRepository.count() == 0) {
            breweryRepository.save(Brewery
                    .builder()
                    .breweryName("Cage Brewing")
                    .build());

            Beer mangoBobs = Beer.builder()
                    .beerName("Mango Bobs")
                    .beerStyle(BeerStyleEnum.IPA)
                    .minOnHand(12)
                    .quantityToBrew(200)
                    .upc(BEER_1_UPC)
                    .build();

            beerRepository.save(mangoBobs);
            beerInventoryRepository.save(BeerInventory.builder()
                    .beer(mangoBobs)
                    .quantityOnHand(500)
                    .build());

            Beer galaxyCat = Beer.builder()
                    .beerName("Galaxy Cat")
                    .beerStyle(BeerStyleEnum.PALE_ALE)
                    .minOnHand(12)
                    .quantityToBrew(200)
                    .upc(BEER_2_UPC)
                    .build();

            beerRepository.save(galaxyCat);
            beerInventoryRepository.save(BeerInventory.builder()
                    .beer(galaxyCat)
                    .quantityOnHand(500)
                    .build());

            Beer pinball = Beer.builder()
                    .beerName("Pinball Porter")
                    .beerStyle(BeerStyleEnum.PORTER)
                    .minOnHand(12)
                    .quantityToBrew(200)
                    .upc(BEER_3_UPC)
                    .build();

            beerRepository.save(pinball);
            beerInventoryRepository.save(BeerInventory.builder()
                    .beer(pinball)
                    .quantityOnHand(500)
                    .build());

        }
    }

    private void loadSecurityData() {
        //Beer auths
        Authority createBeer = authorityRepository.save(Authority.builder().permission("beer.create").build());
        Authority updateBeer = authorityRepository.save(Authority.builder().permission("beer.update").build());
        Authority readBeer = authorityRepository.save(Authority.builder().permission("beer.read").build());
        Authority deleteBeer = authorityRepository.save(Authority.builder().permission("beer.delete").build());

        //Customer auths
        Authority createCustomer = authorityRepository.save(Authority.builder().permission("customer.create").build());
        Authority updateCustomer = authorityRepository.save(Authority.builder().permission("customer.update").build());
        Authority readCustomer = authorityRepository.save(Authority.builder().permission("customer.read").build());
        Authority deleteCustomer = authorityRepository.save(Authority.builder().permission("customer.delete").build());

        //Brewery auths
        Authority createBrewery = authorityRepository.save(Authority.builder().permission("brewery.create").build());
        Authority updateBrewery = authorityRepository.save(Authority.builder().permission("brewery.update").build());
        Authority readBrewery = authorityRepository.save(Authority.builder().permission("brewery.read").build());
        Authority deleteBrewery = authorityRepository.save(Authority.builder().permission("brewery.delete").build());

        //Beer order auths
        Authority createOrder = authorityRepository.save(Authority.builder().permission("order.create").build());
        Authority updateOrder = authorityRepository.save(Authority.builder().permission("order.update").build());
        Authority readOrder = authorityRepository.save(Authority.builder().permission("order.read").build());
        Authority deleteOrder = authorityRepository.save(Authority.builder().permission("order.delete").build());
        Authority pickupOrder = authorityRepository.save(Authority.builder().permission("order.pickup").build());
        Authority createOrderCustomer = authorityRepository.save(Authority.builder().permission("customer.order.create").build());
        Authority updateOrderCustomer = authorityRepository.save(Authority.builder().permission("customer.order.update").build());
        Authority readOrderCustomer = authorityRepository.save(Authority.builder().permission("customer.order.read").build());
        Authority deleteOrderCustomer = authorityRepository.save(Authority.builder().permission("customer.order.delete").build());
        Authority pickupOrderCustomer = authorityRepository.save(Authority.builder().permission("customer.order.pickup").build());

        Role adminRole = roleRepository.save(Role.builder().name("ADMIN").build());
        Role customerRole = roleRepository.save(Role.builder().name("CUSTOMER").build());
        Role userRole = roleRepository.save(Role.builder().name("USER").build());

        adminRole.setAuthorities(new HashSet<>(Set.of(createBeer,updateBeer,readBeer,deleteBeer,
                createCustomer,updateCustomer,readCustomer,deleteCustomer,
                createBrewery,updateBrewery,readBrewery,deleteBrewery,
                createOrder,updateOrder,readOrder, deleteOrder, pickupOrder)));

        customerRole.setAuthorities(new HashSet<>(Set.of(readBeer,readCustomer, readBrewery,
                createOrderCustomer, readOrderCustomer, updateOrderCustomer, deleteOrderCustomer, pickupOrderCustomer)));

        userRole.setAuthorities(new HashSet<>(Set.of(readBeer)));

        roleRepository.saveAll(Arrays.asList(adminRole, customerRole, userRole));

        userRepository.save(User.builder()
                .username("spring")
                .password(passwordEncoder.encode("guru"))
                .role(adminRole)
                .build());

        userRepository.save(User.builder()
                .username("user")
                .password(passwordEncoder.encode("password"))
                .role(userRole)
                .build());

        userRepository.save(User.builder()
                .username("scott")
                .password(passwordEncoder.encode("tiger"))
                .role(customerRole)
                .build());
        /*if(!userRepository.findByUsername("spring").isPresent()) {
            User spring = User.builder()
                    .username("spring")
                    .password(passwordEncoder.encode("guru"))
                    .role(adminRole)
                    .build();
            userRepository.save(spring);
        }

        if(!userRepository.findByUsername("user").isPresent()) {
            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("password"))
                    .role(userRole)
                    .build();
            userRepository.save(user);
        }

        if(!userRepository.findByUsername("scott").isPresent()) {
            User scott = User.builder()
                    .username("scott")
                    .password(passwordEncoder.encode("tiger"))
                    .role(customerRole)
                    .build();
            userRepository.save(scott);
        }*/
        log.debug("Users loaded {}",userRepository.count());
    }
}
