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
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.parity.methods.response.NewAccountIdentifier;
import org.web3j.protocol.parity.methods.response.PersonalUnlockAccount;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.utils.Convert;
import rx.Subscription;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.desprice.unionchc.Utils.logException;

public class EthereumWeb3j {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EthereumWeb3j.class);

    private static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L);

    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(1_500_000L);

    private Web3j mWeb3;
    private Parity mParity;

    private static EthereumWeb3j ourInstance = new EthereumWeb3j();

    private Subscription mSubscription;

    public static EthereumWeb3j getInstance() {
        return ourInstance;
    }

    private EthereumWeb3j() {
        init();
    }

    private void init() {
        try {
            String serverUrl = Options.getInstance().getConfig().server;
            if (null == serverUrl) {
                serverUrl = Options.getInstance().getConfig().serverSocket;
                if (null != serverUrl) {
                    mWeb3 = Web3j.build(new UnixIpcService(serverUrl));
                    mParity = Parity.build(new UnixIpcService(serverUrl));
                    LOGGER.debug("UnixIpcService: " + serverUrl);
                } else
                    throw new ExceptionConfig("Not configure server path ");
            } else {
                mWeb3 = Web3j.build(new HttpService(serverUrl));
                mParity = Parity.build(new HttpService(serverUrl));
                LOGGER.debug("HttpService: " + serverUrl);
            }
            String version = mWeb3.web3ClientVersion().send().getWeb3ClientVersion();
            LOGGER.debug("version Web3 : " + version);

        } catch (IOException | ExceptionConfig ex) {
            logException(ex);
        }
    }

    private void checkConnect() {
        try {
            String serverUrl = Options.getInstance().getConfig().serverSocket;
            if (null != serverUrl) {
                File pipe = new File(serverUrl);
                if (pipe.exists())
                    LOGGER.debug("exists");
                else
                    LOGGER.debug(" no exists");
            }
        } catch (Exception ex) {
            logException(ex);
        }
    }

    public void setSubscribe(String address, String contractAdr) {
        if (mSubscription == null || mSubscription.isUnsubscribed())
            mSubscription = subscribeTransactions(address, contractAdr);
    }

    public void unSubscribe() {
        if (mSubscription != null && !mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();
    }

    public boolean checkUnlock(String address, String password) {
        try {
            PersonalUnlockAccount unlockAccount = mParity.personalUnlockAccount(address, password).send();
            if (null != unlockAccount.getError()) {
                LOGGER.debug(unlockAccount.getError().getMessage());
            } else {
                LOGGER.debug("unlock result: " + unlockAccount.accountUnlocked());
                return unlockAccount.accountUnlocked();
            }
        } catch (IOException ex) {
            logException(ex);
        }
        return false;
    }

    public String createAccount(String password) {
        try {
            NewAccountIdentifier identifier = mParity.personalNewAccount(password).send();
            LOGGER.debug("account id: " + identifier.getAccountId());
            return identifier.getAccountId();
        } catch (IOException ex) {
            logException(ex);
        }
        return null;
    }

    public BigInteger getBalance(String address) {
        if (null == address || address.isEmpty()) {
            LOGGER.debug("getBalance address empty");
            return null;
        }
        try {
            checkConnect();
            BigInteger balance = mWeb3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();
            LOGGER.debug("balance: " + Convert.fromWei(new BigDecimal(balance), Convert.Unit.ETHER));
            return balance;
        } catch (IOException ex) {
            checkConnect();
            logException(ex);
        }
        return null;
    }

    public BigInteger getGasPrice() {
        try {
            BigInteger ethGasPrice = mWeb3.ethGasPrice().send().getGasPrice();
            LOGGER.debug("ethGasPrice: " + ethGasPrice);
            return ethGasPrice;
        } catch (IOException ex) {
            logException(ex);
        }
        return null;
    }

    private boolean checkBalance(String address) {
        BigInteger balance = getBalance(address);
        return balance.compareTo(GAS_PRICE) >= 1;
    }

    public void sendMoney(String from, String to, String value, String password) {
        try {
            LOGGER.debug("sendMoney: " + value + " to:" + to);
            EthGetTransactionCount transactionCount = mWeb3.ethGetTransactionCount(from,
                    DefaultBlockParameterName.LATEST).send();
            BigInteger amount = Convert.toWei(value, Convert.Unit.ETHER).toBigInteger();
            BigInteger nonce = transactionCount.getTransactionCount();
            LOGGER.debug("nonce: " + nonce);

            Transaction transaction = Transaction.createEtherTransaction(from, nonce, GAS_PRICE, GAS_LIMIT, to, amount);
            EthSendTransaction sendTransaction = mParity.personalSignAndSendTransaction(transaction, password).send();

            LOGGER.debug("Transaction hash: " + sendTransaction.getTransactionHash());
        } catch (IOException ex) {
            logException(ex);
        }
    }

    public String sendContract(String address, String contractAdr, String functionName, String password) {
        LOGGER.debug("sendContract " + functionName + " : " + address + " " + contractAdr);
        if (!checkBalance(address))
            return "Недостаточно средств для перевода ";
        try {
            BigInteger nonce = mWeb3.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING)
                    .send().getTransactionCount();

            LOGGER.debug("nonceNew: " + nonce);

            Function function = new Function(functionName, Collections.emptyList(), Collections.emptyList());
            String encodedFunction = FunctionEncoder.encode(function);
            Transaction transaction = Transaction.createFunctionCallTransaction(address, nonce,
                    GAS_PRICE, GAS_LIMIT, contractAdr, encodedFunction);

            mParity.personalSignAndSendTransaction(transaction, password)
                    .observable().subscribe(result -> {
                LOGGER.debug("transaction hash: " + result.toString());
                if (result.hasError()) {
                    LOGGER.debug("error: " + result.getError().getMessage());
                    BotTelegram.getInstance().sendInfoToAddress(address, "error: " + result.getError().getMessage());
                } else {
                    LOGGER.debug("transaction hash: " + result.getTransactionHash());
                    BotTelegram.getInstance().sendInfoToAddress(address, "transaction hash:\n" + result.getTransactionHash());
                }
            });
        } catch (IOException ex) {
            logException(ex);
        }
        return "";
    }

    public BigInteger getValueEvent(String address, String contractAdr, int value) {
        LOGGER.debug("getValueEvent " + " : " + address + " " + contractAdr);
        try {
            BigInteger nonce = mWeb3.ethGetTransactionCount(
                    address, DefaultBlockParameterName.LATEST).send().getTransactionCount();
            LOGGER.debug("nonce: " + nonce);

            ClientTransactionManager transactionManager = new ClientTransactionManager(mWeb3, address);

            MyEvents events = MyEvents.load(contractAdr, mWeb3, transactionManager, GAS_PRICE, GAS_LIMIT);
            Future<Uint256> uint256;
            if (value == 1) {
                uint256 = events.value1();
            } else {
                uint256 = events.value2();
            }
            LOGGER.debug("getValueEvent = " + uint256.get().getValue());
            return uint256.get().getValue();
        } catch (IOException | ExecutionException | InterruptedException ex) {
            logException(ex);
        }
        return null;
    }

    private Subscription subscribeTransactions(String address, String contractAdr) {
        return mWeb3.transactionObservable().subscribe(tx -> {
            try {
                TransactionReceipt receipt = mWeb3.ethGetTransactionReceipt(tx.getHash()).send().getResult();
                ClientTransactionManager transactionManager = new ClientTransactionManager(mWeb3, address);

                MyEvents events = MyEvents.load(contractAdr, mWeb3, transactionManager, GAS_PRICE, GAS_LIMIT);
                List<MyEvents.MyEventEventResponse> items = events.getMyEventEvents(receipt);
                for (MyEvents.MyEventEventResponse event : items) {
                    LOGGER.debug("from: " + event._from);
                    LOGGER.debug("value: " + event._value.getValue());
                    BotTelegram.getInstance().sendInfoToAddress(event._from.toString(), event._value.getValue().toString());
                }

                List<MyEvents.Value1EventEventResponse> items1 = events.getValue1EventEvents(receipt);
                for (MyEvents.Value1EventEventResponse event : items1) {
                    LOGGER.debug("from: " + event._from);
                    LOGGER.debug("value: " + event._value.getValue());
                    BotTelegram.getInstance().sendInfoToAddress(event._from.toString(), "1: " + event._value.getValue().toString());
                }

                List<MyEvents.Value2EventEventResponse> items2 = events.getValue2EventEvents(receipt);
                for (MyEvents.Value2EventEventResponse event : items2) {
                    LOGGER.debug("from: " + event._from);
                    LOGGER.debug("value: " + event._value.getValue());
                    BotTelegram.getInstance().sendInfoToAddress(event._from.toString(), "2: " + event._value.getValue().toString());
                }

            } catch (IOException ex) {
                logException(ex);
            }
        });
    }

    private Subscription subscribeEvents(String address, String contractAdr) {
        ClientTransactionManager transactionManager = new ClientTransactionManager(mWeb3, address);
        MyEvents events = MyEvents.load(contractAdr, mWeb3, transactionManager, GAS_PRICE, GAS_LIMIT);
        return events.myEventEventObservable(
                DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
                .subscribe(myEventEventResponse -> {
                    LOGGER.debug("+++ event +++");
                    LOGGER.debug("from: " + myEventEventResponse._from);
                    LOGGER.debug("value: " + myEventEventResponse._value);
                });
    }


}
