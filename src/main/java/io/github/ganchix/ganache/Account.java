package io.github.ganchix.ganache;

import java.math.BigInteger;

/**
 * Created by Rafael Ríos on 20/05/18.
 */
public class Account {
    public final String privateKey;
    public final BigInteger balance;

    public Account(String privateKey, BigInteger balance) {
        this.privateKey = privateKey;
        this.balance = balance;
    }
}
