package com.samourai.whirlpool.client.tx0;

import com.samourai.stomp.client.AndroidStompClient;
import com.samourai.wallet.api.backend.beans.UnspentResponse;
import com.samourai.wallet.bip69.BIP69OutputComparator;
import com.samourai.wallet.client.Bip84Wallet;
import com.samourai.wallet.hd.HD_Address;
import com.samourai.wallet.segwit.bech32.Bech32UtilGeneric;
import com.samourai.wallet.util.FeeUtil;
import com.samourai.whirlpool.client.wallet.WhirlpoolWalletConfig;
import com.samourai.whirlpool.protocol.fee.WhirlpoolFee;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AndroidTx0Service extends Tx0Service {
    private Logger log = LoggerFactory.getLogger(AndroidTx0Service.class.getSimpleName());

    private final Bech32UtilGeneric bech32Util = Bech32UtilGeneric.getInstance();
    private final FeeUtil feeUtil = FeeUtil.getInstance();

    public AndroidTx0Service(WhirlpoolWalletConfig config) {
        super(config);
    }

    protected long computeTx0MinerFee(int nbPremix, long feeTx0, Collection<? extends UnspentResponse.UnspentOutput> spendFroms) {

        // TODO handle non-segwit inputs
        int nbOutputsNonOpReturn = nbPremix + 2; // outputs + change + fee

        // spend from N bech32 input
        int nbSpendFroms = (spendFroms != null ? spendFroms.size() : 1);
        long tx0MinerFee = feeUtil.estimatedFeeSegwit(0, 0, nbSpendFroms, nbOutputsNonOpReturn, 1, feeTx0);

        if (log.isDebugEnabled()) {
            log.debug(
                    "tx0 minerFee: "
                            + tx0MinerFee
                            + "sats, totalBytes="
                            + "b for nbPremix="
                            + nbPremix
                            + ", feeTx0="
                            + feeTx0);
        }
        return tx0MinerFee;
    }

    @Override
    protected void buildTx0Input(Transaction tx, UnspentOutputWithKey input, NetworkParameters params) {

        // TODO handle non-segwit inputs
        ECKey spendFromKey = ECKey.fromPrivate(input.getKey());
        TransactionOutPoint depositSpendFrom = input.computeOutpoint(params);
        final Script segwitPubkeyScript = ScriptBuilder.createP2WPKHOutputScript(spendFromKey);
        tx.addSignedInput(depositSpendFrom, segwitPubkeyScript, spendFromKey);
    }
}
