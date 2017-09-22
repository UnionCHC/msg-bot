package com.desprice.unionchc;

import com.desprice.unionchc.exceptions.ExceptionConfig;
import com.desprice.unionchc.telegram.BotTelegram;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.parity.methods.response.NewAccountIdentifier;
import org.web3j.protocol.parity.methods.response.PersonalUnlockAccount;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.utils.Convert;
import rx.Subscription;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class EthereumSer {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EthereumSer.class);

    private static final BigInteger GAS_PRICE = BigInteger.valueOf(9_000L);
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(1_500_000L);


    private Web3j mWeb3;
    private Parity mParity;

    private static EthereumSer ourInstance = new EthereumSer();

    private Subscription subscription;


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


    public void setSubscribe(String address, String contractAdr) {
        if (subscription == null || subscription.isUnsubscribed())
            subscription = subscribeTransactions(address, contractAdr);
    }

    public void unSubscribe() {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
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
        if (null == address || address.isEmpty()) {
            LOGGER.debug("getBalance address empty");
            return null;
        }
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
        LOGGER.debug("sendContract " + functionName + " : " + address + " " + contractAdr);
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
/*
            EthSendTransaction result = mParity.personalSignAndSendTransaction(transaction, password).sendAsync().get();
            System.out.println("transaction hash: " + result.toString());
            if (result.hasError()) {
                System.out.println("error: " + result.getError().getMessage());
                return "error: " + result.getError().getMessage();
            } else {
                System.out.println("transaction hash: " + result.getTransactionHash());
                return "transaction hash: " + result.getTransactionHash();
            }
*/


            mParity.personalSignAndSendTransaction(transaction, password)
                    .observable().subscribe(result -> {
               // EthSendTransaction result = x;
                System.out.println("transaction hash: " + result.toString());
                if (result.hasError()) {
                    System.out.println("error: " + result.getError().getMessage());
                    //  return "error: " + result.getError().getMessage();
                    BotTelegram.getInstance().sendInfoToAddress(address, "error: " + result.getError().getMessage());
                } else {
                    System.out.println("transaction hash: " + result.getTransactionHash());
                    //  return "transaction hash: " + result.getTransactionHash();
                    BotTelegram.getInstance().sendInfoToAddress(address, "transaction hash:\n" + result.getTransactionHash());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    public BigInteger getValueEvent(String address, String contractAdr, int value) {
        LOGGER.debug("getValueEvent " + " : " + address + " " + contractAdr);
        try {
            BigInteger nonce = mWeb3.ethGetTransactionCount(
                    address, DefaultBlockParameterName.LATEST).send().getTransactionCount();
            System.out.println("nonce: " + nonce);

            ClientTransactionManager transactionManager = new ClientTransactionManager(mWeb3, address);

            MyEvents events = MyEvents.load(contractAdr, mWeb3, transactionManager, GAS_PRICE, GAS_LIMIT);
            Future<Uint256> uint256;
            if (value == 1) {
                uint256 = events.value1();
            } else {
                uint256 = events.value2();
            }
            System.out.println(uint256.get().getValue());
            return uint256.get().getValue();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Subscription subscribeTransactions(String address, String contractAdr) {
        return mWeb3.transactionObservable().subscribe(tx -> {
            System.out.println("+++ catch new transaction: " + tx.getHash());
            try {
                TransactionReceipt receipt = mWeb3.ethGetTransactionReceipt(tx.getHash()).send().getResult();
                ClientTransactionManager transactionManager = new ClientTransactionManager(mWeb3, address);

                MyEvents events = MyEvents.load(contractAdr, mWeb3, transactionManager, GAS_PRICE, GAS_LIMIT);
                List<MyEvents.MyEventEventResponse> items = events.getMyEventEvents(receipt);
                for (MyEvents.MyEventEventResponse event : items) {
                    System.out.println("from: " + event._from);
                    System.out.println("value: " + event._value.getValue());
                    BotTelegram.getInstance().sendInfoToAddress(event._from.toString(), event._value.getValue().toString());
                }

                List<MyEvents.Value1EventEventResponse> items1 = events.getValue1EventEvents(receipt);
                for (MyEvents.Value1EventEventResponse event : items1) {
                    System.out.println("from: " + event._from);
                    System.out.println("value: " + event._value.getValue());
                    BotTelegram.getInstance().sendInfoToAddress(event._from.toString(), event._value.getValue().toString());
                }

                List<MyEvents.Value2EventEventResponse> items2 = events.getValue2EventEvents(receipt);
                for (MyEvents.Value2EventEventResponse event : items2) {
                    System.out.println("from: " + event._from);
                    System.out.println("value: " + event._value.getValue());
                    BotTelegram.getInstance().sendInfoToAddress(event._from.toString(), event._value.getValue().toString());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private Subscription subscribeEvents(String address, String contractAdr) {
        ClientTransactionManager transactionManager = new ClientTransactionManager(mWeb3, address);
        MyEvents events = MyEvents.load(contractAdr, mWeb3, transactionManager, GAS_PRICE, GAS_LIMIT);
        return events.myEventEventObservable(
                DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
                .subscribe(myEventEventResponse -> {
                    System.out.println("+++ event +++");
                    System.out.print("from: " + myEventEventResponse._from);
                    System.out.print("value: " + myEventEventResponse._value);
                });
    }


}
