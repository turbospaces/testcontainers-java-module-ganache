package io.github.ganchix.ganache;

import static io.github.ganchix.ganache.GanacheConstants.IMAGE;
import static io.github.ganchix.ganache.GanacheConstants.LATEST_VERSION;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

public class GanacheContainer<SELF extends GanacheContainer<SELF>> extends GenericContainer<SELF> {
    private static final Logger LOGGER = LoggerFactory.getLogger( GanacheContainer.class );
    private static final String HOSTNAME = "0.0.0.0";
	private static final Object DRIVER_LOAD_MUTEX = new Object();
	private final List<String> options = new ArrayList<>();
	private Web3j web3j;
	private Integer port = 8545;
	private final List<Credentials> credentials = new ArrayList<>();

	public GanacheContainer() {
		this(LATEST_VERSION);
	}

	public GanacheContainer(String version) {
		super(IMAGE + ":" + version);
        withHostname( HOSTNAME );
	}


	@Override
	protected void configure() {
		withExposedPorts(port);
        withLogConsumer( new LogGanacheExtractorConsumer( LOGGER, this ) );
		if (options.size() > 0) {
            System.out.println( String.join( " ", options ) );
			withCommand(String.join(" ", options));
		}
	}

	void addAccountPrivateKey(Integer position, String privateKey) {
		credentials.add(position, Credentials.create(privateKey));
	}

	public List<Credentials> getCredentials() {
		return this.credentials;
	}

	public SELF withNumberAccounts(Integer accounts) {
		String option = "--accounts ".concat(accounts.toString());
		options.add(option);
		return self();
	}

	public SELF withDefaultBalanceEther(BigInteger defaultBalanceEther) {
		String option = "--defaultBalanceEther ".concat(defaultBalanceEther.toString());
		options.add(option);
		return self();
	}

	public SELF withBlockTime(BigInteger blockTime) {
		String option = "--blockTime ".concat(blockTime.toString());
		options.add(option);
		return self();
	}

	public SELF withDeterministic() {
		options.add("--deterministic");
		return self();
	}


	public SELF withGasPrice(BigInteger gasPrice) {
		String option = "--gasPrice ".concat(gasPrice.toString());
		options.add(option);
		return self();
	}

	public SELF withGasLimit(BigInteger gasLimit) {
		String option = "--gasLimit ".concat(gasLimit.toString());
		options.add(option);
		return self();
	}

	public SELF withNetworkId(Long networkId) {
		String option = "--networkId ".concat(networkId.toString());
		options.add(option);
		return self();
	}

	public SELF withFork(String location) {
		if (!location.startsWith("http"))
            throw new RuntimeException("Location must start with http");
		String option = "--fork ".concat(location);
		options.add(option);
		return self();
	}

	public SELF withNoVMErrorsOnRPCResponse() {
		options.add("--noVMErrorsOnRPCResponse");
		return self();
	}

	public SELF withSecure() {
		options.add("--secure");
		return self();
	}

	public SELF withMnemonic(List<String> words) {
		if (words == null || words.isEmpty())
            throw new RuntimeException("Mnemonic needs a list of words");
		String option = "--mnemonic ".concat(String.join(",", words));
		options.add(option);
		return self();
	}

	public SELF withSeed() {
		options.add("--seed");
		return self();
	}



	public SELF withAccounts(List<Account> accounts) {
		if (accounts == null || accounts.isEmpty())
            throw new RuntimeException("Accounts can't be empty");
        for ( Account next : accounts ) {
            String privateKey = generatePrivateKey( next.privateKey );
            String arg = privateKey + "," + next.balance;
            String option = "--account=" + arg;
            options.add( option );
        }
		return self();
	}

	public SELF withUnlockedAccountByAddress(List<String> addresses) {
		if (addresses == null || addresses.isEmpty())
            throw new RuntimeException("Addresses can't be empty");
		List<String> listOfAccounts = addresses.stream()
				.map(address -> "--unlock=" + address )
				.collect(Collectors.toList());

		options.addAll(listOfAccounts);
		return self();
	}

	public SELF withUnlockedAccountByPosition(List<Integer> positions) {
		if (positions == null || positions.isEmpty())
            throw new RuntimeException("Positions can't be empty");
		List<String> listOfAccounts = positions.stream()
				.map(position -> "--unlock=" + position )
				.collect(Collectors.toList());

		options.addAll(listOfAccounts);
		return self();
	}


	public SELF withPort(Integer port) {
		this.port = port;
		options.add("--port ".concat(port.toString()));
		return self();
	}

    public SELF withHostname(String hostname) {
        options.add( "--hostname ".concat( hostname ) );
        return self();
    }

	public SELF withDebug() {
		options.add("--debug");
		return self();
	}

	public SELF withMemoryUsage() {
		options.add("--mem");
		return self();
	}

	public Web3j getWeb3j() {
		synchronized (DRIVER_LOAD_MUTEX) {
			if (web3j == null) {
				try {
                    web3j = Web3j.build( new HttpService( "http://" + getContainerIpAddress() + ":" + getMappedPort( port ) ) );
                    LOGGER.info( "Start Web3j with net version: {}", web3j.netVersion().send().getNetVersion() );
				} catch (Exception e) {
					throw new RuntimeException("Could not get Web3j", e);
				}
			}
		}

		return web3j;

	}

	private String generatePrivateKey(String privateKey) {
		return privateKey.startsWith("0x") ? privateKey : "0x".concat(privateKey);
	}

}
