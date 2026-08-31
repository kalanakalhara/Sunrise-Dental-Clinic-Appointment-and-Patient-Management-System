package com.mycompany.sunrisedentalclinic;

import com.mycompany.sunrisedentalclinic.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void samePasswordSameHash() {
        assertEquals(PasswordUtil.hashPassword("admin123"), PasswordUtil.hashPassword("admin123"));
    }

    @Test
    void differentPasswordDifferentHash() {
        assertNotEquals(PasswordUtil.hashPassword("admin123"), PasswordUtil.hashPassword("wrong"));
    }
}
