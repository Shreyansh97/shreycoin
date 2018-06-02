import com.google.gson.GsonBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by shreyansh on 29/5/18.
 */
public class Main {

    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();

    public static int difficulty = 5;
    public static float minimumTransaction = 0.1f;
    public static Wallet walletA;
    public static Wallet walletB;
    public static Transaction genesisTransaction;

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());

        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        //Genesis Transaction sending 100 coins to wallet A
        genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
        genesisTransaction.generateSignature(coinbase.privateKey);
        genesisTransaction.transactionId = "0";
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId));
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        System.out.println("Creating and mining genesis block");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        //Testing
        Block block1 = new Block(genesis.hash);
        System.out.println("\nWallet A's balance is: "+walletA.getBalance());
        System.out.println("Wallet A trying to send 40 coins to Wallet B");
        Transaction t = walletA.sendFunds(walletB.publicKey,40f);
        block1.addTransaction(t);
        addBlock(block1);
        System.out.println("Wallet A's balance is: "+walletA.getBalance());
        System.out.println("Wallet B's balance is: "+walletB.getBalance());

        System.out.println();

        Block block2 = new Block(block1.hash);
        System.out.println("Wallet A trying to send 1000 coins to Wallet B");
        block2.addTransaction(walletA.sendFunds(walletB.publicKey,1000f));
        addBlock(block2);
        System.out.println("Wallet A's balance is: "+walletA.getBalance());
        System.out.println("Wallet B's balance is: "+walletB.getBalance());

        System.out.println();

        Block block3 = new Block(block2.hash);
        System.out.println("Wallet B trying to send 30 coins to Wallet A");
        block3.addTransaction(walletB.sendFunds(walletA.publicKey,30f));
        addBlock(block3);
        System.out.println("Wallet A's balance is: "+walletA.getBalance());
        System.out.println("Wallet B's balance is: "+walletB.getBalance());

        isChainValid();
    }

    public static boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = StringUtil.getDifficultyString(difficulty);
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>();
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);

            if(!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("#Current Hashes not equal");
                return false;
            }

            if(!previousBlock.hash.equals(currentBlock.previousHash)) {
                System.out.println("#Previous Hashes not equal");
                return false;
            }

            if(!currentBlock.hash.startsWith(hashTarget)){
                System.out.println("#Block not mined");
                return false;
            }

            TransactionOutput tempOutput;
            for (int t = 0; t<currentBlock.transactions.size();t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if(!currentTransaction.verifySignature()) {
                    System.out.println("Signature on transaction "+t+" not valid");
                    return false;
                }

                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("Inputs not equal to outputs");
                    return false;
                }

                for (TransactionInput input : currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null) {
                        System.out.println("#Referenced Transaction on input missing");
                        return false;
                    }

                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("#Referenced input Transaction value invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for (TransactionOutput output : currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }
                if(currentTransaction.outputs.get(0).recipient != currentTransaction.reciepient) {
                    System.out.println("#Invalid output recipient on transaction");
                    return false;
                }
                if(currentTransaction.outputs.get(1).recipient != currentTransaction.sender) {
                    System.out.println("#Invalid change recipient on transaction");
                    return false;
                }
            }
        }
        System.out.println("Transaction is valid");
        return true;
    }

    public static void addBlock(Block block) {
        block.mine(difficulty);
        blockchain.add(block);
    }
}
