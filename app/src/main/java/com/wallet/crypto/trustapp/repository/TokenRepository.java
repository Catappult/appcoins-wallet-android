package com.wallet.crypto.trustapp.repository;

import com.wallet.crypto.trustapp.entity.Token;
import com.wallet.crypto.trustapp.entity.TokenInfo;
import com.wallet.crypto.trustapp.entity.Wallet;
import com.wallet.crypto.trustapp.service.TokenExplorerClientType;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bytes;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.rx.Web3jRx;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableOperator;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOperator;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;

public class TokenRepository implements TokenRepositoryType {

    private final TokenExplorerClientType tokenNetworkService;
    private final TokenLocalSource tokenLocalSource;

    public TokenRepository(TokenExplorerClientType tokenNetworkService, TokenLocalSource tokenLocalSource) {
        this.tokenNetworkService = tokenNetworkService;
        this.tokenLocalSource = tokenLocalSource;
    }

    @Override
    public Observable<Token[]> fetch(String walletAddress) {
        return Observable.create(new ObservableOnSubscribe<Token[]>() {
            @Override
            public void subscribe(ObservableEmitter<Token[]> e) throws Exception {
                Wallet wallet = new Wallet(walletAddress);
                Token[] tokens = tokenLocalSource.fetch(wallet)
                        .map(items -> {
                            int len = items.length;
                            Token[] result = new Token[len];
                            for (int i = 0; i < len; i++) {
                                result[i] = new Token(items[i], 0);
                            }
                            return result;
                        })
                        .blockingGet();
                e.onNext(tokens);

                tokenNetworkService
                        .fetch(walletAddress)
                        .flatMapCompletable(items -> Completable.fromAction(() -> {
                            for (Token token : items) {
                                try {
                                    tokenLocalSource.put(wallet, token.tokenInfo)
                                            .blockingAwait();
                                } catch (Throwable t) { /* Quietly */ }
                            }
                        })).blockingAwait();

                tokenLocalSource.fetch(wallet)
                        .lift((SingleOperator<Token[], TokenInfo[]>) observer -> new DisposableSingleObserver<TokenInfo[]>() {
                            @Override
                            public void onSuccess(TokenInfo[] items) {
                                int len = items.length;
                                Token[] result = new Token[len];
                                for (int i = 0; i < len; i++) {
                                    result[i] = new Token(items[i], getBalance(wallet, items[i]));
                                }
                                observer.onSuccess(result);
                            }

                            @Override
                            public void onError(Throwable e1) {
                                observer.onError(e1);
                            }
                        });

                Single.just(tokens)
                        .map(new Function<Token[], Map<String, Token>>() {
                            @Override
                            public Map<String, Token> apply(Token[] tokens) throws Exception {
                                Map<String, Token> result = new HashMap<>(tokens.length);
                                for (Token token : tokens) {
                                    result.put(token.tokenInfo.address, token);
                                }
                                return result;
                            }
                        })
                        .compose(new SingleTransformer<Map<String,Token>, Token[]>() {
                            @Override
                            public SingleSource<Token[]> apply(Single<Map<String, Token>> upstream) {
                                Map<String, Token> localTokenMap = upstream.blockingGet();
                                Token[] remoteTokens = tokenNetworkService.fetch(walletAddress).blockingFirst();

                                for (Token remoteToken : remoteTokens) {
                                    if (localTokenMap.containsKey(remoteToken.tokenInfo.address)) {
                                        localTokenMap.put(remoteToken.tokenInfo.address, remoteToken);
                                    }
                                }
                                Token[] result = new Token[localTokenMap.size()];
                                return Single.just(localTokenMap.values().toArray(result));
                            }
                        })
                        .lift(new SingleOperator<Token[], Token[]>() {
                            @Override
                            public SingleObserver<? super Token[]> apply(SingleObserver<? super Token[]> observer) throws Exception {
                                return new DisposableSingleObserver<Token[]>() {
                                    @Override
                                    public void onSuccess(Token[] tokens) {
                                        for (Token token : tokens) {
                                            org.web3j.abi.datatypes.Function function = balanceOf(token.tokenInfo.address);
                                            String responseValue = callSmartContractFunction(function, contractAddress);

                                            List<Type> response = FunctionReturnDecoder.decode(
                                                    responseValue, function.getOutputParameters());
                                            assertThat(response.size(), is(1));
                                            assertThat(response.get(0), equalTo(new Uint256(expected)));
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }
                                };
                            }
                        });
            }
        });
        return Single.fromObservable(tokenNetworkService.fetch(walletAddress));
    }

    private static org.web3j.abi.datatypes.Function balanceOf(String owner) {
        return new org.web3j.abi.datatypes.Function(
                "balanceOf",
                Collections.singletonList(new Address(owner)),
                Collections.singletonList(new TypeReference<Uint256>() {}));
    }

    private String callSmartContractFunction(
            org.web3j.abi.datatypes.Function function, String contractAddress, Wallet wallet) throws Exception {
        String encodedFunction = FunctionEncoder.encode(function);

        org.web3j.protocol.core.methods.response.EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(wallet.address, contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST)
                .sendAsync().get();

        return response.getValue();
    }
}
