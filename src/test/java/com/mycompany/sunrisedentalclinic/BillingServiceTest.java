package com.mycompany.sunrisedentalclinic;

import com.mycompany.sunrisedentalclinic.service.BillingService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BillingServiceTest {

    private final BillingService s = new BillingService();

    @Test
    void total() {
        assertEquals(new BigDecimal("7500.00"), s.calculateTotal(new BigDecimal("2500.00"), new BigDecimal("5000.00")));
    }

    @Test
    void negativeRejected() {
        assertThrows(IllegalArgumentException.class, () -> s.calculateTotal(new BigDecimal("-1"), BigDecimal.ZERO));
    }
}
