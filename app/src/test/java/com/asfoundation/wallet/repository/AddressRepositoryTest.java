package com.asfoundation.wallet.repository;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by trinkes on 07/03/2018.
 */
public class AddressRepositoryTest {

  private AddressRepository addressRepository;

  @Before public void before() throws Exception {
    addressRepository = new AddressRepository();
  }

  @Test public void getStoreAddress() throws Exception {
    assertEquals(addressRepository.getOemAddress(), ("0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be"));
  }

  @Test public void getOemAddress() throws Exception {
    assertEquals(addressRepository.getOemAddress(), ("0x2c30194bd2e7b6b8ff1467c5af1650f53cd231be"));
  }
}