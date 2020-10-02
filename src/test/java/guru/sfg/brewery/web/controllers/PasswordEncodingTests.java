package guru.sfg.brewery.web.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordEncodingTests {

    static final String PASSWORD = "password";

    @Test
    void testBCrypt() {
        PasswordEncoder bcrypt = new BCryptPasswordEncoder();
        System.out.println("bcrypt "+PASSWORD+"->" +bcrypt.encode(PASSWORD));
        System.out.println(bcrypt.encode(PASSWORD));
        System.out.println("bcrypt guru->" + bcrypt.encode("guru"));
        System.out.println("bcrypt tiger->"+bcrypt.encode("tiger"));
    }

    @Test
    void testBCrypt15() {
        PasswordEncoder bcrypt = new BCryptPasswordEncoder(15);
        System.out.println("bcrypt15 "+PASSWORD+"->" +bcrypt.encode(PASSWORD));
        System.out.println(bcrypt.encode(PASSWORD));
        System.out.println("bcrypt15 guru->" + bcrypt.encode("guru"));
        System.out.println("bcrypt15 tiger->"+bcrypt.encode("tiger"));
    }

    @Test
    void testSha256() {
        PasswordEncoder sha256 = new StandardPasswordEncoder();
        System.out.println("sha256 "+PASSWORD+"->" +sha256.encode(PASSWORD));
        System.out.println("sha256 "+PASSWORD+"->" +sha256.encode(PASSWORD));
        System.out.println("sha256 guru->" + sha256.encode("guru"));
        System.out.println("sha256 tiger->"+sha256.encode("tiger"));
    }

    @Test
    void testLdap() {
        PasswordEncoder ldap = new LdapShaPasswordEncoder();

        System.out.println("ldap "+PASSWORD+"->"+ldap.encode(PASSWORD));
        System.out.println("ldap "+PASSWORD+"->"+ldap.encode(PASSWORD));

        String passEncoded = ldap.encode(PASSWORD);

        assertTrue(ldap.matches(PASSWORD,passEncoded));

        System.out.println("ldap guru->"+ldap.encode("guru"));
        System.out.println("ldap tiger->"+ldap.encode("tiger"));

    }

    @Test
    void testNoOp() {
        PasswordEncoder noOp = NoOpPasswordEncoder.getInstance();

        System.out.println(noOp.encode(PASSWORD));
    }

    @Test
    void hashingExample() {
        System.out.println(DigestUtils.md5DigestAsHex(PASSWORD.getBytes()));
        System.out.println(DigestUtils.md5DigestAsHex(PASSWORD.getBytes()));

        String salted = PASSWORD+"thisIsMySALTVALUE";
        System.out.println(DigestUtils.md5DigestAsHex(salted.getBytes()));
    }
}
