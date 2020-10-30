package guru.sfg.brewery.web.controllers.api;

import antlr.ASTNULLType;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.sfg.brewery.bootstrap.DefaultBreweryLoader;
import guru.sfg.brewery.domain.Beer;
import guru.sfg.brewery.domain.Customer;
import guru.sfg.brewery.repositories.BeerOrderRepository;
import guru.sfg.brewery.repositories.BeerRepository;
import guru.sfg.brewery.repositories.CustomerRepository;
import guru.sfg.brewery.web.controllers.BaseIT;
import guru.sfg.brewery.web.model.BeerOrderDto;
import guru.sfg.brewery.web.model.BeerOrderLineDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BeerOrderControllerTest extends BaseIT {

    public static final String API_ROOT="/api/v1/customers/";

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Customer stPeteCustomer;
    private Customer dunedinCustomer;
    private Customer keyWestCustomer;
    private List<Beer> loadedBeers;

    @BeforeEach
    public void setUp() {
        super.setUp();
        stPeteCustomer = this.customerRepository.findAllByCustomerName(DefaultBreweryLoader.ST_PETE_DISTRIBUTING).orElseThrow();
        dunedinCustomer = this.customerRepository.findAllByCustomerName(DefaultBreweryLoader.DUNEDIN_DISTRIBUTING).orElseThrow();
        keyWestCustomer = this.customerRepository.findAllByCustomerName(DefaultBreweryLoader.KEY_WEST_DISTRIBUTORS).orElseThrow();
        loadedBeers = this.beerRepository.findAll();
    }

    @Test
    void createOrderNotAuth()throws Exception{
        BeerOrderDto beerOrderDto = buildOrderDto(stPeteCustomer,loadedBeers.get(0).getId());
        mockMvc.perform(post(API_ROOT+stPeteCustomer.getId()+"/orders")
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerOrderDto))
            )
            .andExpect(status().isUnauthorized());
    }

    @WithUserDetails("spring")
    @Test
    void createOrderUserAdmin()throws Exception{
        BeerOrderDto beerOrderDto = buildOrderDto(stPeteCustomer,loadedBeers.get(0).getId());
        mockMvc.perform(post(API_ROOT+stPeteCustomer.getId()+"/orders")
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerOrderDto))
        )
                .andExpect(status().isCreated());
    }

    @WithUserDetails(DefaultBreweryLoader.STPETE_USER)
    @Test
    void createOrderAuthCustomer()throws Exception{
        BeerOrderDto beerOrderDto = buildOrderDto(stPeteCustomer,loadedBeers.get(0).getId());
        mockMvc.perform(post(API_ROOT+stPeteCustomer.getId()+"/orders")
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerOrderDto))
            )
            .andExpect(status().isCreated());
    }

    @WithUserDetails(DefaultBreweryLoader.KEYWEST_USER)
    @Test
    void createOrderNotAuthCustomer()throws Exception{
        BeerOrderDto beerOrderDto = buildOrderDto(stPeteCustomer,loadedBeers.get(0).getId());
        mockMvc.perform(post(API_ROOT+stPeteCustomer.getId()+"/orders")
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerOrderDto))
        )
                .andExpect(status().isForbidden());
    }

    private BeerOrderDto buildOrderDto(Customer customr, UUID beerId) {
        List<BeerOrderLineDto> orderLines = Arrays.asList(BeerOrderLineDto.builder()
                .id(UUID.randomUUID())
                .beerId(beerId)
                .orderQuantity(5)
                .build());

        return BeerOrderDto.builder()
                .customerId(customr.getId())
                .customerRef("123")
                .orderStatusCallbackUrl("http://example.com")
                .beerOrderLines(orderLines)
                .build();
    }

    @Test
    void listOrdersNoAut() throws Exception{
        mockMvc.perform(get(API_ROOT+stPeteCustomer.getId()+"/orders"))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails("spring")
    @Test
    void listOrdersAdmin() throws Exception{
        mockMvc.perform(get(API_ROOT+stPeteCustomer.getId()+"/orders"))
                .andExpect(status().isOk());
    }

    @WithUserDetails(DefaultBreweryLoader.STPETE_USER)
    @Test
    void listOrdersAuthCustomer() throws Exception{
        mockMvc.perform(get(API_ROOT+stPeteCustomer.getId()+"/orders"))
                .andExpect(status().isOk());
    }

    @WithUserDetails(DefaultBreweryLoader.KEYWEST_USER)
    @Test
    void listOrdersNoAuthCustomer() throws Exception{
        mockMvc.perform(get(API_ROOT+stPeteCustomer.getId()+"/orders"))
                .andExpect(status().isForbidden());
    }


    @Disabled
    @Test
    void pickUpOrderNotAuth() {
    }

    @Disabled
    @Test
    void pickUpOrderNotAdminUser() {
    }

    @Disabled
    @Test
    void pickUpOrderCustomerUserAuth() {
    }

    @Disabled
    @Test
    void pickUpOrderCustomerUserNotAuth() {
    }
}
