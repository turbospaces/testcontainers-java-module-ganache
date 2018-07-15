package io.github.ganchix.ganache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;

public class GanacheContainerTest {
    private final String PRIVATE_KEY_0 = "27dad0620a057abb1ff71966e6da0608065c8e4bf0669c8f554f07ad94197956";
    private final String PRIVATE_KEY_1 = "bcca18e6ebbb7f2d5fac88ed676ef3e9b6d4d947381d6d977a798238df0ff78a";


	@Rule
	public GanacheContainer ganacheContainer = getDefaultGanacheContainer();

	private GanacheContainer getDefaultGanacheContainer() {
		return new GanacheContainer()
				.withDebug()
				.withPort(1485)
				.withNumberAccounts(2)
				.withDefaultBalanceEther(new BigInteger(String.valueOf(1258)))
				.withNetworkId(10L)
				.withBlockTime(BigInteger.ONE)
				.withMemoryUsage()
				.withSecure()
				.withNoVMErrorsOnRPCResponse()
				.withUnlockedAccountByPosition(Arrays.asList(0, 1));
	}


	@Test
	public void simpleTestWithClientCreation() throws IOException {
		Web3j web3j = ganacheContainer.getWeb3j();
		assertNotNull(web3j);
		assertEquals(web3j.ethBlockNumber().send().getBlockNumber(), BigInteger.ZERO);
		assertNotNull(ganacheContainer.getCredentials());
		assertEquals(ganacheContainer.getCredentials().size(), 2);
	}


	@Test
	public void testAccountCreation() throws IOException {
		ganacheContainer.stop();
		ganacheContainer = new GanacheContainer()
				.withAccounts(
						Arrays.asList(
								new Account(PRIVATE_KEY_0, BigInteger.ONE),
								new Account(PRIVATE_KEY_1, BigInteger.TEN)
						)
				);
		ganacheContainer.start();
		Web3j web3j = ganacheContainer.getWeb3j();
		assertEquals(web3j.ethBlockNumber().send().getBlockNumber(), BigInteger.ZERO);
		assertEquals(ganacheContainer.getCredentials().size(), 2);
		assertEquals(web3j.ethGetBalance(((Credentials) ganacheContainer.getCredentials().get(0)).getAddress(), DefaultBlockParameter.valueOf("latest")).send().getBalance(),
				BigInteger.ONE);

		assertEquals(web3j.ethGetBalance(((Credentials) ganacheContainer.getCredentials().get(1)).getAddress(), DefaultBlockParameter.valueOf("latest")).send().getBalance(),
				BigInteger.TEN);



	}

	@Test(expected = Exception.class)
	public void testGetClientFail(){
		ganacheContainer.stop();
		ganacheContainer.getWeb3j();
	}

}
