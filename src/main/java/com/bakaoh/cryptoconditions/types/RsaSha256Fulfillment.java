package com.bakaoh.cryptoconditions.types;

import com.bakaoh.cryptoconditions.Condition;
import com.bakaoh.cryptoconditions.ConditionType;
import com.bakaoh.cryptoconditions.Fulfillment;
import com.bakaoh.cryptoconditions.UnsignedBigInteger;
import com.bakaoh.cryptoconditions.der.DEROutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;

public class RsaSha256Fulfillment implements Fulfillment
{
  public static final BigInteger PUBLIC_EXPONENT = BigInteger.valueOf(65537L);
  private RsaSha256Condition condition;
  private RSAPublicKey publicKey;
  private byte[] signature;
  
  public RsaSha256Fulfillment(RSAPublicKey publicKey, byte[] signature)
  {
    this.signature = new byte[signature.length];
    System.arraycopy(signature, 0, this.signature, 0, signature.length);
    this.publicKey = publicKey;
  }
  
  public ConditionType getType()
  {
    return ConditionType.RSA_SHA256;
  }
  
  public RSAPublicKey getPublicKey() {
    return publicKey;
  }
  
  public byte[] getSignature() {
    byte[] signature = new byte[this.signature.length];
    System.arraycopy(this.signature, 0, signature, 0, this.signature.length);
    return signature;
  }
  
  public byte[] getEncoded()
  {
    try
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DEROutputStream out = new DEROutputStream(baos);
      out.writeTaggedObject(0, UnsignedBigInteger.toUnsignedByteArray(publicKey.getModulus()));
      out.writeTaggedObject(1, signature);
      out.close();
      byte[] buffer = baos.toByteArray();
      

      baos = new ByteArrayOutputStream();
      out = new DEROutputStream(baos);
      out.writeTaggedConstructedObject(getType().getTypeCode(), buffer);
      out.close();
      
      return baos.toByteArray();
    }
    catch (IOException e) {
      throw new RuntimeException("DER Encoding Error", e);
    }
  }
  
  public RsaSha256Condition getCondition()
  {
    if (condition == null) {
      condition = new RsaSha256Condition(publicKey);
    }
    return condition;
  }
  

  public boolean verify(Condition condition, byte[] message)
  {
    if (condition == null) {
      throw new IllegalArgumentException("Can't verify a RsaSha256Fulfillment against an null condition.");
    }
    

    if (!(condition instanceof RsaSha256Condition)) {
      throw new IllegalArgumentException("Must verify a RsaSha256Fulfillment against RsaSha256Condition.");
    }
    

    if (!getCondition().equals(condition)) {
      return false;
    }
    try
    {
      Signature rsaSigner = Signature.getInstance("SHA256withRSA/PSS");
      rsaSigner.initVerify(publicKey);
      rsaSigner.update(message);
      return rsaSigner.verify(signature);
    }
    catch (InvalidKeyException|NoSuchAlgorithmException|SignatureException e) {
      e.printStackTrace(); }
    return false;
  }
}
