package org.vilasha.simpleblockchain;

import java.util.Vector;
import java.util.HashMap;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class BaseChain {
    private static Vector<Block> blockchain = new Vector<Block>();
    private static HashMap<String,TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();

    private static final int difficulty = 3;
    private static float minimumTransaction = 0.1f;
    private static Wallet walletA;
    private static Wallet walletB;
    private static Transaction genesisTransaction;

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());

        walletA = new Wallet();
        walletB = new Wallet();

        Wallet coinbase = new Wallet();

        genesisTransaction = new Transaction(coinbase.getPublicKey(), walletA.getPublicKey(), 100f, null);
        genesisTransaction.generateSignature(coinbase.getPrivateKey());
        genesisTransaction.setTransactionId("0");

        genesisTransaction.getOutputs().add(new TransactionOutput(genesisTransaction.getReciepient(), genesisTransaction.getValue(), genesisTransaction.getTransactionId()));
        UTXOs.put(genesisTransaction.getOutputs().get(0).id, genesisTransaction.getOutputs().get(0));

        System.out.println("Creating and Mining Genesis block... ");

        Block genesis = new Block("0");

        genesis.addTransaction(genesisTransaction);

        addBlock(genesis);

        Block block1 = new Block(genesis.getHash());

        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletA is Attempting to send funds (50) to WalletB...");

        block1.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 40f));

        addBlock(block1);

        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.getHash());

        System.out.println("\nWalletA Attempting to send more funds (100) than it has...");

        block2.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 1000f));

        addBlock(block2);

        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.getHash());

        System.out.println("\nWalletB is Attempting to send funds (250) to WalletA...");

        block3.addTransaction(walletB.sendFunds( walletA.getPublicKey(), 20));

        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        isChainValid();
    }

    private static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>();

        tempUTXOs.put(genesisTransaction.getOutputs().get(0).id, genesisTransaction.getOutputs().get(0));

        for (int i=1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);

            if (!currentBlock.getHash().equals(currentBlock.calculateHash()) ){
                System.out.println("#Current Hashes not equal");

                return false;
            }

            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash()) ) {
                System.out.println("#Previous Hashes not equal");

                return false;
            }

            if (!currentBlock.getHash().substring( 0, difficulty).equals(hashTarget)) {
                System.out.println("#This block hasn't been mined");

                return false;
            }

            TransactionOutput tempOutput;

            for (int t=0; t < currentBlock.getTransactions().size(); t++) {
                Transaction currentTransaction = currentBlock.getTransactions().get(t);

                if(!currentTransaction.verifySignature()) {
                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");

                    return false;
                }

                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");

                    return false;
                }

                for (TransactionInput input: currentTransaction.getInputs()) {
                    tempOutput = tempUTXOs.get(input.getTransactionOutputId());

                    if (tempOutput == null) {
                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");

                        return false;
                    }

                    if (input.getUTXO().value != tempOutput.value) {
                        System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");

                        return false;
                    }

                    tempUTXOs.remove(input.getTransactionOutputId());
                }

                for (TransactionOutput output: currentTransaction.getOutputs()) {
                    tempUTXOs.put(output.id, output);
                }

                if (currentTransaction.getOutputs().get(0).reciepient != currentTransaction.getReciepient()) {
                    System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");

                    return false;
                }

                if (currentTransaction.getOutputs().get(1).reciepient != currentTransaction.getSender()) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");

                    return false;
                }
            }
        }

        System.out.println("Blockchain is valid");

        return true;
    }

    private static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);

        blockchain.add(newBlock);
    }

    public static HashMap<String,TransactionOutput> getUTXOs() {
        return UTXOs;
    }

    public static float getMinimumTransaction() {
        return minimumTransaction;
    }
}