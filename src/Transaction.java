import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;

/**
 * Created by shreyansh on 2/6/18.
 */
public class Transaction {
    public String transactionId;
    public PublicKey sender;
    public PublicKey reciepient;
    public float value;
    public byte signature[];

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    private static int sequence = 0;

    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
        sender = from;
        reciepient = to;
        this.value = value;
        this.inputs = inputs;
    }

    public String calculateHash() {
        sequence++;
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender)+
                        StringUtil.getStringFromKey(reciepient)+
                        Float.toString(value)+
                        Integer.toString(sequence)
        );
    }

    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) +
                StringUtil.getStringFromKey(reciepient) +
                Float.toString(value);
        signature = StringUtil.applyECDSASig(privateKey,data);
    }

    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) +
                StringUtil.getStringFromKey(reciepient) +
                Float.toString(value);
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    public float getInputsValue() {
        float total = 0;
        for(TransactionInput i : inputs) {
            if(i.UTXO == null)
                continue;
            total += i.UTXO.value;
        }
        return total;
    }

    public float getOutputsValue() {
        float total = 0;
        for (TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }

    public boolean processTransaction() {
        if(!verifySignature()) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        for (TransactionInput i : inputs) {
            i.UTXO = Main.UTXOs.get(i.transactionOutputId);
        }

        if (getInputsValue() < Main.minimumTransaction) {
            System.out.println("#Transaction inputs too small" + getInputsValue());
            return false;
        }

        float leftOver = getInputsValue() - value;
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(reciepient, value, transactionId));
        outputs.add(new TransactionOutput(sender, leftOver, transactionId));

        for (TransactionOutput o : outputs)
            Main.UTXOs.put(o.id, o);

        for (TransactionInput i : inputs) {
            if (i.UTXO == null)
                continue;
            Main.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }
}
