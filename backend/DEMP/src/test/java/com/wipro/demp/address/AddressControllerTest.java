package com.wipro.demp.address;

import com.wipro.demp.auth.TestConfig;
import com.wipro.demp.controller.AddressController;
import com.wipro.demp.entity.Address;
import com.wipro.demp.service.AddressService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestConfig.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressService addressService;

    private Address sampleAddress() {
        Address address = new Address();
        address.setAddressId(1);
        address.setAddress("Madhapur");
        address.setState("TS");
        address.setCountry("India");
        address.setPincode("500081");
        return address;
    }

    @Test
    void createAddressSuccess() throws Exception {
        Mockito.when(addressService.addAddress(any(Address.class))).thenReturn(sampleAddress());

        String payload = """
                {
                    "address": "Madhapur",
                    "state": "TS",
                    "country": "India",
                    "pincode": "500081"
                }
                """;

        mockMvc.perform(post("/api/admin/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.address").value("Madhapur"));
    }

    @Test
    void updateAddressInvalidInput() throws Exception {
        mockMvc.perform(put("/api/admin/-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid request body."));
    }

    @Test
    void updateAddressSuccess() throws Exception {
        Mockito.when(addressService.updateAddress(eq(1), any(Address.class))).thenReturn(sampleAddress());

        String payload = """
                {
                    "address": "Madhapur",
                    "state": "TS",
                    "country": "India",
                    "pincode": "500081"
                }
                """;

        mockMvc.perform(put("/api/admin/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pincode").value("500081"));
    }

    @Test
    void getAddressInvalidId() throws Exception {
        mockMvc.perform(get("/api/admin/-2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid address ID."));
    }

    @Test
    void getAddressFound() throws Exception {
        Mockito.when(addressService.getAddress(1)).thenReturn(sampleAddress());

        mockMvc.perform(get("/api/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressId").value(1));
    }

    @Test
    void getAllAddresses() throws Exception {
        Mockito.when(addressService.getAllAddresses()).thenReturn(List.of(sampleAddress()));

        mockMvc.perform(get("/api/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].country").value("India"));
    }

    @Test
    void deleteAddressInvalidId() throws Exception {
        mockMvc.perform(delete("/api/admin/-5"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid address ID."));
    }

    @Test
    void deleteAddressSuccess() throws Exception {
        mockMvc.perform(delete("/api/admin/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Address deleted successfully!"));
    }
}
