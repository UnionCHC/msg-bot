package com.desprice.unionchc;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.parity.methods.response.NewAccountIdentifier;
import org.web3j.protocol.parity.methods.response.PersonalUnlockAccount;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class EthereumSer {


    private static final BigInteger GAS_PRICE = BigInteger.valueOf(9_000L);
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(1_500_000L);


    private Web3j mWeb3;
    private Parity mParity;

    private static EthereumSer ourInstance = new EthereumSer();

    public static EthereumSer getInstance() {
        return ourInstance;
    }

    private EthereumSer() {
        init();
    }

    private void init() {
        try {
            String serverUrl = Options.getInstance().getConfig().server;
            if (null == serverUrl) {
                throw new ExceptionConfig("Not configure server path ");
            }
            mWeb3 = Web3j.build(new HttpService(serverUrl));
            mParity = Parity.build(new HttpService(serverUrl));

            String version = mWeb3.web3ClientVersion().send().getWeb3ClientVersion();
            System.out.println("version: " + version);

        } catch (IOException | ExceptionConfig e) {
            e.printStackTrace();
        }
    }

    public boolean checkUnlock(String address, String password) {
        try {
            PersonalUnlockAccount unlockAccount = mParity.personalUnlockAccount(address, password).send();
            System.out.println("unlock result: " + unlockAccount.accountUnlocked());
            return unlockAccount.accountUnlocked();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public String createAccount(String password) {
        try {
            NewAccountIdentifier identifier = mParity.personalNewAccount(password).send();
            System.out.println("account id: " + identifier.getAccountId());
            return identifier.getAccountId();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public BigInteger getBalance(String address) {
        try {
            BigInteger balance = mWeb3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();
            System.out.println("balance: " + Convert.fromWei(new BigDecimal(balance), Convert.Unit.ETHER));
            return balance;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendMoney(String from, String to, String value, String password) {
        try {
            EthGetTransactionCount transactionCount = mWeb3.ethGetTransactionCount(from,
                    DefaultBlockParameterName.LATEST).send();
            BigInteger amount = Convert.toWei(value, Convert.Unit.ETHER).toBigInteger();
            BigInteger nonce = transactionCount.getTransactionCount();
            System.out.println("nonce: " + nonce);

            Transaction transaction = Transaction.createEtherTransaction(from, nonce, GAS_PRICE, GAS_LIMIT, to, amount);
            EthSendTransaction sendTransaction = mParity.personalSignAndSendTransaction(transaction, password).send();

            System.out.println("Transaction hash: " + sendTransaction.getTransactionHash());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String sendContract(String address, String contractAdr, String functionName, String password) {
        try {
            BigInteger nonce = mWeb3.ethGetTransactionCount(
                    address, DefaultBlockParameterName.LATEST).send().getTransactionCount();
            System.out.println("nonce: " + nonce);

            Function function = new Function(functionName, Collections.emptyList(), Collections.emptyList());
            String encodedFunction = FunctionEncoder.encode(function);
            Transaction transaction = Transaction.createFunctionCallTransaction(address, nonce,
                    GAS_PRICE, GAS_LIMIT, contractAdr, encodedFunction);

            /*
            EthSendTransaction result = mParity.personalSignAndSendTransaction(transaction, password).send();
            System.out.println("transaction hash: " + result.getTransactionHash());
            if (result.hasError()) {
                return "transaction hash: " + result.getError().getMessage();
            } else
                return "transaction hash: " + result.getTransactionHash();
            */
            CompletableFuture<EthSendTransaction> result = mParity.personalSignAndSendTransaction(transaction, password).sendAsync();
            System.out.println("transaction hash: " + result.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


}
