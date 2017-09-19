package com.desprice.unionchc;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import rx.Observable;
import rx.functions.Func1;

/**
 * Auto generated code.<br>
 * <strong>Do not modify!</strong><br>
 * Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>, or {@link org.web3j.codegen.SolidityFunctionWrapperGenerator} to update.
 *
 * <p>Generated with web3j version 2.3.1.
 */
public final class MyEvents extends Contract {
    private static final String BINARY = "6060604052341561000f57600080fd5b5b5b5b610247806100216000396000f300606060405263ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416633033413b811461006957806332b7a7611461008e57806349180ff1146100b35780635d33a27f146100c8578063f1eca64c146100ed575b600080fd5b341561007457600080fd5b61007c610102565b60405190815260200160405180910390f35b341561009957600080fd5b61007c610108565b60405190815260200160405180910390f35b34156100be57600080fd5b6100c6610165565b005b34156100d357600080fd5b61007c6101bd565b60405190815260200160405180910390f35b34156100f857600080fd5b6100c66101c3565b005b60015481565b6000805460010180825573ffffffffffffffffffffffffffffffffffffffff3316907fdf50c7bb3b25f812aedef81bc334454040e7b27e27de95a79451d663013b7e179060405190815260200160405180910390a2506000545b90565b6001805481019081905573ffffffffffffffffffffffffffffffffffffffff3316907fa44cf2eef72461c5116acf7805cf31b3673e4556926dfbfbf533dad27313e7e79060405190815260200160405180910390a25b565b60025481565b600280546001019081905573ffffffffffffffffffffffffffffffffffffffff3316907e384b703d4edb31eeb329f5d2e696c6659a3e33d68fe69765e317f62f5f23469060405190815260200160405180910390a25b5600a165627a7a723058207443b4bf7a4afce01c358c7071c2d1743580d9a5ae8d079887a35bc952f1f2dd0029";

    private MyEvents(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    private MyEvents(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public List<MyEventEventResponse> getMyEventEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("MyEvent", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<MyEventEventResponse> responses = new ArrayList<MyEventEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            MyEventEventResponse typedResponse = new MyEventEventResponse();
            typedResponse._from = (Address) eventValues.getIndexedValues().get(0);
            typedResponse._value = (Uint256) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<MyEventEventResponse> myEventEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("MyEvent", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, MyEventEventResponse>() {
            @Override
            public MyEventEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                MyEventEventResponse typedResponse = new MyEventEventResponse();
                typedResponse._from = (Address) eventValues.getIndexedValues().get(0);
                typedResponse._value = (Uint256) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public List<Value1EventEventResponse> getValue1EventEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Value1Event", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<Value1EventEventResponse> responses = new ArrayList<Value1EventEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            Value1EventEventResponse typedResponse = new Value1EventEventResponse();
            typedResponse._from = (Address) eventValues.getIndexedValues().get(0);
            typedResponse._value = (Uint256) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<Value1EventEventResponse> value1EventEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Value1Event", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, Value1EventEventResponse>() {
            @Override
            public Value1EventEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                Value1EventEventResponse typedResponse = new Value1EventEventResponse();
                typedResponse._from = (Address) eventValues.getIndexedValues().get(0);
                typedResponse._value = (Uint256) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public List<Value2EventEventResponse> getValue2EventEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Value2Event", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<Value2EventEventResponse> responses = new ArrayList<Value2EventEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            Value2EventEventResponse typedResponse = new Value2EventEventResponse();
            typedResponse._from = (Address) eventValues.getIndexedValues().get(0);
            typedResponse._value = (Uint256) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<Value2EventEventResponse> value2EventEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Value2Event", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, Value2EventEventResponse>() {
            @Override
            public Value2EventEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                Value2EventEventResponse typedResponse = new Value2EventEventResponse();
                typedResponse._from = (Address) eventValues.getIndexedValues().get(0);
                typedResponse._value = (Uint256) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public Future<Uint256> value1() {
        Function function = new Function("value1", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> sendEvent() {
        Function function = new Function("sendEvent", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<TransactionReceipt> incValue1() {
        Function function = new Function("incValue1", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<Uint256> value2() {
        Function function = new Function("value2", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> incValue2() {
        Function function = new Function("incValue2", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public static Future<MyEvents> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue) {
        return deployAsync(MyEvents.class, web3j, credentials, gasPrice, gasLimit, BINARY, "", initialWeiValue);
    }

    public static Future<MyEvents> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue) {
        return deployAsync(MyEvents.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "", initialWeiValue);
    }

    public static MyEvents load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new MyEvents(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static MyEvents load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new MyEvents(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class MyEventEventResponse {
        public Address _from;

        public Uint256 _value;
    }

    public static class Value1EventEventResponse {
        public Address _from;

        public Uint256 _value;
    }

    public static class Value2EventEventResponse {
        public Address _from;

        public Uint256 _value;
    }
}
