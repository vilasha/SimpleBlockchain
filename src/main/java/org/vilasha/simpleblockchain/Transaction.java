package org.vilasha.simpleblockchain;

import java.security.*;
import java.util.Vector;

public class Transaction {
    private String transactionId;
    private PublicKey sender;
    private PublicKey reciepient;
    private float value;
    private byte[] signature;

    private Vector<TransactionInput> inputs = new Vector<TransactionInput>();
    private Vector<TransactionOutput> outputs = new Vector<TransactionOutput>();

    private static int sequence = 0;

    public Transaction(PublicKey from, PublicKey to, float value,  Vector<TransactionInput> inputs) {
        this.sender = from;
        this.reciepient = to;
        this.value = value;
        this.inputs = inputs;
    }

    public boolean processTransaction() {
        if(verifySignature() == false) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        for (TransactionInput i : inputs) {
            i.setUTXO(BaseChain.getUTXOs().get(i.getTransactionOutputId()));
        }

        if (getInputsValue() < BaseChain.getMinimumTransaction()) {
            System.out.println("Transaction Inputs too small: " + getInputsValue());
            System.out.println("Please enter the amount greater than " + BaseChain.getMinimumTransaction());

            return false;
        }

        float leftOver = getInputsValue() - value;
        transactionId = calulateHash();

        outputs.add(new TransactionOutput( this.reciepient, value,transactionId));
        outputs.add(new TransactionOutput( this.sender, leftOver,transactionId));

        for (TransactionOutput o : outputs) {
            BaseChain.getUTXOs().put(o.id , o);
        }

        for (TransactionInput i : inputs) {
            if (i.getUTXO() == null) {
                continue;
            }

            BaseChain.getUTXOs().remove(i.getUTXO().id);
        }

        return true;
    }

    public float getInputsValue() {
        float total = 0;

        for (TransactionInput i : inputs) {
            if(i.getUTXO() == null) continue;
            total += i.getUTXO().value;
        }

        return total;
    }

    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value);

        signature = StringUtil.applyECDSASig(privateKey,data);
    }

    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender)
		+ StringUtil.getStringFromKey(reciepient) + Float.toString(value);

        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    public float getOutputsValue() {
        float total = 0;

        for (TransactionOutput o : outputs) {
            total += o.value;
        }

        return total;
    }

    private String calulateHash() {
        sequence++;

        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                        StringUtil.getStringFromKey(reciepient) +
                        Float.toString(value) + sequence
        );
    }

    public Vector<TransactionOutput> getOutputs() {
        return this.outputs;
    }

    public Vector<TransactionInput> getInputs() {
        return this.inputs;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(String transactionId ) {
        this.transactionId = transactionId;
    }

    public PublicKey getSender() {
        return this.sender;
    }

    public PublicKey getReciepient() {
        return this.reciepient;
    }

    public float getValue() {
        return this.value;
    }
}