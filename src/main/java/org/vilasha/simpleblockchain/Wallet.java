package org.vilasha.simpleblockchain;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Wallet {
    private PrivateKey privateKey;
    private PublicKey publicKey;

    private final HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();

    public Wallet() {
        this.generateKeyPair();
    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();

            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public float getBalance() {
        float total = 0;

        for (Map.Entry<String, TransactionOutput> item: BaseChain.getUTXOs().entrySet()){
            TransactionOutput UTXO = item.getValue();

            if (UTXO.isMine(publicKey)) {
                UTXOs.put(UTXO.id,UTXO);

                total += UTXO.value;
            }
        }

        return total;
    }

    public Transaction sendFunds(PublicKey _recipient,float value ) {
        if (this.getBalance() < value) {
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");

            return null;
        }

        Vector<TransactionInput> inputs = new Vector<TransactionInput>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();

            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));

            if (total > value) {
                break;
            }
        }

        Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);

        newTransaction.generateSignature(privateKey);

        for (TransactionInput input: inputs){
            UTXOs.remove(input.getTransactionOutputId());
        }

        return newTransaction;
    }

    public final PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public final PublicKey getPublicKey() {
        return this.publicKey;
    }
}