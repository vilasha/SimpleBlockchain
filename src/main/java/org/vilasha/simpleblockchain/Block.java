package org.vilasha.simpleblockchain;

import java.util.Date;
import java.util.Vector;

public class Block {
    private String hash;
    private String previousHash;
    private String merkleRoot;
    private long timeStamp;
    private int nonce;
    private Vector<Transaction> transactions = new Vector<Transaction>();

    public Block(String previousHash ) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = this.calculateHash();
    }

    public String calculateHash() {
        StringBuilder sb = new StringBuilder();

        sb.append(previousHash);
        sb.append(timeStamp);
        sb.append(nonce);
        sb.append(merkleRoot);

        String calculatedhash = StringUtil.applySha256(sb.toString());

        return calculatedhash;
    }

    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);

        String target = StringUtil.getDificultyString(difficulty);

        while (!hash.substring( 0, difficulty).equals(target)) {
            nonce ++;
            hash = calculateHash();
        }

        System.out.println("Block Mined: " + hash);
    }

    public boolean addTransaction(Transaction transaction) {
        if (transaction == null) {
            return false;
        }

        if ((!"0".equals(previousHash))) {
            if((transaction.processTransaction() != true)) {
                System.out.println("Transaction failed to process. Discarded.");

                return false;
            }
        }

        transactions.add(transaction);

        System.out.println("Transaction Successfully added to Block");

        return true;
    }

    public String getHash(){
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return this.previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public Vector<Transaction> getTransactions(){
        return this.transactions;
    }
}